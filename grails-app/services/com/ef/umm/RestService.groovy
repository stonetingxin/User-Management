package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import grails.plugins.rest.client.RestBuilder

@Transactional
class RestService {
    def authorizationService

    def APUserSync(){
        User user
        def rest = new RestBuilder()
        def micro = Microservice.findByName("efadminpanel")
        def resp = rest.get("${micro?.ipAddress}/${micro.name}/user/list")
        log.info("Response for CCX user Sync is: ${resp.responseEntity.statusCode.value}:${resp.json}")
        def currentUserList = User.findAllByType("CC")
        def userList = resp?.json
        def supervisor = Role.findByAuthority("supervisor")
        def AP = Microservice.findByName("efadminpanel")
        userList.each {
            user = User.findByUsername(it?.username)
            if (!user) {
                try {
                    user = new User(username: it?.username, password: "123456!", isActive: true, fullName: it?.fullName,
                            dateCreated: it?.dateCreated, lastUpdated: it?.lastUpdated, lastLogin: it?.lastLogin,
                            email: it?.email, type: "CC")
                    user.save(flush: true, failOnError: true)

                    UMR.create user, supervisor, AP, true

                } catch (Exception ex) {
                    log.error("Error while Syncing user and Error is ")
                    log.error("_____________________________________")
                    log.error(ex.getMessage())
                    log.error("_____________________________________")
                }
            }
        }

        currentUserList?.each {
            try {
                if (!userList?.findAll { u -> u?.get("username") == it.username } /*&& it.username != 'admin'*/)
                    it.delete(flush: true)
            } catch (Exception e) {
                log.error("Error occurred while deleting user which was deleted from CUCM.. ${e.getMessage()}")
            }
        }

        return resp.responseEntity.statusCode.value
    }

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

        if(request.multipartFiles){
            resp = rest."${method}"("${micro?.ipAddress}${req}") {
                contentType "multipart/form-data"
                file = params.file
//                setProperty "file", params.file
//                setProperty "agentId", params.agentId
            }
            return resp
        }

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
        return false
    }
}
