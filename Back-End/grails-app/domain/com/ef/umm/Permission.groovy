package com.ef.umm

class Permission {
    String name
    String expression
    String description

    static hasOne = [micro: Microservice]
    static hasMany = [roles: Role, preReqs: String]
    static belongsTo = [Role]
    static constraints = {
        name(maxSize: 50, blank: false)
        expression(blank: false, nullable: false, unique: true)
        description(blank: true, maxSize: 1000, nullable: true)
        preReqs nullable: true, blank:true
    }

    String toString() {
        return "Name: ${name}, Expression: ${expression}"
    }
}
