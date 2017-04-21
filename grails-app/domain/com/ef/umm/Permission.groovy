package com.ef.umm

class Permission {
    String name
    String expression
    String description

    static hasMany = [roles: Role]
    static belongsTo = [Role]
    static constraints = {
        name(maxSize: 20, blank: false)
        expression(blank: false, nullable: false, unique: true)
        description(blank: true, maxSize: 1000, nullable: true)
    }

    String toString() {
        return "Name: ${name}, Expression: ${expression}"
    }
}
