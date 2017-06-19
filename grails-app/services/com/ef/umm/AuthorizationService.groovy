package com.ef.umm

import grails.transaction.Transactional
import org.apache.commons.codec.binary.Base64

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
        def microName, controller, action, strippedURI
        strippedURI = uri - "/umm"
        def tokens = strippedURI.tokenize("/")

        if(tokens[0]== 'user' ||tokens[0]== 'microservice' ||tokens[0]== 'role' ||tokens[0]== 'permission'){
            microName = "umm"
            controller = tokens[0]
            action = tokens[1]
        } else {
            microName = tokens[0]
            controller = tokens[1]
            action = tokens[2]
        }

        return [microName, controller, action]
    }

    def extractUsername(def token){
        Base64 coder = new Base64()
        def tok = token - "Bearer "
        def principal = tok.tokenize(".")
        def dec = coder.decode(principal[1])
        def sub = new String(dec)
        def user = sub.tokenize(",")
        def username=user[1].tokenize(":")
        username = username[1].replaceAll("^\"|\"\$" , "");
        return username
    }

    def maskIt(){
        Base64 coder = new Base64()
        def ret = coder.decode("YXZzZHhcenhubSEkJSNtLGJubSxiKiZeJiojJiQjJSlubWJ2R0hWTkIlXiQhQCMmXiojJCFAJG0gbSxraGI=")
        new String(ret)
    }

}
