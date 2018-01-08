package com.ef.umm

import com.ef.apps.licensing.JniWrapper
import grails.converters.JSON

import java.text.SimpleDateFormat

import static org.springframework.http.HttpStatus.*

class LicenseController {

    def licensingService
    def index(){
        def resultSet = [:]
        def lic = License.first()
        if(lic){
            response.status = 200
            resultSet.put("status", OK)
            resultSet.put("licenseKey", lic.lic)
            resultSet.creationDate = licensingService.creationDate
            resultSet.expiryDate = licensingService.expiryDate
            resultSet.supportExpiryDate = licensingService.supportExpiryDate
            resultSet.numberOfAgents = licensingService.numberOfAgents
            resultSet.mac = licensingService.mac
            resultSet.licensedTo = licensingService.licensedTo
            resultSet.valid = licensingService.valid
            render resultSet as JSON
            return
        } else {
            response.status = 404
            resultSet.put("status", NOT_FOUND)
            render resultSet as JSON
        }
    }

    def save(){
        def resultSet = [:]
        if(!params?.lic){
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

            def newInstance = new License(lic: params?.lic)
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
                resultSet << [licenseKey:params?.lic]
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
            log.error("Exception occured while creating/updating license: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    def validate(){
        def resultSet = [:]
        String pattern = "yyyy-MM-dd"
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)
        def licenseKey = params?.lic
        if(!licenseKey){
            response.status = 406
            resultSet.put("status", NOT_ACCEPTABLE)
            resultSet.put("message", "Please provide license key in parameters.")
            render resultSet as JSON
            return
        }

        try{
            def keyString = licenseKey.toString()
            def decrypted = JniWrapper.decrypt(keyString)
            def tokenized = decrypted.tokenize("#")
            resultSet.creationDate = simpleDateFormat.parse(tokenized[0])
            resultSet.expiryDate = simpleDateFormat.parse(tokenized[1])
            resultSet.supportExpiryDate = simpleDateFormat.parse(tokenized[2])
            resultSet.numberOfAgents = tokenized[3].toInteger()
            def fullMAC = tokenized[4]
            resultSet.mac = fullMAC.tokenize(":")
            resultSet.licensedTo = tokenized[5]
            resultSet.TRIAL = tokenized[6] == "trial"
            resultSet.valid = true
            log.info("Successfully validated the license key.")
            resultSet.message = "success"
            response.status = 200
            render resultSet as JSON
            return

        } catch(Exception ex){
            log.error("Exception occured while validating license: ", ex)
            resultSet.creationDate = null
            resultSet.expiryDate = null
            resultSet.supportExpiryDate = null
            resultSet.numberOfAgents = null
            resultSet.mac = null
            resultSet.licensedTo = null
            resultSet.valid = false
            resultSet.message = "error"
            response.status = 406
            render resultSet as JSON
        }
    }
}
