package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import grails.plugins.rest.client.RestBuilder

@Transactional
class RestService {
    def authorizationService

    def callAPI(def params, def request){
        def queryString = params.collect { k, v -> "$k=$v" }.join(/&/)
        def queryJson = params as JSON
        def rest = new RestBuilder()
        def microName, controller, action
        def req = request?.forwardURI - "/umm"

        def method = request?.method.toLowerCase()
        def userName= authorizationService.extractUsername(request?.getHeader("Authorization"))
        (microName, controller, action) = authorizationService.extractURI(request.forwardURI)
        def micro = Microservice.findByName(microName)
        def jsonData = request.getJSON()

        def resp

        if(params && jsonData){
            if(action == "save" || action == "create"){
                queryString = queryString + "&createdBy='${userName}'"
            } else {
                queryString = queryString + "&updatedBy='${userName}'"
            }

            if(validJson(params.values())){
                resp = rest."${method}"("${micro?.ipAddress}${req}") {
                    json(queryJson)
                }
            }
            else{
                jsonData = jsonData as JSON
                resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}"){
                    json(jsonData)
                }
            }
        }else if(params){

            if(validJson(params.values())) {
                log.info("Sending JSON in the body: " + queryJson)
                resp = rest."${method}"("${micro?.ipAddress}${req}") {
                    json(queryJson)
                }
            } else{
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

        return resp
    }

    def validJson(def value){
        for(def val: value){
            try{
                def parsed = JSON.parse(val)
                if(parsed){
                    return true
                }
                else{
                    continue
                }
            }catch (Exception ex){
                continue
            }
        }
    }
}
