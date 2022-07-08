package com.joyceworks.workflow.command.service.impl

import com.joyceworks.workflow.command.WorkflowInstanceApproveCommand
import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand
import com.joyceworks.workflow.command.service.WorkflowInstanceCommandService
import com.joyceworks.workflow.domain.aggregate.WorkflowInstance
import com.joyceworks.workflow.domain.repository.WorkflowInstanceRepository
import com.joyceworks.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.workflow.infrastruture.db.repository.WorkflowRepository
import com.joyceworks.workflow.infrastruture.util.FormDataUtils

class WorkflowInstanceCommandServiceImpl(
    private val repository: WorkflowInstanceRepository,
    private val domainService: WorkflowInstanceDomainService,
    private val formDataUtils: FormDataUtils,
    private val workflowRepository: WorkflowRepository
) : WorkflowInstanceCommandService {

    override fun create(command: WorkflowInstanceCreateCommand): Long {
        val instance = WorkflowInstance.create(command)
        repository.save(instance)
        return instance.id!!
    }

    override fun pass(command: WorkflowInstanceApproveCommand) {
        val instance = repository.findById(command.id!!)
        val workflow = workflowRepository.find(instance.workflowId)
        instance.forward(command.message, workflow, domainService, formDataUtils)
    }

    /**
     * 提交流程
     *
     * @param id 流程实例ID
     */
    override fun submit(id: Long) {
        val instance = repository.findById(id)
        val workflow = workflowRepository.find(instance.workflowId)
        instance.submit(workflow, domainService, formDataUtils)
    }

    override fun cancelSubmit(id: Long) {
        val instance = repository.findById(id)
        instance.cancelSubmit()
    }

    override fun reject(command: WorkflowInstanceApproveCommand) {
        val instance = repository.findById(command.id!!)
        val workflow = workflowRepository.find(instance.workflowId)
        instance.backward(workflow, command.message, domainService)
    }
}