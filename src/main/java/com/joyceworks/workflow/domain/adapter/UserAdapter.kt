package com.joyceworks.api.workflow.domain.adapter

import com.joyceworks.api.domain.service.UserDomainService
import com.joyceworks.api.workflow.domain.translator.ApproverTranslator
import com.joyceworks.api.workflow.domain.vo.Approver
import org.springframework.stereotype.Component

@Component
class UserAdapter(
    private val domainService: UserDomainService,
    private val translator: ApproverTranslator
) {
    fun getApprover(): Approver {
        val user = domainService.queryCurrent()
        return translator.translate(user)
    }
}