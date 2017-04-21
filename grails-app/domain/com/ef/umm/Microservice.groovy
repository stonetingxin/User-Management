package com.ef.umm

class Microservice {
    String name
    String description

    static hasMany = [roles: Role]
    static constraints = {
        name(maxSize: 20, nullable: false, blank: false, unique: true)
        description(blank: true, maxSize: 1000, nullable: true)
    }

    String toString() {
        return name
    }
}
