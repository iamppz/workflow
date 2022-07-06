package com.joyceworks.api.workflow.infrastructure.enums;

public enum WorkflowNodeState {
  START("start"),
  END("end"),
  OPERATION("operation");
  private String value;

  WorkflowNodeState(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
