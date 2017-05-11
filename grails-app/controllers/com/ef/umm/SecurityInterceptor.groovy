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

        (microName, controller, action) = authorizationService.extractURI(request.forwardURI)

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

        if(permSuper && authorizationService.hasPermission(user, micro, permSuper)){
            if(microName != "umm"){
                redirect(url: "${micro?.ipAddress}${request?.forwardURI}")
                return true
            }
            return true
        }

        if(permFull && authorizationService.hasPermission(user, micro, permFull)){
            if(microName != "umm"){
                redirect(url: "${micro?.ipAddress}${request?.forwardURI}")
                return true
            }
            return true
        }

        if(permAction && authorizationService.hasPermission(user, micro, permAction)) {
            if(microName != "umm"){
                redirect(url: "${micro?.ipAddress}${request?.forwardURI}")
                return true
            }
            return true
        }

        render resultSet as JSON
        false

    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
