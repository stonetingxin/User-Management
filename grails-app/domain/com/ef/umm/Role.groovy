package com.ef.umm

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes='authority')
@ToString(includes='authority', includeNames=true, includePackage=false)
class Role {
    String authority
    String description

    static belongsTo = [Microservice]
    static hasMany = [permissions:Permission, microservices: Microservice]
    static constraints = {
        authority(maxSize: 20, nullable: false, blank: false, unique: true)
        description(blank: true, maxSize: 1000, nullable: true)
    }

    static mapping = {
        cache true
    }

}
