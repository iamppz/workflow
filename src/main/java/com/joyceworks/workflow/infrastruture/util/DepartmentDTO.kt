package com.joyceworks.workflow.infrastruture.util

import lombok.Data
import java.util.*

@Data
class DepartmentDTO {
    private val id: Long? = null
    private val parentId: Long? = null
    private val name: String? = null
    private val leaderId: Long? = null
    private val createdAt: Date? = null
    private val creator: BasicUserDTO? = null
}