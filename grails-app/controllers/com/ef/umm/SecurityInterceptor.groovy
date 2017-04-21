package com.ef.umm

import grails.converters.JSON
import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    def springSecurityService

    private SecurityInterceptor(){
        match(controller: 'user')
        match(controller: 'microservice')
        match(controller: 'role')
    }

    boolean before() {
        def user = User.findByUsername(springSecurityService.principal.username)
        def micro = Microservice.findByName('UMM')
        def umr = UMR.findAllByUsersAndMicroservices(user, micro)
        def role = umr.roles.authority

        if(role[0]!= 'ROLE_ADMIN'){
            def resultSet = [:]
            resultSet.put("status", FORBIDDEN)
            resultSet.put("message", "Access forbidden. User not authorized.")
            response.status = 403
            render resultSet as JSON
            return false
        }
//        switch (actionName){
//            case 'list':
//                if(role[0] != 'ROLE_ADMIN')
//                    return false
//                break
//            case 'show':
//                if(role[0] != 'ROLE_ADMIN')
//                    return false
//                break
//            case 'create':
//                if(role[0] != 'ROLE_ADMIN')
//                    return false
//                break
//            case 'delete':
//                if(role[0] != 'ROLE_ADMIN')
//                    return false
//                break
//            case 'update':
//                if(role[0] != 'ROLE_ADMIN')
//                    return false
//                break
//            default: return false
//        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
