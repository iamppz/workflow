package com.joyceworks.api.workflow.domain.entity

import com.joyceworks.api.domain.entity.Entity

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