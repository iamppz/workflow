package com.joyceworks.api.workflow.infrastructure.event

class WorkflowInstanceForwardEvent(var id: Long, var nodeIds: List<Long>)