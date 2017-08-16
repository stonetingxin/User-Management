package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import org.apache.commons.codec.binary.Base64

import static org.springframework.http.HttpStatus.FORBIDDEN

@Transactional
class AuthorizationService {

    def springSecurityService
    def restService

    def authIntercept(def request, def params ){
        def resultSet = [:]
        def user, microName, controller
        def action, micro, resp
        def response = [:]
        try{
            def req = request?.forwardURI - "/umm"
            resultSet.put("status", FORBIDDEN)
            resultSet.put("message", "Access forbidden. User not authorized to request this resource.")

            if(!authToken(request)){
                log.info("Access denied. Either token not provided in the header or header name is wrong. Header name should be 'Authorization' without quotes.")
                resultSet.put("message", "Access denied. Token not provided in the header.")
                response.status = 403
                response.resultSet = resultSet
                response.auth = false
                return response
            }

            def userName= extractUsername(authToken(request))
            (microName, controller, action) = extractURI(request.forwardURI)

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

            if(!(getUsernameFromSpring() || userName)){
                log.info("Access denied. Token not provided in the header.")
                resultSet.put("message", "Access denied. Token not provided in the header.")
                response.status = 403
                response.resultSet = resultSet
                response.auth = false
                return response
            }

            try{
                user = User.findByUsername(userName)
                if(!user){
                    new User(username: userName, password: maskIt(), type: "AD")
                            .save(flush: true, failOnError: true)
                }
            }catch (Exception ex){
                log.error("Exception occured while retrieving username in the AuthorizationService.", ex)
            }

            if(!micro){
                log.info("Microservice: '${microName}' does not exist. Contact system admin.")
                resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")
                response.status = 403
                response.resultSet = resultSet
                response.auth = false
                return response
            }

            if(microName != "umm"){
                resp = makeRestCall(params, request)
            }

            if(!UMR.findByUsersAndMicroservices(user, micro)){
                log.info("Access forbidden. User not authorized to request this resource.")
                resultSet.put("message", "Access forbidden. User not authorized to request this resource.")
                response.status = 403
                response.resultSet = resultSet
                response.auth = false
                return response
            }

            def permSuper = Permission.findByExpression("*:*")
            def permFull = Permission.findByExpression("${controller}:*")
            def permAction = Permission.findByExpression("${controller}:${action}")

            if(permSuper && hasPermission(user, micro, permSuper)){
                if(microName != "umm"){
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        response.resultSetJSON= resp.json
                    else if(resp.responseEntity.body)
                        response.resultSetBody = resp.responseEntity.body
                    else
                        response.resultSetZero = 0
                    response.auth = false
                    return response
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                response.auth = true
                return response
            }

            if(permFull && hasPermission(user, micro, permFull)){
                if(microName != "umm"){
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        response.resultSetJSON= resp.json
                    else if(resp.responseEntity.body)
                        response.resultSetBody = resp.responseEntity.body
                    else
                        response.resultSetZero = 0
                    response.auth = false
                    return response
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                response.auth = true
                return response
            }

            if(permAction && hasPermission(user, micro, permAction)) {
                if(microName != "umm"){
                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")
                    log.info("Response is: ${resp.responseEntity.statusCode.value}:${resp.json}")
                    response.status = resp.responseEntity.statusCode.value
                    if(resp.json)
                        response.resultSetJSON= resp.json
                    else if(resp.responseEntity.body)
                        response.resultSetBody = resp.responseEntity.body
                    else
                        response.resultSetZero = 0
                    response.auth = false
                    return response
                }
                log.info("Successfully Authorized. Forwarding request to: ${req}")
                response.auth = true
                return response
            }

            log.info("Access forbidden. User is not authorized to request this resource.")
            response.status = 403
            response.resultSet = resultSet
            response.auth = false
            return response
        }catch(Exception ex){
            log.error("Exception occurred in the AuthorizationService.")
            log.error("Following is the stack trace along with the error message: ", ex)
            response.status = 500
            response.ex= true
            return response
        }
    }

    def makeRestCall(def p, def r){
        return restService.callAPI(p, r)
    }
    def getUsernameFromSpring(){
        return springSecurityService?.principal?.username
    }
    def authToken(def req){
        if(req?.getHeader("Authorization"))
            return req?.getHeader("Authorization")
        else
            return false
    }

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
