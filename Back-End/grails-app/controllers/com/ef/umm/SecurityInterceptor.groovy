package com.ef.umm

import com.ef.apps.licensing.JniWrapper
import grails.converters.JSON
import grails.transaction.Transactional

import org.grails.web.util.WebUtils
import static org.springframework.http.HttpStatus.*

class SecurityInterceptor {
    static transactional = false
    int order = HIGHEST_PRECEDENCE+100
    def authorizationService
    def licensingService

    public SecurityInterceptor(){
        match(uri: "/**")
                .excludes(uri: "/umm/console/**")
                .excludes(uri: "/umm/static/console/**")
                .excludes(uri: "/umm/base/**")
                .excludes(uri: "/umm/monitoring/**")
                .excludes(uri: "/umm/monitoring")
                .excludes(uri: "/umm/user/logout")
                .excludes(uri: "/umm/")

    }

    @Transactional
    boolean before() {
        def resp = [:]

        def validity = licensingService.validateLicense()

        if(     request.forwardURI != "/umm/license/index" &&
                request.forwardURI != "/umm/license/save" &&
                request.forwardURI != "/umm/license/validate"){

            if(validity?.license == "valid" || validity?.license == "supportExpired"){
                if (validity?.licStatus == "trial") {
                    if(validity?.license == "expired"){
                        response.status = 400
                        render validity as JSON
                        return false
                    }
                }
            }else{
                response.status = 400
                render validity as JSON
                return false
            }
        }

        resp<< validity
        resp<< licensingService?.getAttribs()
        resp << authorizationService.authIntercept(request, params)

        // In case of exception, return status with termination of execution
        if(resp?.ex){
            response.status = resp?.status
            return false
        }

        // In case of authorization debounce, terminate.
        if(resp.status == 403){
            response.status = resp?.status
            render resp?.resultSet as JSON
            return resp?.auth
        }

        if(resp.auth){
            return true
        } else{
            if(resp.resultSetJSON){
                response.status = resp?.status
                render resp.resultSetJSON as JSON
                return resp.auth
            } else if(resp.resultSetBody){
                response.status = resp?.status
                render resp.resultSetBody
                return resp.auth
            } else{
                response.status = resp?.status
                render 0
                return resp.auth
            }
        }

    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
