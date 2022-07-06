package com.joyceworks.workflow.domain.repository

import com.joyceworks.workflow.domain.aggregate.WorkflowInstance

interface WorkflowInstanceRepository {
    fun delete(id: Long)
    fun deleteInBatch(instanceList: List<WorkflowInstance>)
    fun findByWorkflowId(workflowId: Long): List<WorkflowInstance>
    fun save(instance: WorkflowInstance)
    fun findById(id: Long): WorkflowInstance
}