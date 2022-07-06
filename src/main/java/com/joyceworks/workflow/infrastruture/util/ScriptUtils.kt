package com.joyceworks.api.infrastructure.util

import java.util.function.Predicate
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

object ScriptUtils {
    private val NASHORN: ScriptEngine = ScriptEngineManager().getEngineByName(
        "nashorn"
    )

    fun test(predicateScript: String, data: Map<String, Any?>): Boolean {
        val predicate =
            NASHORN.eval("new java.util.function.Predicate($predicateScript)") as Predicate<Map<String, Any?>>
        return predicate.test(data)
    }
}