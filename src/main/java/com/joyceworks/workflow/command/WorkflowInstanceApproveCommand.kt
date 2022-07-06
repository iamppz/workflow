package com.joyceworks.api.workflow.application.command

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

class WorkflowInstanceApproveCommand {
    @field:NotNull(message = "ID 不能为空")
    @field:Min(value = 1L, message = "ID 不合法")
    var id: Long? = null
    var message: String? = null
}