package com.ef.umm

class Microservice {
    String name
    String ipAddress
    String description

    static hasMany = [roles: Role]
    static constraints = {
        name(maxSize: 20, nullable: false, blank: false, unique: true)
        ipAddress(maxSize: 28, nullable: false, blank: false)
        description(blank: true, maxSize: 1000, nullable: true)
    }

    String toString() {
        return name
    }
}
