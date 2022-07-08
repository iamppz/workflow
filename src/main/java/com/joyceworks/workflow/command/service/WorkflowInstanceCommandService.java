package com.joyceworks.workflow.command.service;

import com.joyceworks.workflow.command.WorkflowInstanceApproveCommand;
import com.joyceworks.workflow.command.WorkflowInstanceCreateCommand;

public interface WorkflowInstanceCommandService {

  Long create(WorkflowInstanceCreateCommand command);

  void pass(WorkflowInstanceApproveCommand command);

  void submit(Long processInstanceId);

  void cancelSubmit(Long id);

  void reject(WorkflowInstanceApproveCommand command);
}
