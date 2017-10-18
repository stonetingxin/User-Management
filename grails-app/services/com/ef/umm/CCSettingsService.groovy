package com.ef.umm

import grails.transaction.Transactional
/*
* This service is to serve the contact center settings in application wherever needed
* Settings will be saved by Base Application in the variables of of this service and as Services are singleton
* settings will be there for always.
* Settings can be set by calling 'setSettings()' and can be obtained calling 'getSettings()'
* */
@Transactional
class CCSettingsService {
    int ccType //0 for CCE, 1 for CCX
    // setting for the ucce prompt network
    String domain,username,password,machineIp,sharedFolder
    //setting for ucce Database
    String databaseMachineIp,databaseName,databaseUsername,databasePassword
    //Secondary and primary UCCX server
    String secondaryIp,secondaryUsername,secondaryPassword
    String primaryIp,primaryUsername,primaryPassword

    def setSettings(settings){
        ccType = settings.ccType
        if(ccType ==1){ // Contact Center is of CCX type
            primaryIp = settings?.primaryIp;
            primaryUsername = settings?.primaryUsername
            primaryPassword = settings?.primaryPassword
            secondaryIp = settings?.secondaryIp
            secondaryPassword = settings?.secondaryPassword
            secondaryUsername = settings?.secondaryUsername
        }
        if(ccType == 0){ // Contact Center is of CCE type
            domain = settings?.domain
            username = settings?.username
            password = settings?.password
            machineIp = settings?.machineIp
            sharedFolder = settings?.sharedFolder
            databaseMachineIp = settings?.databaseMachineIp
            databaseName = settings?.databaseName
            databasePassword = settings?.databasePassword
            databaseUsername = settings?.databaseUsername
        }
        getSettings()
    }
    def getSettings(){
        def renderJson = [:]
        if(ccType == 1){
            renderJson.put("primaryIp", primaryIp)
            renderJson.put("primaryUsername", primaryUsername)
            renderJson.put("primaryPassword", primaryPassword)
            renderJson.put("secondaryIp", secondaryIp)
            renderJson.put("secondaryPassword", secondaryPassword)
            renderJson.put("secondaryUsername", secondaryUsername)
        }
        else{
            renderJson.put("domain", domain)
            renderJson.put("username", username)
            renderJson.put("password",password)
            renderJson.put("machineIp",machineIp)
            renderJson.put("sharedFolder",sharedFolder)
            renderJson.put("databaseMachineIp",databaseMachineIp)
            renderJson.put("databaseName",databaseName)
            renderJson.put("databasePassword",databasePassword)
            renderJson.put("databaseUsername",databaseUsername)
        }
        return renderJson
    }
}
