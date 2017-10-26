package com.ef.umm

class UMR implements Serializable{

    User users
    Role roles
    Microservice microservices

    static constraints = {
        id composite: ['users', 'microservices', 'roles']
        roles nullable: false
        microservices nullable: false
        users nullable: false
    }

    String toString() {
        return "${users.username} as ${roles.authority} in ${microservices.name}"
    }

    static void removeAll(Microservice m, boolean flush = false) {
        if (m == null) return

        UMR.where { microservices == m }.deleteAll()

        if (flush) { UMR.withSession { it.flush() } }
    }

    static UMR create(User user, Role role, Microservice microservice, boolean flush = false) {
        def instance = new UMR(users: user, roles: role, microservices: microservice)
        instance.save(flush: flush, insert: true)
        instance
    }

}
