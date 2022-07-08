package com.joyceworks.workflow.domain.aggregate

import com.joyceworks.workflow.domain.entity.WorkflowNode
import com.joyceworks.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.workflow.domain.vo.WorkflowInstanceRound
import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand
import com.joyceworks.workflow.infrastruture.GeneralException
import com.joyceworks.workflow.infrastruture.enums.WorkflowInstanceState
import com.joyceworks.workflow.infrastruture.util.FormDataUtils
import com.joyceworks.workflow.infrastruture.util.JWTUtil
import com.joyceworks.workflow.infrastruture.util.SnowFlake
import java.util.*

class WorkflowInstance : AggregateRoot() {
    var workflowId: Long? = null
    var dataId: Long? = null
    var rounds = mutableListOf<WorkflowInstanceRound>()
    var state: String? = null
    var creator: Long? = null
    var attachmentId: String? = null
    var lastUpdatedAt: Date? = null
    var createdAt: Date? = null

    fun cancelSubmit() {
        if (!isCreator()) {
            throw GeneralException("Only creator can cancel submit")
        }

        if (!isProcessing()) {
            throw GeneralException("Workflow is not in INPROCESS state")
        }

        if (hasApproved()) {
            throw GeneralException("Workflow is already approved")
        }

        state = WorkflowInstanceState.UNSUBMIT.value
        getPendingRound()!!.consumeAll(null, "撤回提交")
    }

    fun backward(
        workflow: Workflow,
        message: String?,
        domainService: WorkflowInstanceDomainService
    ) {
        if (!isProcessing()) {
            throw GeneralException("Workflow instance is not in PROCESSING state")
        }

        val node = getPendingNodes(workflow).firstOrNull { it.canApprove(domainService) }
            ?: throw GeneralException("No permission to approve")

        state = WorkflowInstanceState.UNSUBMIT.value
        getPendingRound()!!.consumeAll(node.id, message)
    }

    fun submit(
        workflow: Workflow,
        domainService: WorkflowInstanceDomainService,
        formDataUtils: FormDataUtils
    ): List<Long> {
        if (state != WorkflowInstanceState.UNSUBMIT.value) {
            throw GeneralException("Workflow instance is not in UNSUBMIT state")
        }

        val start = workflow.getStartNode()
        state = WorkflowInstanceState.PROCESSING.value
        rounds.add(WorkflowInstanceRound().apply {
            addNode(start.id!!)
            createdAt = Date()
        })

        return forward("Submit", workflow, domainService, formDataUtils)
    }

    fun forward(
        message: String?,
        workflow: Workflow,
        domainService: WorkflowInstanceDomainService,
        formDataUtils: FormDataUtils
    ): List<Long> {
        if (state != WorkflowInstanceState.PROCESSING.value) {
            throw GeneralException("Workflow instance is not in PROCESSING state")
        }

        val data = formDataUtils.get(workflow.formId!!, dataId!!)
        val pendingNodes = getPendingNodes(workflow)
        val processableNodes = pendingNodes.filter { it.canForward(domainService) }.map { it.id!! }
        val unprocessableNodes =
            pendingNodes.filter { !it.canForward(domainService) }.map { it.id!! }
        val pendingRound = getPendingRound()!!

        for (i in processableNodes.indices) {
            val restProcessableNodes = processableNodes.subList(i + 1, processableNodes.size)
            val nodeId = processableNodes[i]

            pendingRound.consume(nodeId, message)

            // Add next nodes
            val nextNodes = workflow.match(nodeId, data).map { it.destination!! }
            for (j in nextNodes.indices) {
                val node = nextNodes[j]
                val rest = nextNodes.subList(j + 1, nextNodes.size)
                val restAll = restProcessableNodes + unprocessableNodes + rest
                if (restAll.all { !workflow.reachable(it, node) }) {
                    pendingRound.addNode(node)
                }
            }
        }

        if (pendingRound.nodes.any { workflow.isLast(it.workflowNodeId!!) }) {
            state = WorkflowInstanceState.SUCCESS.value
        }

        return processableNodes
    }

    private fun hasApproved(): Boolean {
        val pendingRound = getPendingRound() ?: return false
        return pendingRound.logs.size > 1
    }

    private fun isCreator() = creator!! == JWTUtil.getUserId()

    private fun isProcessing() = state == WorkflowInstanceState.PROCESSING.value

    private fun getPendingRound() = rounds.maxByOrNull { it.createdAt!! }

    private fun getPendingNodes(workflow: Workflow): List<WorkflowNode> {
        val pendingRound = getPendingRound() ?: return emptyList()
        return pendingRound.nodes.map { workflow.nodes.find { node -> node.id == it.workflowNodeId }!! }
    }

    companion object {
        @JvmStatic
        fun create(command: WorkflowInstanceCreateCommand): WorkflowInstance =
            WorkflowInstance().apply {
                attachmentId = command.attachmentId
                state = WorkflowInstanceState.UNSUBMIT.value
                dataId = command.dataId
                workflowId = command.workflowId
                creator = JWTUtil.getUserId()
                createdAt = Date()
            }
    }
}