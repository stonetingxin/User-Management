package com.ef.umm

class Microservice {
    String name
    String ipAddress
    String description

    static hasMany = [permissions: Permission]
    static constraints = {
        name(maxSize: 20, nullable: false, blank: false, unique: true)
        ipAddress(blank: false, unique: true, matches: /^(http:\/\/|https:\/\/)((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?):[0-9]{1,5}$/)
        description(blank: true, maxSize: 1000, nullable: true)
    }

    String toString() {
        return name
    }
}
