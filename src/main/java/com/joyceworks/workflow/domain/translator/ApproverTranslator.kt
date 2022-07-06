package com.joyceworks.workflow.domain.translator

import com.joyceworks.workflow.domain.vo.Approver
import com.joyceworks.workflow.infrastruture.util.UserDTO

class ApproverTranslator {
    fun translate(user: UserDTO): Approver {
        return Approver(
            user.id!!,
            user.department!!.id!!,
            user.department!!.leaderId == user.id,
            user.roles!!.map { it.id!! }
        )
    }
}