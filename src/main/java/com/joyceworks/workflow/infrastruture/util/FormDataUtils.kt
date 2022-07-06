package com.joyceworks.api.infrastructure.util.form

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Charsets
import com.google.common.io.Files
import com.joyceworks.api.infrastructure.GeneralException
import com.joyceworks.api.infrastructure.Result
import com.joyceworks.api.infrastructure.db.mapper.FormMapper
import com.joyceworks.api.infrastructure.db.mapper.NativeQueryMapper
import com.joyceworks.api.infrastructure.schema.CellData
import com.joyceworks.api.infrastructure.schema.CellDataType
import com.joyceworks.api.infrastructure.schema.DBDataType
import com.joyceworks.api.infrastructure.util.JSONUtils
import com.joyceworks.api.infrastructure.util.NamingUtils.camel2Underscore
import com.joyceworks.api.infrastructure.util.NamingUtils.underscore2Camel
import org.apache.commons.csv.CSVFormat
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.PrintWriter
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.script.Invocable
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

@Service
class FormDataUtils(
    private val nativeQueryMapper: NativeQueryMapper,
    private val formMapper: FormMapper,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${workspace}/form")
    private val formPath: String? = null

    fun get(formId: Long, dataId: Long): Map<String, Any> {
        val form = formMapper.findById(formId).orElseThrow { GeneralException("表单不存在: ${formId}") }
        val data = nativeQueryMapper.findById(dataId, form.tableName)
        val cellDataList = getCellDataList(formId)
        for (cellData in cellDataList) {
            if (cellData.isList) {
                val detailTableName = "${form.tableName}_${cellData.id}"
                val detailForeignKey = "${form.tableName}_id"
                val detail = nativeQueryMapper.find(
                    "select * from $detailTableName where $detailForeignKey = :fk",
                    HashMap<String, Any>().apply { this["fk"] = dataId }
                )
                data[cellData.id] = detail
            }
        }
        return data
    }

    fun add(formId: Long, json: String): Result<Long?> {
        val map = deserializeFormData(json)
        val form = formMapper.findById(formId).orElse(null)
            ?: return Result(false, "表单不存在", null)
        val cellDataList = deserializeCellDataList(readContent(formId))

        // Save master
        val insert: MutableMap<String?, Any> = HashMap()
        for ((key, value) in map) {
            if (cellDataList.none { it.id == key && it.isList }) {
                insert[key] = value
            }
        }
        val id = nativeQueryMapper.insert(insert, form.tableName)

        // Save details
        for (cellData in cellDataList) {
            if (cellData.isList) {
                val detail = map[cellData.id] as List<HashMap<String, Any>>?
                for (detailItem in detail!!) {
                    val detailTableName = String.format("%s_%s", form.tableName, cellData.id)
                    val detailForeignKey = String.format("%s_id", form.tableName)
                    detailItem[underscore2Camel(detailForeignKey)] = id
                    nativeQueryMapper.insert(detailItem, detailTableName)
                }
                map.remove(cellData.id)
            }
        }
        val args = HashMap<String, Any>().apply { this["id"] = id }
        val invocable = createInvocable(readEventScript(formId))
        try {
            val result = invocable.invokeFunction("onCreate", args)
            log.info(
                "Complete eval onCreate, result: ${ObjectMapper().writeValueAsString(result)}"
            )
        } catch (e: NoSuchMethodException) {
            log.info("Skip eval onCreate: no such method.")
        }
        return Result(true, "保存成功", id)
    }

    private fun createInvocable(script: String): Invocable {
        val manager = ScriptEngineManager().apply {
            bindings = SimpleBindings().apply {
                this["log"] = log
                this["nativeQueryMapper"] = nativeQueryMapper
            }
        }
        val nashorn = manager.getEngineByName("nashorn").apply { eval(script) }
        return nashorn as Invocable
    }

    fun update(formId: Long, json: String): Result<Void?> {
        val map = deserializeFormData(json)
        val form = formMapper.findById(formId).orElse(null)
            ?: return Result(false, "表单不存在", null)

        // Save details and remove entry sets from map
        val cellDataList = deserializeCellDataList(readContent(formId))
        for (cellData in cellDataList) {
            if (cellData.isList) {
                val detail = map[cellData.id] as List<Map<String, Any>>
                val detailTableName = "${form.tableName}_${cellData.id}"
                val detailForeignKey = "${form.tableName}_id"

                // Delete not exist
                val parameters = HashMap<String, Any?>().apply {
                    this["ids"] = detail.filter { it.containsKey("id") }.map { it["id"] }
                    this["fk"] = map["id"]
                }

                nativeQueryMapper.execute(
                    "delete from $detailTableName where $detailForeignKey = :fk and id not in (:ids)",
                    parameters
                )
                for (detailItem in detail) {
                    if (detailItem.containsKey("id")) {
                        nativeQueryMapper.update(detailItem, detailTableName)
                    } else {
                        nativeQueryMapper.insert(detailItem, detailTableName)
                    }
                }
                map.remove(cellData.id)
            }
        }
        nativeQueryMapper.update(map, form.tableName)
        return Result(true, "保存成功", null)
    }

    fun deserializeCellDataList(content: String?): List<CellData> {
        val cellDataListRef = object : TypeReference<ArrayList<CellData>>() {}
        return JSONUtils.OBJECT_MAPPER.readValue(content, cellDataListRef)
    }

    private fun deserializeFormData(json: String): HashMap<String, Any> {
        val reference = object : TypeReference<HashMap<String, Any>>() {}
        return JSONUtils.OBJECT_MAPPER.readValue(json, reference)
    }

    fun readContent(id: Long): String {
        val rootPath = formPath
        val relativePath = String.format("/%d.json", id)
        val contentPathname = rootPath + relativePath
        val contentFile = File(contentPathname)
        return Files.readLines(contentFile, Charsets.UTF_8).stream()
            .collect(Collectors.joining(System.lineSeparator()))
    }

    fun readScript(id: Long, type: String): String {
        val rootPath = formPath
        val scriptRelativePath = String.format("/%d.%s.js", id, type)
        val scriptPathname = rootPath + scriptRelativePath
        val scriptFile = File(scriptPathname)
        return if (scriptFile.exists()) {
            Files.readLines(scriptFile, Charsets.UTF_8).joinToString(System.lineSeparator())
        } else StringUtils.EMPTY
    }

    fun readEventScript(id: Long): String {
        val rootPath = formPath
        val globalScriptRelativePath = "/global.js"
        val globalScriptPathname = rootPath + globalScriptRelativePath
        val globalScriptFile = File(globalScriptPathname)
        val globalScript = Files.readLines(globalScriptFile, Charsets.UTF_8).stream()
            .collect(Collectors.joining(System.lineSeparator()))
        return globalScript + readScript(id, "event")
    }

    fun readClientScript(id: Long): String {
        return readScript(id, "client")
    }

    fun writeContent(id: Long, content: String) {
        val rootPath = formPath
        val relativePath = String.format("/%d.json", id)
        val pathname = rootPath + relativePath
        write(pathname, content)
    }

    fun writeScript(id: Long, script: String) {
        val rootPath = formPath
        val relativePath = String.format("/%d.client.js", id)
        val pathname = rootPath + relativePath
        write(pathname, script)
    }

    private fun write(pathname: String, content: String) {
        val file = File(pathname)
        Files.write(content.toByteArray(), file)
    }

    fun createTable(tableName: String, content: String?) {
        val cellDataListRef = object : TypeReference<ArrayList<CellData>>() {}
        val cellDataList: List<CellData> =
            JSONUtils.OBJECT_MAPPER.readValue(content, cellDataListRef)
        val sb = StringBuilder(
            "CREATE TABLE `$tableName` (" +
                    "`id` bigint NOT NULL, " +
                    "`creator_id` bigint DEFAULT NULL, " +
                    "`created_at` datetime DEFAULT CURRENT_TIMESTAMP, "
        )
        for (cellData in cellDataList) {
            if (!cellData.isList) {
                val dbType = convertCellDataTypeToDBType(cellData.type)
                val columnName = camel2Underscore(cellData.id!!)
                sb.append("`$columnName` $dbType DEFAULT NULL,")
                continue
            }
            val childTableName = "${tableName}_${cellData.id}"
            createChildTable(childTableName, cellData.lanes!![0].cellDataList!!, tableName)
        }
        sb.append(" PRIMARY KEY (`id`));")
        nativeQueryMapper.execute(sb.toString())
    }

    fun alterTable(tableName: String, content: String?) {
        val cellDataListRef = object : TypeReference<ArrayList<CellData>>() {}
        val cellDataList = JSONUtils.OBJECT_MAPPER.readValue(content, cellDataListRef)
        for (cellData in cellDataList) {
            if (!cellData.isList) {
                val column = camel2Underscore(cellData.id!!)
                if (!exist(tableName, column)) {
                    val dbType = convertCellDataTypeToDBType(cellData.type)
                    val query = "ALTER TABLE `$tableName` ADD COLUMN `$column` $dbType DEFAULT NULL"
                    nativeQueryMapper.execute(query)
                }
                continue
            }
            val childTableName = "${tableName}_${cellData.id}"
            if (!exist(childTableName)) {
                createChildTable(childTableName, cellData.lanes!![0].cellDataList!!, tableName)
            } else {
                alterChildTable(childTableName, cellData.lanes!![0].cellDataList!!)
            }
        }
    }

    private fun exist(table: String): Boolean {
        val parameters = HashMap<String, Any>()
        parameters["db_name"] = "joyce"
        parameters["table_name"] = table
        val list = nativeQueryMapper.find(
            "SELECT * FROM information_schema.TABLES "
                    + "WHERE TABLE_SCHEMA = :db_name AND "
                    + "TABLE_NAME = :table_name", parameters
        )
        return list.isNotEmpty()
    }

    private fun convertCellDataTypeToDBType(type: String?): String {
        return when (type) {
            CellDataType.SELECT_WRAPPER -> DBDataType.BIGINT
            CellDataType.DATETIME -> DBDataType.DATETIME
            else -> DBDataType.TEXT
        }
    }

    private fun createChildTable(
        tableName: String, cellDataList: List<CellData>,
        parentTableName: String
    ) {
        val builder = StringBuilder("CREATE TABLE `$tableName` (`id` bigint NOT NULL, ")
        for (cellData in cellDataList) {
            val dbType = convertCellDataTypeToDBType(cellData.type)
            val columnName = camel2Underscore(cellData.id!!)
            builder.append(String.format("`%s` %s DEFAULT NULL,", columnName, dbType))
        }
        builder.append(String.format("`%s_id` bigint NOT NULL,", parentTableName))
        builder.append(" PRIMARY KEY (`id`));")
        nativeQueryMapper.execute(builder.toString())
    }

    private fun alterChildTable(tableName: String, cellDataList: List<CellData>) {
        for (cellData in cellDataList) {
            val column = camel2Underscore(cellData.id!!)
            if (!exist(tableName, column)) {
                val dbType = convertCellDataTypeToDBType(cellData.type)
                val query = "ALTER TABLE `$tableName` ADD COLUMN `$column` $dbType DEFAULT NULL"
                nativeQueryMapper.execute(query)
            }
        }
    }

    private fun exist(table: String, column: String): Boolean {
        val parameters = HashMap<String, Any>()
        parameters["db_name"] = "joyce"
        parameters["table_name"] = table
        parameters["column_name"] = column
        val list = nativeQueryMapper.find(
            "SELECT * FROM information_schema.COLUMNS "
                    + "WHERE TABLE_SCHEMA = :db_name AND "
                    + "TABLE_NAME = :table_name AND "
                    + "COLUMN_NAME = :column_name", parameters
        )
        return list.isNotEmpty()
    }

    fun deleteContent(id: Long) {
        val rootPath = formPath
        val relativePath = String.format("/%d.json", id)
        val pathname = rootPath + relativePath
        delete(pathname)
    }

    private fun delete(pathname: String) {
        val file = File(pathname)
        if (file.exists()) {
            val delete = file.delete()
            log.info("Delete file $pathname: $delete.")
        }
    }

    fun getCellDataList(formId: Long): List<CellData> {
        val content = readContent(formId)
        return deserializeCellDataList(content)
    }

    fun getFlattedCellDataList(formId: Long): List<CellData> {
        val result = ArrayList<CellData>()
        val list = getCellDataList(formId)
        for (item in list) {
            forEach(item) { e -> result.add(e) }
        }
        return result
    }

    fun getUniqueCellDataList(formId: Long): List<CellData> {
        return getFlattedCellDataList(formId).filter { item -> item.unique != null && item.unique!! }
    }

    fun deleteScript(id: Long) {
        val rootPath = formPath
        val relativePath = String.format("/%d.client.js", id)
        val pathname = rootPath + relativePath
        delete(pathname)
    }

    fun deleteTable(tableName: String, content: String) {
        val cellDataListRef = object : TypeReference<ArrayList<CellData>>() {}
        val cellDataList = JSONUtils.OBJECT_MAPPER.readValue(content, cellDataListRef)
        for (cellData in cellDataList) {
            if (!cellData.isList) {
                continue
            }
            val childTableName = "${tableName}_${cellData.id}"
            nativeQueryMapper.execute("DROP TABLE `$childTableName`;")
        }
        nativeQueryMapper.execute("DROP TABLE `$tableName`;")
    }

    fun forEach(l1: CellData, consumer: Consumer<CellData>) {
        consumer.accept(l1)
        if (l1.isGrid || l1.isTab) {
            for (lane in l1.lanes!!) {
                for (l2 in lane.cellDataList!!) {
                    forEach(l2, consumer)
                }
            }
        }
    }

    fun getImportTemplate(formId: Int): File {
        val file = File.createTempFile("customer-import-template", ".csv")
        val printWriter = PrintWriter(file, "GBK")
        val cellDataList = getFlattedCellDataList(formId.toLong())
        val csvHeaders = cellDataList.filter { !it.isGrid }.map(CellData::label)
        CSVFormat.EXCEL.withFirstRecordAsHeader().print(printWriter).run {
            printRecord(csvHeaders)
            flush()
            close()
        }
        return file
    }

    fun publish(event: String, args: Map<String, Any>) {
        val list = formMapper.findAll()
        for (formDO in list) {
            val eventScript = readEventScript(formDO.id!!)
            createInvocable(eventScript).invokeFunction(event, args)
        }
    }

    fun delete(formId: Long, dataId: Long) {
        val form = formMapper.findById(formId).orElseThrow { GeneralException("表单不存在") }
        nativeQueryMapper.delete(dataId, form.tableName)
        val cellDataList = getFlattedCellDataList(formId)
        for (cellData in cellDataList) {
            if (cellData.isList) {
                val detailTableName = "${form.tableName}_${cellData.id}"
                val detailForeignKey = "${form.tableName}_id"
                val query = "delete from $detailTableName where $detailForeignKey = :fk"
                val parameters = HashMap<String, Any>().apply { this["fk"] = dataId }
                nativeQueryMapper.execute(query, parameters)
            }
        }
    }

    fun delete(id: Long) {
        val form = formMapper.findById(id).orElseThrow { GeneralException("表单不存在") }
        val content = readContent(id)
        deleteTable(form.tableName, content)
        deleteContent(id)
        deleteScript(id)
    }
}