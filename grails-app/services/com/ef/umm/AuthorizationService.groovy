package com.ef.umm

import grails.transaction.Transactional

@Transactional
class AuthorizationService {

    def hasRole(User user, Microservice micro, Role role) {
        def umr = UMR.findAllByUsersAndMicroservices(user, micro)
        if(umr*.roles.contains(role)){
            return true
        }
        else
            return false

    }

    def hasPermission(User user, Microservice micro, Permission perm){
        UMR umr = UMR.findAllByUsers(user)
        if(umr*.microservices.contains(micro) && umr*.roles*.permissions.contains(perm)){
            return true
        }
        else
            return false
    }
}
