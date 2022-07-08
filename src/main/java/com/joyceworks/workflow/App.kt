package com.joyceworks.workflow

import com.joyceworks.workflow.command.WorkflowInstanceApproveCommand
import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand
import com.joyceworks.workflow.command.service.WorkflowInstanceCommandService
import com.joyceworks.workflow.command.service.impl.WorkflowInstanceCommandServiceImpl
import com.joyceworks.workflow.domain.repository.WorkflowInstanceRepository
import com.joyceworks.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.workflow.infrastruture.db.repository.WorkflowInstanceRepositoryImpl
import com.joyceworks.workflow.infrastruture.db.repository.WorkflowRepository
import com.joyceworks.workflow.infrastruture.util.FormDataUtils

/**
 * Hello world!
 */
object App {
    private val repository = WorkflowRepository()
    private val instanceRepository: WorkflowInstanceRepository = WorkflowInstanceRepositoryImpl()
    private val domainService = WorkflowInstanceDomainService()
    private val formDataUtils = FormDataUtils()
    private val service: WorkflowInstanceCommandService = WorkflowInstanceCommandServiceImpl(
        instanceRepository,
        domainService,
        formDataUtils,
        repository
    )

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello World!")
        val command = WorkflowInstanceCreateCommand()
        command.workflowId = 1L
        command.dataId = 1L
        command.attachmentId = "1,2"
        val id = service.create(command)
        service.submit(id)
        service.cancelSubmit(id)
        service.submit(id)
        val rejectCommand = WorkflowInstanceApproveCommand()
        rejectCommand.id = id
        rejectCommand.message = "No message."
        service.reject(rejectCommand)
        service.submit(id)
        val approveCommand = WorkflowInstanceApproveCommand()
        approveCommand.id = id
        approveCommand.message = "No message."
        service.pass(approveCommand)
    }
}