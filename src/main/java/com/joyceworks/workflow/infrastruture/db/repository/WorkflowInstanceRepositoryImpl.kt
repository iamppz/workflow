package com.joyceworks.workflow.infrastruture.db.repository

import com.joyceworks.workflow.domain.aggregate.WorkflowInstance
import com.joyceworks.workflow.domain.repository.WorkflowInstanceRepository

class WorkflowInstanceRepositoryImpl : WorkflowInstanceRepository {

    override fun findByWorkflowId(workflowId: Long): List<WorkflowInstance> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): WorkflowInstance {
        TODO("Not yet implemented")
    }

    override fun save(instance: WorkflowInstance) {
        TODO("Not yet implemented")
    }
}