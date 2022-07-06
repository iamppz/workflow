package com.joyceworks.workflow.infrastruture.util

import java.util.*

class UserDTO {
    var id: Long? = null
    var name: String? = null
    var mobile: String? = null
    var password: String? = null
    var createdAt: Date? = null
    var disabled: Boolean? = null
    var roles: List<OptionDTO>? = null
    var department: DepartmentDTO? = null
    var creator: OptionDTO? = null
    var avatar: String? = null
    var bio: String? = null
    var email: String? = null
    var homepage: String? = null
}