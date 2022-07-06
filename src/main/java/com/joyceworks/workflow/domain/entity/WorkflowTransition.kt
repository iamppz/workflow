package com.joyceworks.workflow.domain.entity

class WorkflowTransition : Entity() {
    var name: String? = null
    var source: Long? = null
    var destination: Long? = null
    var expression: String? = null
    var sourcePosition: String? = null
    var destinationPosition: String? = null
    var type: String? = null
}