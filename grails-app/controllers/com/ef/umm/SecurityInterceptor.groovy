package com.ef.umm

import grails.converters.JSON
import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    def springSecurityService
    def authorizationService
    public SecurityInterceptor(){
        match(uri: "/**")
                .excludes(uri: "/umm/console/**")
                .excludes(uri: "/umm/static/console/**")
    }

    boolean before() {
        def resultSet = [:]
        def user
        def microName
        def controller
        def action
        def micro
        def req = request.forwardURI - "/umm"
        resultSet.put("status", FORBIDDEN)
        resultSet.put("message", "Access forbidden. User not authorized to request this resource.")


        (microName, controller, action) = authorizationService.extractURI(request.forwardURI)
        println("Requested URI is: " + request.forwardURI)
        println("Name of the microservice is: " + microName)
        println("Name of the controller is: " + controller)
        println("Name of the action is: " + action)

        log.debug("Requested URI is: " + request.forwardURI)
        log.debug("Name of the microservice is: " + microName)
        log.debug("Name of the controller is: " + controller)
        log.debug("Name of the action is: " + action)

        micro = Microservice.findByName(microName)

        if(microName != "umm"){
            println("If authorized, request will be forwarded to: ${micro?.ipAddress}${req}")
            log.debug("If authorized, request will be forwarded to: ${micro?.ipAddress}${req}")
        }

        println("Logged in user is " + springSecurityService.getCurrentUser())
        if(!springSecurityService?.principal?.username){
            log.debug("Access denied. Token not provided in the header.")
            resultSet.put("message", "Access denied. Token not provided in the header.")
            response.status = 403
            render resultSet as JSON
            return false
        }

        try{
            user = User.findByUsername(springSecurityService?.principal?.username)
        }catch (Exception ex){
            println ex.getMessage()
        }

        if(!micro){
            log.debug("Microservice: '${microName}' does not exist. Contact system admin.")
            resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")
            response.status = 403
            render resultSet as JSON
            return false
        }

        if(!UMR.findByUsersAndMicroservices(user, micro)){
            log.debug("Access forbidden. User not authorized to request this resource.")
            resultSet.put("message", "Access forbidden. User not authorized to request this resource.")
            response.status = 403
            render resultSet as JSON
            return false
        }

        def permSuper = Permission.findByExpression("*:*")
        def permFull = Permission.findByExpression("${controller}:*")
        def permAction = Permission.findByExpression("${controller}:${action}")

        if(permSuper && authorizationService.hasPermission(user, micro, permSuper)){
            if(microName != "umm"){
                log.debug("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                redirect(url: "${micro?.ipAddress}${req}")
                return true
            }
            log.debug("Successfully Authorized. Forwarding request to: ${req}")
            return true
        }

        if(permFull && authorizationService.hasPermission(user, micro, permFull)){
            if(microName != "umm"){
                log.debug("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                redirect(url: "${micro?.ipAddress}${req}")
                return true
            }
            log.debug("Successfully Authorized. Forwarding request to: ${req}")
            return true
        }

        if(permAction && authorizationService.hasPermission(user, micro, permAction)) {
            if(microName != "umm"){
                log.debug("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                redirect(url: "${micro?.ipAddress}${req}")
                return true
            }
            log.debug("Successfully Authorized. Forwarding request to: ${req}")
            return true
        }

        log.debug("Access forbidden. User is not authorized to request this resource.")
        response.status = 403
        render resultSet as JSON
        false

    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
