package com.joyceworks.api.workflow.domain.entity

import com.joyceworks.api.domain.entity.Entity

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