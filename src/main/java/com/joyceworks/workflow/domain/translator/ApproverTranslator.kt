package com.joyceworks.api.workflow.domain.translator

import com.joyceworks.api.domain.aggregate.User
import com.joyceworks.api.workflow.domain.vo.Approver
import org.springframework.stereotype.Component

@Component
class ApproverTranslator {
    fun translate(user: User): Approver {
        return Approver(
            user.id!!,
            user.department!!.id!!,
            user.department!!.leaderId == user.id,
            user.roles!!.map { it.id!! }
        )
    }
}