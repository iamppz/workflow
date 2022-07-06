package com.joyceworks.api.workflow.infrastructure.enums;

public enum WorkflowInstanceState {
  UNSUBMIT("unsubmit"),
  PROCESSING("processing"),
  SUCCESS("success"),
  FAILED("failed");
  private String value;

  WorkflowInstanceState(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
