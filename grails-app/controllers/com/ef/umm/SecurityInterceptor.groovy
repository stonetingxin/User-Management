package com.ef.umm

import grails.converters.JSON
import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    def springSecurityService
    def authorizationService
    private SecurityInterceptor(){
        match(controller: 'user')
    }

    boolean before() {
        def resultSet = [:]
        resultSet.put("status", FORBIDDEN)
        resultSet.put("message", "Access forbidden. User not authorized.")
        response.status = 403

        def user = User.findByUsername(springSecurityService.principal.username)
        def micro = Microservice.findByName('UMM')
        def role = Role.findById("1")

        switch (actionName){
            case 'list':
                if(!authorizationService.hasRole(user, micro, role)) {
                    render resultSet as JSON
                    return false
                }
                break
            case 'show':
                def userId = params?.id as Long
                def permSuper = Permission.findByExpression("*:*")
                def permUser = Permission.findByExpression("user:show:${userId}")
                def permFull = Permission.findByExpression("user:show:*")

                if(authorizationService.hasPermission(user, micro, permSuper))
                    return true

                if(authorizationService.hasPermission(user, micro, permFull))
                    return true

                if(authorizationService.hasPermission(user, micro, permUser))
                    return true

                render resultSet as JSON
                return false
                break
            case 'create':
                if(!authorizationService.hasRole(user, micro, role)){
                    render resultSet as JSON
                    return false
                }
                break
            case 'delete':
                if(!authorizationService.hasRole(user, micro, role)){
                    render resultSet as JSON
                    return false
                }
                break
            case 'update':
                def jsonObject = request.getJSON()
                def userId = jsonObject?.id as Long
                def permSuper = Permission.findByExpression("*:*")
                def permUser = Permission.findByExpression("user:update:${userId}")
                def permFull = Permission.findByExpression("user:update:*")

                if(authorizationService.hasPermission(user, micro, permSuper))
                    return true

                if(authorizationService.hasPermission(user, micro, permFull))
                    return true

                if(authorizationService.hasPermission(user, micro, permUser))
                    return true

                render resultSet as JSON
                return false
                break
            case 'resetPassword':
                if(!authorizationService.hasRole(user, micro, role)){
                    render resultSet as JSON
                    return false
                }
                break
            case 'updatePassword':
                def jsonObject = request.getJSON()
                def userId = jsonObject?.id as Long
                def permUser = Permission.findByExpression("user:updatePassword:${userId}")

                if(authorizationService.hasPermission(user, micro, permUser))
                    return true

                render resultSet as JSON
                return false
                break
            case 'addRevokeMicroserviceRoles':
                if(!authorizationService.hasRole(user, micro, role)){
                    render resultSet as JSON
                    return false
                }
                break
            default:
                render resultSet as JSON
                return false

        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
