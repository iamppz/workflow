package com.joyceworks.workflow.command.service

import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand
import com.joyceworks.workflow.command.service.impl.WorkflowInstanceCommandServiceImpl
import com.joyceworks.workflow.domain.repository.WorkflowInstanceRepository
import com.joyceworks.workflow.domain.service.WorkflowInstanceDomainService
import com.joyceworks.workflow.infrastruture.db.repository.WorkflowRepository
import com.joyceworks.workflow.infrastruture.util.FormDataUtils
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class WorkflowInstanceCommandServiceTest {
    @Test
    fun test() {
        val instanceRepository = Mockito.mock(WorkflowInstanceRepository::class.java)
        val domainService = Mockito.mock(WorkflowInstanceDomainService::class.java)
        val formDataUtils = Mockito.mock(FormDataUtils::class.java)
        val repository = Mockito.mock(WorkflowRepository::class.java)
        val service = WorkflowInstanceCommandServiceImpl(
            instanceRepository,
            domainService,
            formDataUtils,
            repository
        )
        service.create(WorkflowInstanceCreateCommand().apply {
            dataId = 1L
            workflowId = 1L
            attachmentId = "1, 2"
        })
        println("Hi!")
    }
}