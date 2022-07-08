package com.joyceworks.workflow.domain.repository

import com.joyceworks.workflow.domain.aggregate.WorkflowInstance

interface WorkflowInstanceRepository {
    fun findByWorkflowId(workflowId: Long): List<WorkflowInstance>
    fun findById(id: Long): WorkflowInstance
    fun save(instance: WorkflowInstance)
}