package com.ef.umm

class UMR implements Serializable{

    User users
    Role roles
    Microservice microservices

    static constraints = {
        id composite: ['users', 'microservices']
        roles nullable: false
        microservices nullable: false
        users nullable: false
    }

    String toString() {
        return "${users.username} as ${roles.authority} in ${microservices.name}"
    }


    static UMR create(User user, Role role, Microservice microservice, boolean flush = false) {
        def instance = new UMR(users: user, roles: role, microservices: microservice)
        if(!instance.validate()){
            println instance.errors
        }
        instance.save(flush: flush, insert: true)
        instance
    }

}
