package com.ef.umm

import com.ef.apps.licensing.JniWrapper
import grails.transaction.Transactional

import java.text.SimpleDateFormat

import java.net.NetworkInterface

@Transactional
class LicensingService {

    String pattern = "yyyy-MM-dd"
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)

    String key = null
    Date creationDate = null
    Date expiryDate = null
    Date supportExpiryDate=null
    int numberOfAgents = 0
    def mac = null
    String licensedTo = null
    Boolean TRIAL
    Boolean valid

    def init() {
        def licenseKey = getLicenseFromDB()

        if(!licenseKey){
            valid = false
        } else{
            try{
                def keyString = licenseKey.toString()
                def decrypted = JniWrapper.decrypt(keyString)
                def tokenized = decrypted.tokenize("#")
                creationDate = simpleDateFormat.parse(tokenized[0])
                expiryDate = simpleDateFormat.parse(tokenized[1])
                supportExpiryDate = simpleDateFormat.parse(tokenized[2])
                numberOfAgents = tokenized[3].toInteger()
                def fullMAC = tokenized[4]
                mac = fullMAC.tokenize(":")
                licensedTo = tokenized[5]
                TRIAL = tokenized[6] == "trial"
                valid = true

            } catch(Exception ex){
                creationDate = null
                expiryDate = null
                supportExpiryDate = null
                numberOfAgents = 0
                mac = null
                licensedTo = null
                valid = false
            }
        }
    }

    def getLicenseFromDB(){
        if(License.count()!=0){
            def licenseKey = License.first()
            key = licenseKey.lic
            return licenseKey.lic
        }else{
            return null
        }
    }

    def getAttribs(){
        return [creationDate: creationDate,
                expiryDate: expiryDate,
                supportExpiryDate: supportExpiryDate,
                numberOfAgents: numberOfAgents,
                licensedTo: licensedTo,
                valid: valid]
    }

    def getCurrentParameters(){
        def interfaces = NetworkInterface.getNetworkInterfaces()
        def hardwareMAC =  NetworkInterface.getHardwareAddress()
        return [date: new Date(), mac:hardwareMAC]
    }
}
