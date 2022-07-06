package com.joyceworks.workflow.domain.vo

import java.util.*

class WorkflowInstanceRoundLog {
    var createdAt: Date = Date()
    var workflowNodeId: Long? = null
    var message: String? = null
    var action: String? = null
    var userId: Long? = null
}