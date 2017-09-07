package com.ef.umm

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.transaction.Transactional
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonSlurper


@Transactional
class RestService {
    def authorizationService
    GrailsApplication grailsApplication

    def APUserSync(){
        User user
        def adminPanel = getAPName()
        def micro = Microservice.findByName(adminPanel)
        def resp = getCCXUserList(micro)
        log.info("Response for CCX user Sync is: ${resp.responseEntity.statusCode.value}:${resp.json}")
        def currentUserList = User.findAllByType("CC")
        def userList = resp?.json
        def supervisor = Role.findByAuthority("supervisor")
        def AP = Microservice.findByName(adminPanel)
        userList.each {
            user = User.findByUsername(it?.username)
            if (!user) {
                try {
                    user = new User(username: it?.username, password: "123456!", isActive: true, fullName: it?.fullName,
                            dateCreated: it?.dateCreated, lastUpdated: it?.lastUpdated, lastLogin: it?.lastLogin,
                            email: it?.email, type: "CC")
                    user?.profileExists = it?.profileExists
                    user.save(flush: true, failOnError: true)

                    UMR.create user, supervisor, AP, true

                } catch (Exception ex) {
                    log.error("Error while Syncing user and Error is ")
                    log.error("_____________________________________")
                    log.error(ex.getMessage())
                    log.error("_____________________________________")
                }
            } else {
                user?.profileExists = it?.profileExists
                user.save(flush: true, failOnError: true)
            }
        }

        currentUserList?.each {
            try {
                if (!userList?.findAll { u -> u?.get("username") == it.username } ){
                    def umr = UMR.findAllByUsers(it)
                    umr.each{um->
                        um?.delete(flush: true, failOnErrors:true)
                    }
                    it.delete(flush: true)
                }

            } catch (Exception e) {
                log.error("Error occurred while deleting user which was deleted from CUCM.. ${e.getMessage()}")
            }
        }

        return resp.responseEntity.statusCode.value
    }

    def callAPI(def params, def request){
        params = purgeParams(params)
        def queryString = params.collect { k, v -> "$k=$v" }.join(/&/)
        def queryJson = params as JSON
        def microName, controller, action
        def req = request?.forwardURI - "/umm"

        def method = request?.method.toLowerCase()
        def userName= extractUsername(request)
        (microName, controller, action) = extractUri(request)
        def micro = Microservice.findByName(microName)
        def jsonData = request.getJSON()

        def jsonString = jsonData as JSON
        def jsonSlurped= new JsonSlurper().parseText(jsonString as String)
        def resp

        if(params && jsonData){
            if(action == "save" || action == "create"){
                queryString = queryString + "&createdBy='${userName}'"
            } else {
                queryString = queryString + "&updatedBy='${userName}'"
            }

            if(params?.keySet == jsonSlurped?.keySet){
                jsonData = jsonData as JSON
                resp = makeRestCall(1, method, micro, req, jsonData, null, null)
//                resp = rest."${method}"("${micro?.ipAddress}${req}"){
//                    json(jsonData)
//                }
            } else{
                if(validJson(params.values())){
                    resp =makeRestCall(2, method, micro, req, null, queryJson, null)
//                    resp = rest."${method}"("${micro?.ipAddress}${req}") {
//                        json(queryJson)
//                    }
                }
                else{
                    jsonData = jsonData as JSON
                    resp =makeRestCall(3, method, micro, req, jsonData, null, queryString)
//                    resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}"){
//                        json(jsonData)
//                    }
                }
            }

        }else if(params){

            if(validJson(params.values())) {
                log.info("Sending JSON in the body: " + queryJson)
                resp =makeRestCall(4, method, micro, req, null, queryJson, null)
//                resp = rest."${method}"("${micro?.ipAddress}${req}") {
//                    json(queryJson)
//                }
            } else{
                if(action == "save" || action == "create"){
                    queryString = queryString + "&createdBy='${userName}'"
                } else {
                    queryString = queryString + "&updatedBy='${userName}'"
                }
                log.info("Sending parameters as Query String: " + queryString)
                resp = makeRestCall(5, method, micro, req, null, null, queryString)
//                resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}")
            }
        } else if(jsonData){
            if(action == "save" || action == "create"){
                jsonData['createdBy'] = userName
            } else {
                jsonData['updatedBy'] = userName
            }
            jsonData = jsonData as JSON
            resp =makeRestCall(6, method, micro, req, jsonData, null, null)
//            resp = rest."${method}"("${micro?.ipAddress}${req}") {
//                json(jsonData)
//            }
        } else{
            resp = makeRestCall(7, method, micro, req, null, null, null)
//            resp = rest."${method}"("${micro?.ipAddress}${req}")
        }

        return resp
    }

    private makeRestCall(def type, def method, def micro, def req, def jsonData, def queryJson, def queryString){
        def rest = new RestBuilder()
        def resp
        switch(type){
            case 1:
                resp = rest."${method}"("${micro?.ipAddress}${req}"){
                    json(jsonData)
                }
                break
            case 2:
                resp = rest."${method}"("${micro?.ipAddress}${req}") {
                    json(queryJson)
                }
                break
            case 3:
                resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}"){
                    json(jsonData)
                }
                break
            case 4:
                resp = rest."${method}"("${micro?.ipAddress}${req}") {
                    json(queryJson)
                }
                break
            case 5:
                resp = rest."${method}"("${micro?.ipAddress}${req}?${queryString}")
                break
            case 6:
                resp = rest."${method}"("${micro?.ipAddress}${req}") {
                    json(jsonData)
                }
                break
            case 7:
                resp = rest."${method}"("${micro?.ipAddress}${req}")
                break
        }
        return resp
    }

    private purgeParams(def params){
        if(params.containsKey('format'))
            params.remove("format")
        if(params.containsKey('controller'))
            params.remove("controller")
        if(params.containsKey('action'))
            params.remove("action")
        return params
    }
    private extractUsername(def req){
        return authorizationService.extractUsername(req?.getHeader("Authorization"))
    }

    private extractUri(def req){
        return authorizationService.extractURI(req.forwardURI)
    }

    private getAPName(){
        return grailsApplication.config.getProperty('names.adminPanel')
    }

    private getCCXUserList(def micro){
        def rest = new RestBuilder()
        return rest.get("${micro?.ipAddress}/${micro.name}/user/list")
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
        return false
    }
}
