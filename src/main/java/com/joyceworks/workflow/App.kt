package com.joyceworks.workflow

import com.joyceworks.workflow.command.WorkflowInstanceApproveCommand
import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand
import com.joyceworks.workflow.command.service.impl.WorkflowInstanceCommandServiceImpl
import com.joyceworks.workflow.domain.adapter.UserAdapter
import com.joyceworks.workflow.domain.aggregate.Workflow
import com.joyceworks.workflow.domain.aggregate.WorkflowInstance
import com.joyceworks.workflow.domain.repository.WorkflowInstanceRepository
import com.joyceworks.workflow.domain.repository.WorkflowRepository
import com.joyceworks.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.workflow.domain.translator.ApproverTranslator
import com.joyceworks.workflow.infrastruture.util.FormDataUtils
import com.joyceworks.workflow.infrastruture.util.UserService

/**
 * Hello world!
 */
object App {

    @JvmStatic
    fun main(args: Array<String>) {
        val approverTranslator = ApproverTranslator()
        val userService = UserService()
        val userAdapter = UserAdapter(userService, approverTranslator)
        val workflowInstanceRepository = object : WorkflowInstanceRepository {
            override fun save(instance: WorkflowInstance) {
                TODO("Not yet implemented")
            }

            override fun findById(id: Long): WorkflowInstance {
                TODO("Not yet implemented")
            }
        }
        val formDataUtils = FormDataUtils()
        val workflowRepository = object : WorkflowRepository {
            override fun find(id: Long?): Workflow {
                return Workflow()
            }
        }

        val domainService = WorkflowInstanceDomainService(userAdapter)
        val service = WorkflowInstanceCommandServiceImpl(
            workflowInstanceRepository,
            domainService,
            formDataUtils,
            workflowRepository
        )

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