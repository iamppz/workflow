package com.joyceworks.workflow.infrastruture.util

class OptionDTO {
    var name: String? = null
    var id: Long? = null

    constructor(name: String?, id: Long?) {
        this.name = name
        this.id = id
    }

    constructor() {}
}