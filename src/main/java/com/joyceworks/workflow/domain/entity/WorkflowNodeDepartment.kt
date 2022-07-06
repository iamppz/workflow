package com.joyceworks.workflow.domain.entity

class WorkflowNodeDepartment : Entity() {
    var departmentId: Long? = null

    companion object {
        fun create(id: Long): WorkflowNodeDepartment {
            val result = WorkflowNodeDepartment()
            result.departmentId = id
            return result
        }
    }
}