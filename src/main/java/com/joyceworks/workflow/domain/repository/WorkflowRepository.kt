package com.joyceworks.workflow.domain.repository

import com.joyceworks.workflow.domain.aggregate.Workflow

interface WorkflowRepository {
    fun find(id: Long?): Workflow
}