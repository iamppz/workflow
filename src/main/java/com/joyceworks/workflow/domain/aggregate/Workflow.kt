package com.joyceworks.workflow.domain.aggregate

import com.joyceworks.workflow.domain.entity.WorkflowNode
import com.joyceworks.workflow.domain.entity.WorkflowTransition
import com.joyceworks.workflow.infrastruture.enums.WorkflowNodeState
import com.joyceworks.workflow.infrastruture.util.ScriptUtils
import org.apache.commons.lang3.StringUtils

class Workflow : AggregateRoot() {
    var name: String? = null
    var type: String? = null
    var formId: Long? = null
    var nodes = mutableListOf<WorkflowNode>()
    fun update(name: String?) {
        this.name = name
    }

    companion object {
        @JvmStatic
        fun create(name: String?, formId: Long?): Workflow {
            return Workflow().apply {
                this.type = "workflow"
                this.name = name
                this.formId = formId
            }
        }
    }

    private fun getNewestVersion(): Long {
        return nodes.maxOf { it.version!! }
    }

    fun getStartNode(): WorkflowNode {
        val version = getNewestVersion()
        return nodes.find { it.type == WorkflowNodeState.START.value && it.version == version }!!
    }

    fun isLast(nodeId: Long): Boolean {
        return (nodes.find { it.id == nodeId })!!.type == WorkflowNodeState.END.value
    }

    fun reachable(source: Long, target: Long): Boolean {
        val routes = nodes.map { it.transitions }.reduce { acc, list -> acc + list }
        val allPaths: MutableList<List<Long>> = ArrayList()
        for (route in routes.filter { it.source == source }) {
            val paths: MutableList<List<Long>> = ArrayList()
            val path: MutableList<Long> = ArrayList()
            paths.add(path)
            path.add(source)
            pathing(paths, path, route.destination!!, routes)
            allPaths.addAll(paths)
        }
        return allPaths.any { it.contains(target) }
    }

    fun match(from: Long, data: Map<String, Any?>): List<WorkflowTransition> {
        val transitions = nodes.filter { it.id == from }.map { it.transitions }
            .reduce { acc, list -> acc + list }
        return transitions.filter {
            StringUtils.isBlank(it.expression) || ScriptUtils.test(it.expression!!, data)
        }
    }

    private fun pathing(
        allPaths: MutableList<List<Long>>,
        currentPath: MutableList<Long>,
        destination: Long,
        routes: List<WorkflowTransition>
    ) {
        currentPath.add(destination)
        val mirrorOfCurrentPath = ArrayList(currentPath)
        val nextRoutes = routes.filter { it.source == destination }
        for (i in nextRoutes.indices) {
            val nextRoute = nextRoutes[i]
            if (i == 0) {
                pathing(allPaths, currentPath, nextRoute.destination!!, routes)
            } else {
                val newPath = ArrayList(mirrorOfCurrentPath)
                allPaths.add(newPath)
                pathing(allPaths, newPath, nextRoute.destination!!, routes)
            }
        }
    }
}