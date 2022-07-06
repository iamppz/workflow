package com.joyceworks.api.workflow.domain.entity

import com.joyceworks.api.domain.entity.Entity
import com.joyceworks.api.infrastructure.GeneralException
import com.joyceworks.api.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.api.workflow.infrastructure.enums.WorkflowNodeState

class WorkflowNode : Entity() {
    var name: String? = null
    var workflowId: Long? = null
    var type: String? = null
    var approveMethod: Int? = null
    var editableFields: String? = null
    var transitions: List<WorkflowTransition> = mutableListOf()
    var users: List<WorkflowNodeUser> = mutableListOf()
    var ccUsers: List<WorkflowNodeCcUser> = mutableListOf()
    var roles: List<WorkflowNodeRole> = mutableListOf()
    var departments: List<WorkflowNodeDepartment> = mutableListOf()
    var version: Long? = null

    fun canApprove(domainService: WorkflowInstanceDomainService): Boolean {
        val approver = domainService.getApprover()
        return when (approveMethod) {
            1 -> users.any { it.userId == approver.id }
            2 -> {
                val approvableRoles = roles.map { it.roleId }
                approver.roles.any { approvableRoles.contains(it) }
            }
            3 -> departments.any { it.departmentId == approver.department && approver.isLeader }
            else -> throw GeneralException("不支持的审核方式: $approveMethod")
        }
    }

    fun canForward(domainService: WorkflowInstanceDomainService): Boolean {
        return type == WorkflowNodeState.START.value || canApprove(domainService)
    }
}