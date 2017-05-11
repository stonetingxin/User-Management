package com.ef.umm

import grails.transaction.Transactional

@Transactional
class AuthorizationService {

    def hasRole(User user, Microservice micro, Role role) {
        if(!role){
            return false
        }
        def umr = UMR.findAllByUsersAndMicroservices(user, micro)

        if(!umr){
            return false
        }
        def roles = umr*.roles
        return roles.contains(role)
    }

    def hasPermission(User user, Microservice micro, Permission perm){
        if(!perm){
            return false
        }

        def umr = UMR.findAllByUsersAndMicroservices(user, micro)

        if(!umr){
            return false
        }

        def perms = umr*.roles*.permissions
        def truePerm = perms*.contains(perm)
        return truePerm.contains(true)
    }

    def extractURI(def uri){
        def microName, controller, action
        def tokens = uri.tokenize("/")
        if(tokens.size()>=3){
            microName = tokens[0]
            controller = tokens[1]
            action = tokens[2]
        }
        if(tokens.size()==2){
            microName = "umm"
            controller = tokens[0]
            action = tokens[1]
        }

        return [microName, controller, action]
    }
}
