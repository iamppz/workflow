package com.joyceworks.api.workflow.domain.vo

import com.joyceworks.api.infrastructure.util.JWTUtil
import com.joyceworks.api.workflow.infrastructure.enums.WorkflowInstanceAction
import java.util.*

class WorkflowInstanceRound {
    var createdAt: Date? = null
    fun addNode(nodeId: Long) {
        nodes.add(WorkflowInstanceRoundNode().apply {
            workflowNodeId = nodeId
            createdAt = Date()
        })
    }

    fun consume(nodeId: Long, message: String?) {
        nodes.removeIf { it.workflowNodeId == nodeId }
        log(WorkflowInstanceAction.PASS.value, nodeId, message)
    }

    fun consumeAll(nodeId: Long?, message: String?) {
        nodes.clear()
        log(WorkflowInstanceAction.REJECT.value, nodeId, message)
    }

    fun log(action: String, nodeId: Long?, message: String?) {
        logs.add(WorkflowInstanceRoundLog().apply {
            this.action = action
            this.workflowNodeId = nodeId
            this.message = message
            this.userId = JWTUtil.getUserId()
        })
    }

    var nodes = mutableListOf<WorkflowInstanceRoundNode>()
    var logs = mutableListOf<WorkflowInstanceRoundLog>()
}