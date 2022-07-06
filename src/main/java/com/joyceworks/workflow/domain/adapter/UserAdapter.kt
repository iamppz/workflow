package com.joyceworks.workflow.domain.adapter

import com.joyceworks.workflow.domain.translator.ApproverTranslator
import com.joyceworks.workflow.domain.vo.Approver
import com.joyceworks.workflow.infrastruture.util.UserService

class UserAdapter(
    private val domainService: UserService,
    private val translator: ApproverTranslator
) {
    fun getApprover(): Approver {
        val user = domainService.queryCurrent()
        return translator.translate(user)
    }
}