package com.joyceworks.workflow.domain.entity

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