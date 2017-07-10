package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile

import static org.springframework.http.HttpStatus.*

import grails.plugins.rest.client.RestBuilder

class SecurityInterceptor {
    static transactional = false
    int order = HIGHEST_PRECEDENCE+100
    def springSecurityService
    def authorizationService
    public SecurityInterceptor(){
        match(uri: "/**")
                .excludes(uri: "/umm/console/**")
                .excludes(uri: "/umm/static/console/**")
    }

    @Transactional
    boolean before() {
        def resultSet = [:]
        def user, microName, controller, method
        def action, micro, resp
        def queryString = params.collect { k, v -> "$k=$v" }.join(/&/)
        def jsonQuery
        def fileQuery = false
        def rest = new RestBuilder()
        def jsonData = [:]
//        queryString = queryString.replaceAll("\"", "%22")
//        queryString = queryString.replaceAll(/[{]/, "%7B")
//        queryString = queryString.replaceAll(/[}]/, "%7D")
//        queryString = queryString.replaceAll(" ", "+")
        def queryJson = params as JSON
        try{
            def req = request?.forwardURI - "/umm"
            method = request?.method.toLowerCase()
            jsonData = request.getJSON()
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

            log.info("Request data is: " + (params? params : jsonData))
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
                //Todo: Should be improved by adding all corner cases.
                if(params && jsonData){
                    if(action == "save" || action == "create"){
                        jsonData['createdBy'] = userName
                        queryString = queryString + "&createdBy='${userName}'"
                    } else {
                        queryString = queryString + "&updatedBy='${userName}'"
                        jsonData['updatedBy'] = userName
                    }
                    jsonData = jsonData as JSON
                    resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}"){
                        json(jsonData)
                    }
                }else if(params){
                    for(def val: params.values()){
                        try{
                            def parsed = JSON.parse(val)
                            if(parsed){
                                jsonQuery = true
                                break
                            }
                            else{
                                jsonQuery = false
                            }
                        }catch (Exception ex){
                            println ex
                            jsonQuery = false
                        }

                        if(val.class == MultipartFile){
                            fileQuery = true
                        }
                    }

                    if(jsonQuery) {
                        log.info("Sending JSON in the body: " + queryJson)
                        resp = rest."${method}"("${micro?.ipAddress}${req}") {
                            json(queryJson)
                        }
                    }
                    else if(fileQuery){
                        resp = rest."${method}"("${micro?.ipAddress}${req}") {
                            body(params)
                        }
                    }
                    else{
                        if(action == "save" || action == "create"){
                            queryString = queryString + "&createdBy='${userName}'"
                        } else {
                            queryString = queryString + "&updatedBy='${userName}'"
                        }
                        log.info("Sending parameters as Query String: " + queryString)
                        resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}")
                    }
                } else if(jsonData){
                    if(action == "save" || action == "create"){
                        jsonData['createdBy'] = userName
                    } else {
                        jsonData['updatedBy'] = userName
                    }
                    jsonData = jsonData as JSON
                    resp = rest."${method}"("${micro?.ipAddress}${req}") {
                        json(jsonData)
                    }
                } else{
                    resp = rest."${method}"("${micro?.ipAddress}${req}")
                }
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
