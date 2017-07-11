package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile

import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    static transactional = false
    int order = HIGHEST_PRECEDENCE+100
    def springSecurityService
    def authorizationService
    def restService
    public SecurityInterceptor(){
        match(uri: "/**")
                .excludes(uri: "/umm/console/**")
                .excludes(uri: "/umm/static/console/**")
    }

    @Transactional
    boolean before() {
        def resultSet = [:]
        def user, microName, controller
        def action, micro, resp

        try{
            def req = request?.forwardURI - "/umm"
            resultSet.put("status", FORBIDDEN)
            resultSet.put("message", "Access forbidden. User not authorized to request this resource.")

            if(!request?.getHeader("Authorization")){
                log.info("Access denied. Either token not provided in the header or header name is wrong. Header name should be 'Authorization' without quotes.")
                resultSet.put("message", "Access denied. Either token not provided in the header or header name is wrong. Header name should be 'Authorization' without quotes.")
                response.status = 403
                render resultSet as JSON
                return false
            }

            def userName= authorizationService.extractUsername(request?.getHeader("Authorization"))
            (microName, controller, action) = authorizationService.extractURI(request.forwardURI)

            log.info("Name of the microservice is: " + microName)
            log.info("Name of the controller is: " + controller)
            log.info("Name of the action is: " + action)
            log.info("Logged in user is " + userName)
//            log.info("Query translated to JSON: " + queryJson)

//        To test the logging of various levels in the application. Uncomment following
//        lines and each level of logging will invoke corresponding logback configuration.
//
//        try{
//            throw new Exception("Blah Blah")
//        }catch (Exception ex){
//            log.error("manual exception", ex)
//        }
//
//        log.error("...........................Error.............................")
//        log.warn("...........................warn.............................")
//        log.info("...........................info.............................")
//        log.debug("...........................debug.............................")
//        log.trace("...........................trace.............................")

            micro = Microservice.findByName(microName)

            if(microName != "umm"){
                log.info("If authorized, request will be forwarded to: ${micro?.ipAddress}${req}")
            }

            if(!(springSecurityService?.principal?.username || userName)){
                log.info("Access denied. Token not provided in the header.")
                resultSet.put("message", "Access denied. Token not provided in the header.")
                response.status = 403
                render resultSet as JSON
                return false
            }

            try{
                user = User.findByUsername(userName)
                if(!user){
                    new User(username: userName, password: authorizationService.maskIt(), AD: true)
                            .save(flush: true, failOnError: true)
                }
            }catch (Exception ex){
                log.error("Exception occured while retrieving username in the securityInterceptor.", ex)
            }

            if(!micro){
                log.info("Microservice: '${microName}' does not exist. Contact system admin.")
                resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")
                response.status = 403
                render resultSet as JSON
                return false
            }

            if(microName != "umm"){
                resp = restService.callAPI(params, request)
            }

            if(!UMR.findByUsersAndMicroservices(user, micro)){
                log.info("Access forbidden. User not authorized to request this resource.")
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
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        render resp.json as JSON
                    else if(resp.responseEntity.body)
                        render resp.responseEntity.body
                    else
                        render 0

                    return false
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                return true
            }

            if(permFull && authorizationService.hasPermission(user, micro, permFull)){
                if(microName != "umm"){
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        render resp.json as JSON
                    else
                        render resp

                    return false
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                return true
            }

            if(permAction && authorizationService.hasPermission(user, micro, permAction)) {
                if(microName != "umm"){
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        render resp.json as JSON
                    else
                        render resp

                    return false
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                return true
            }

            log.info("Access forbidden. User is not authorized to request this resource.")
            response.status = 403
            render resultSet as JSON
            return false
        }catch(Exception ex){
            log.error("Exception occurred in the interceptor.")
            log.error("Following is the stack trace along with the error message: ", ex)
            return false
        }


    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
