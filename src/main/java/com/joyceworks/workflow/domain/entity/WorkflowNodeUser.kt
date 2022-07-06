package com.joyceworks.api.workflow.domain.entity

import com.joyceworks.api.domain.entity.Entity

class WorkflowNodeUser : Entity() {
    var userId: Long? = null

    companion object {
        fun create(id: Long?): WorkflowNodeUser {
            val nodeUser = WorkflowNodeUser()
            nodeUser.userId = id
            return nodeUser
        }
    }
}