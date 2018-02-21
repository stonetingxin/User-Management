package com.ef.umm

import grails.converters.JSON
import static org.springframework.http.HttpStatus.*

class LicenseController {

    def licensingService
    def index(){
        def resultSet = [:]
        def lic = License.first()
        if(lic){
            response.status = 200
            resultSet.put("status", OK)
            resultSet.put("licenseKey", lic.licenseKey)
            resultSet << licensingService.getAttribs()
            resultSet << licensingService.validateLicense()
            resultSet.curDate = licensingService?.getCurrentDate()
            render resultSet as JSON
            return
        } else {
            licensingService.init()
            response.status = 404
            resultSet.put("status", NOT_FOUND)
            render resultSet as JSON
        }
    }

    def save(){
        def resultSet = [:]
        if(!params?.licenseKey){
            response.status = 406
            resultSet.put("status", NOT_ACCEPTABLE)
            resultSet.put("message", "Please provide license key in parameters.")
            render resultSet as JSON
            return
        }

        try{
            def instance = License.first()
            if(instance) {
                instance.delete()
            }

            def newInstance = new License(licenseKey: params?.licenseKey)
            newInstance.validate()
            if(newInstance.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newInstance.save(flush:true, failOnError: true)

            licensingService.init()
            if(licensingService.valid){
                log.info("Successfully added the license key.")
                resultSet.put("status", CREATED)
                resultSet.put("message", "Successfully added the license key")
                resultSet << licensingService.getAttribs()
                resultSet << licensingService.validateLicense()
                resultSet << [licenseKey:params?.licenseKey]
                resultSet.curDate = licensingService?.getCurrentDate()
                response.status = 201
                render resultSet as JSON
                return
            } else{
                log.info("License key validation failed")
                resultSet.put("status", BAD_REQUEST)
                resultSet.put("message", "License key validation failed")
                response.status = 400
                render resultSet as JSON
                return
            }

        }catch (Exception ex){
            log.error("Exception occured while creating/updating license: ${ex.getMessage()}", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    def validate(){
        def resultSet = [:]

        def licenseKey = params?.licenseKey
        if(!licenseKey){
            response.status = 406
            resultSet.put("status", NOT_ACCEPTABLE)
            resultSet.put("message", "Please provide license key in parameters.")
            render resultSet as JSON
            return
        }

        try{
            resultSet << licensingService.validateKey(licenseKey)
            resultSet.curDate = licensingService?.getCurrentDate()
            response.status = 200
            render resultSet as JSON

        } catch(Exception ex){
            log.error("Exception occured while validating license: ${ex.getMessage()}", ex)
            resultSet.message = "error"
            response.status = 500
            render resultSet as JSON
        }
    }
}
