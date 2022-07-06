package com.joyceworks.api.workflow.domain.service

import com.joyceworks.api.workflow.domain.adapter.UserAdapter
import com.joyceworks.api.workflow.domain.vo.Approver
import org.springframework.stereotype.Service

@Service
class WorkflowInstanceDomainService(
    private val userAdapter: UserAdapter,
) {
    fun getApprover(): Approver {
        return userAdapter.getApprover()
    }
}