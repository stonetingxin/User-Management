package com.ef.umm

import grails.transaction.Transactional

@Transactional
class AuthorizationService {

    def hasRole(User user, Microservice micro, Role role) {
        def umr = UMR.findAllByUsersAndMicroservices(user, micro)
        def roles = umr*.roles
        return roles.contains(role)
    }

    def hasPermission(User user, Microservice micro, Permission perm){
        def umr = UMR.findAllByUsersAndMicroservices(user, micro)
        def perms = umr*.roles*.permissions
        def truePerm = perms*.contains(perm)
        return truePerm.contains(true)
    }

}
