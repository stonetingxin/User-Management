package com.ef.umm

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.transaction.Transactional
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonSlurper
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import static grails.async.Promises.*
import static org.springframework.http.HttpStatus.*


@Transactional
class RestService {
    def authorizationService
    GrailsApplication grailsApplication
    def CCSettingsService
    def executorService
    UCCXServiceStatusBean uccxServiceStatusBean = new UCCXServiceStatusBean()

    def syncAgents() {
        def adminPanel
        def currentUserList
        def supervisor
        def AP
        def agentsCollate = []
        adminPanel = getAPName()
        currentUserList = User.findAllByType("CC")
        supervisor = Role.findByAuthority("supervisor")
        AP = Microservice.findByName(adminPanel)
        def supers = []
        def agentsJson
        def agentsList = []
        agentsJson = fetchAgents()
        agentsList = agentsJson?.resource

        try {
            if(agentsList){
                //Made 8 sub-lists of the agent list using collate()
                agentsCollate = agentsList.collate(Math.ceil(agentsList.size()/8) as Integer)

                //Spawned new tasks for each sub-list. Each task will have its own thread.
                //Documentation for tasks is given at https://async.grails.org/latest/guide/index.html#promises
                def p1 =task {syncUsers(agentsCollate[0], AP, supervisor)}
                def p2 =task {syncUsers(agentsCollate[1], AP, supervisor)}
                def p3 =task {syncUsers(agentsCollate[2], AP, supervisor)}
                def p4 =task {syncUsers(agentsCollate[3], AP, supervisor)}
                def p5 =task {syncUsers(agentsCollate[4], AP, supervisor)}
                def p6 =task {syncUsers(agentsCollate[5], AP, supervisor)}
                def p7 =task {syncUsers(agentsCollate[6], AP, supervisor)}
                def p8 =task {syncUsers(agentsCollate[7], AP, supervisor)}
                def result = waitAll(p1, p2, p3, p4, p5, p6, p7, p8)
                result.each{
                    if(it.size()>0){
                        supers+=it
                    }
                }
                purgeUsers(currentUserList, supers)
            }
        } catch (Exception e) {
            log.error "Error " + e.getMessage()
        }
    }

    def syncUsers(agentsList, AP, supervisor){
        User user
        def supers =[]
        agentsList.each {
            user = User.findByUsername(it?.userID)
            if (it.get("type") == 2) {
                supers.push(it)
                if (!user) {
                    try {
                        user = new User(username: it?.userID, password: "123456!", isActive: true, fullName: it?.firstName + ' ' + it?.lastName,
                                dateCreated: new Date(), email: it?.email, type: "CC")
                        user.save(flush: true, failOnError: true)
                        UMR.create user, supervisor, AP, true

                    } catch (Exception ex) {
                        log.error("Error while Syncing user and Error is ")
                        log.error("_____________________________________")
                        log.error(ex.getMessage())
                        log.error("_____________________________________")
                    }
                }
            } else {
                if (user){
                    def umr = UMR.findAllByUsers(user)
                    umr.each{
                        it.delete(flush:true, failOnError: true)
                    }
                    user.delete(flush: true)
                }
            }
        }
        return supers
    }
    def purgeUsers(currentUserList, supers){
        currentUserList?.each {
            try {
                if (!supers?.findAll { u -> u?.userID == it.username } ){
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
    }
    def fetchAgents(){
        AuthenticationBean authenticationBean = new AuthenticationBean()
        authenticationBean?.setUsername(CCSettingsService?.primaryUsername)
        authenticationBean?.setPassword(CCSettingsService?.primaryPassword)
        authenticationBean?.setBaseUrl(CCSettingsService?.webRequest+"://"+CCSettingsService?.primaryIp+"/adminapi")

        def result = CCSettingsService.callAPI(authenticationBean, "GET", "/resource/", null, null, null)
        if(result.available){
            uccxServiceStatusBean.setPrimaryServerStatus(true)
            return result.data
        }else{
            uccxServiceStatusBean.setPrimaryServerStatus(false)
            authenticationBean?.setUsername(CCSettingsService?.secondaryUsername)
            authenticationBean?.setPassword(CCSettingsService?.secondaryPassword)
            authenticationBean?.setBaseUrl(CCSettingsService?.webRequest+"://"+CCSettingsService?.secondaryIp+"/adminapi")
            if (result.available) {
                uccxServiceStatusBean.setSecondaryServerStatus(true)
                return result.data
            } else {
                uccxServiceStatusBean.setPrimaryServerStatus(true)
                uccxServiceStatusBean.setSecondaryServerStatus(false)
                return [status: SERVICE_UNAVAILABLE]
            }
        }
    }

    File convertMultipartFileToFile(file) {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
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

//        if(params.file !=null){
//            MultipartHttpServletRequest mpr
//            mpr = (MultipartHttpServletRequest) request;
//            MultipartFile file1 = mpr.getFile("file");
//
//            if(params.file instanceof  MultipartFile){
////                def webDirectory = ServletContextHolder?.servletContext?.getRealPath("/")
////                File destinationFile = new File(webDirectory + "/base/tmp/" )
//                params.file=file1
//                def rest = new RestBuilder()
//                resp = rest."${method}"("${micro?.ipAddress}${req}"){
//                    json(params)
//                    contentType "multipart/form-data"
//                    file = file1
////                    contentType "multipart/form-data"
//                }
//                return resp
//            }
//        }

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
