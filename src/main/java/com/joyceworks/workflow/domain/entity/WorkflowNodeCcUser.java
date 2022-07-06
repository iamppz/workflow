package com.joyceworks.api.workflow.domain.entity;

import com.joyceworks.api.domain.entity.Entity;

public class WorkflowNodeCcUser extends Entity {

  private Long userId;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }
}
