package com.joyceworks.workflow.domain.service

import com.joyceworks.workflow.domain.adapter.UserAdapter
import com.joyceworks.workflow.domain.vo.Approver

class WorkflowInstanceDomainService(
    private val userAdapter: UserAdapter,
) {
    fun getApprover(): Approver {
        return userAdapter.getApprover()
    }
}