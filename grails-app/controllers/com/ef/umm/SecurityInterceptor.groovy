package com.ef.umm

import grails.converters.JSON
import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    def springSecurityService
    def authorizationService
    public SecurityInterceptor(){
        match(uri: "/**")
                .excludes(uri: "/console/**")
                .excludes(uri: "/static/console/**")
    }

    boolean before() {
        def resultSet = [:]
        def user
        def microName
        def controller
        def action
        resultSet.put("status", FORBIDDEN)
        resultSet.put("message", "Access forbidden. User not authorized.")
        response.status = 403

        def uri = request.forwardURI
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
        println microName
        println controller
        println action
        if(!springSecurityService?.principal?.username){
            resultSet.put("message", "Access denied. Token not provided in the header.")
            render resultSet as JSON
            return false
        }

        try{
            user = User.findByUsername(springSecurityService?.principal?.username)
        }catch (Exception ex){
            println ex.getMessage()
        }

        def micro = Microservice.findByName(microName)

        if(!micro){
            resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")
            render resultSet as JSON
            return false
        }

        if(!UMR.findByUsersAndMicroservices(user, micro)){
            resultSet.put("message", "Access forbidden. User not authorized.")
            render resultSet as JSON
            return false
        }

        def permSuper = Permission.findByExpression("*:*")
        def permFull = Permission.findByExpression("${controller}:*")
        def permAction = Permission.findByExpression("${controller}:${action}")

        if(permSuper && authorizationService.hasRole(user, micro, permSuper)){
            return true
        }

        if(permFull && authorizationService.hasPermission(user, micro, permFull)){
            return true
        }

        if(permAction && authorizationService.hasPermission(user, micro, permAction)) {
            return true
        }

        false

//        switch (actionName){
//            case 'list':
//                if(!authorizationService.hasRole(user, micro, role)) {
//                    render resultSet as JSON
//                    return false
//                }
//                break
//            case 'show':
//                def userId = params?.id as Long
//                def permSuper = Permission.findByExpression("*:*")
//                def permUser = Permission.findByExpression("user:show:${userId}")
//                def permFull = Permission.findByExpression("user:show:*")
//
//                if(authorizationService.hasPermission(user, micro, permSuper))
//                    return true
//
//                if(authorizationService.hasPermission(user, micro, permFull))
//                    return true
//
//                if(authorizationService.hasPermission(user, micro, permUser))
//                    return true
//
//                render resultSet as JSON
//                return false
//                break
//            case 'create':
//                if(!authorizationService.hasRole(user, micro, role)){
//                    render resultSet as JSON
//                    return false
//                }
//                break
//            case 'delete':
//                if(!authorizationService.hasRole(user, micro, role)){
//                    render resultSet as JSON
//                    return false
//                }
//                break
//            case 'update':
//                def jsonObject = request.getJSON()
//                def userId = jsonObject?.id as Long
//                def permSuper = Permission.findByExpression("*:*")
//                def permUser = Permission.findByExpression("user:update:${userId}")
//                def permFull = Permission.findByExpression("user:update:*")
//
//                if(authorizationService.hasPermission(user, micro, permSuper))
//                    return true
//
//                if(authorizationService.hasPermission(user, micro, permFull))
//                    return true
//
//                if(authorizationService.hasPermission(user, micro, permUser))
//                    return true
//
//                render resultSet as JSON
//                return false
//                break
//            case 'resetPassword':
//                if(!authorizationService.hasRole(user, micro, role)){
//                    render resultSet as JSON
//                    return false
//                }
//                break
//            case 'updatePassword':
//                def jsonObject = request.getJSON()
//                def userId = jsonObject?.id as Long
//                def permUser = Permission.findByExpression("user:updatePassword:${userId}")
//
//                if(authorizationService.hasPermission(user, micro, permUser))
//                    return true
//
//                render resultSet as JSON
//                return false
//                break
//            case 'addRevokeMicroserviceRoles':
//                if(!authorizationService.hasRole(user, micro, role)){
//                    render resultSet as JSON
//                    return false
//                }
//                break
//            default:
//                render resultSet as JSON
//                return false
//
//        }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
