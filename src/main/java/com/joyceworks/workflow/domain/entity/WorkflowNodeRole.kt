package com.joyceworks.workflow.domain.entity

class WorkflowNodeRole : Entity() {
    var roleId: Long? = null

    companion object {
        fun create(id: Long): WorkflowNodeRole {
            val result = WorkflowNodeRole()
            result.roleId = id
            return result
        }
    }
}