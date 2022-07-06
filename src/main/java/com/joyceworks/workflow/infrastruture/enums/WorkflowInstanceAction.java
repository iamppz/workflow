package com.joyceworks.workflow.infrastruture.enums;

public enum WorkflowInstanceAction {
  PASS("pass"),
  REJECT("reject");
  private String value;

  WorkflowInstanceAction(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
