package com.ef.umm

import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

@Transactional(readOnly = true)
class ApplicationSettingController {

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def CCSettingsService
    def ucceFetchCampaignService
    def checkUccxStatusService

    def index() {
        respond ApplicationSetting.first(), [status: OK]
    }

    @Transactional
    def save(ApplicationSetting applicationSettingInstance) {
        if (applicationSettingInstance == null) {
            render status: NOT_FOUND
            return
        }

        applicationSettingInstance.validate()
        if (applicationSettingInstance.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        applicationSettingInstance.save flush: true
        CCSettingsService.setSettings(applicationSettingInstance)

        respond applicationSettingInstance, [status: CREATED]
    }

    @Transactional
    def update(ApplicationSetting applicationSettingInstance) {
        if (applicationSettingInstance == null) {
            render status: NOT_FOUND
            return
        }

        applicationSettingInstance.validate()
        if (applicationSettingInstance.hasErrors()) {
            render status: NOT_ACCEPTABLE
            return
        }

        applicationSettingInstance.save flush: true
        CCSettingsService.setSettings(applicationSettingInstance)
//        UCCEPromptService.initializeBeans()

        respond applicationSettingInstance, [status: OK]
    }

    @Transactional
    def delete(ApplicationSetting applicationSettingInstance) {

        if (applicationSettingInstance == null) {
            render status: NOT_FOUND
            return
        }

        applicationSettingInstance.delete flush: true
        render status: NO_CONTENT
    }


    def verifyTheNetworkSetting(){
        def result = UCCEPromptService.verifySetting(request.getJSON())
        if(result?.status){
            response.status = 406
        }
        respond result
    }

    def verifyTheDatabaseSetting() {
        def result = ucceFetchCampaignService.getConnection(request.getJSON())
        if(result?.get("message")?.equals("OK")){
            result = ["message":"OK"]
        }
        if(result?.status){
            response.status = 406
        }
        respond result
    }

    def verifyTheUCCXSetting() {
        def jsonData = request.getJSON()
        def result = checkUccxStatusService.checkAuthenticationForOne(jsonData)
        if(result?.status){
            response.status = 406
        }
        respond result
    }
}

