package com.ef.umm

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import grails.transaction.Transactional

import java.text.SimpleDateFormat


//Define an Interface that exactly matches our header file
interface LicensingInterface extends Library {
    LicensingInterface LI = (LicensingInterface) Native.loadLibrary("TrippleDesJNA", LicensingInterface.class)
    String encrypt(String key, String value)
    String encrypt(String value)
    String decrypt(String key, String value)
    Pointer decrypt(String value)
    void free_decrypted_string(Pointer str)
}

@Transactional
class LicensingService {
    String pattern = "yyyy-MM-dd"
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern)


    String key = null

    // Attributes of license
    Date creationDate = null
    Date expiryDate = null
    Date supportExpiryDate=null
    int numberOfAgents = 0
    def mac = null
    String licensedTo = null
    String licStatus

    // Validity flag for license
    Boolean valid

    def init() {

        def licenseKey = getLicenseFromDB()

        if(!licenseKey){
            valid = false
        } else{
            Pointer decrypted
            try{
                decrypted = LicensingInterface.LI.decrypt(licenseKey)
                def parsed = parseDecryptedKey(decrypted.getString(0))
                def arr = []
                parsed.each{
                    arr.push(it.value)
                }
                (creationDate,
                expiryDate,
                supportExpiryDate,
                numberOfAgents,
                mac,
                licensedTo,
                licStatus,
                valid) = arr

            } catch(Exception ex){
                String key = null

                valid = false
                log.error("Exception occurred during initialization of license parameters: ${ex.getMessage()}", ex)
            } finally{
                if(decrypted)
                    LicensingInterface.LI.free_decrypted_string(decrypted)
            }
        }
    }

    def getLicenseFromDB(){
        if(License.count()!=0){
            def licenseKey = License.first()
            key = licenseKey.licenseKey
            return licenseKey.licenseKey
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
                mac: mac,
                licStatus: licStatus,
                valid: valid]
    }


    def parseDecryptedKey(decrypted){
        try{
            def result = [:]
            def tokenized = decrypted.tokenize("#")

            result.creationDate = simpleDateFormat.parse(tokenized[0].toString())
            if(tokenized[1] =~ /([0-9]{4})-([0-9]{2})-([0-9]{2})/)
                result.expiryDate = simpleDateFormat.parse(tokenized[1].toString())
            else
                result.expiryDate = null

            if(tokenized[2] =~ /([0-9]{4})-([0-9]{2})-([0-9]{2})/)
                result.supportExpiryDate = simpleDateFormat.parse(tokenized[2].toString())
            else
                result.supportExpiryDate = null

            result.numberOfAgents = tokenized[3] as Integer
            result.mac =tokenized[4]
            result.licensedTo = tokenized[5]
            result.licStatus = tokenized[6]
            result.valid = true
            return result
        }catch (Exception ex){
            log.error("Error occurred while parsing decrypted key: ${ex.getMessage()}", ex)
            return null
        }
    }

    def validateKey(licenseKey){
        Pointer decrypted
        try{
            decrypted = LicensingInterface.LI.decrypt(licenseKey)
            def res = parseDecryptedKey(decrypted.getString(0))
            log.info("Successfully validated the license key.")
            return res
        }catch(Exception ex){
            log.error("Exception occurred while validating key: ${ex.getMessage()}", ex)
            return [valid: false]
        }finally{
            if(decrypted)
                LicensingInterface.LI.free_decrypted_string(decrypted)
        }
    }

    // Validates the license based on the attributes.
    def validateLicense(){

        if(!valid){
            return ["license": "invalid"]
        } else {
            def curMAC = getCurrentMAC()
            def curDate = getCurrentDate()

            if(licStatus != "trial"){
                if(!mac || !curMAC.equalsIgnoreCase(mac)){
                    return ["license": "invalidMAC"]
                }
            }

            if(expiryDate && curDate> expiryDate){
                return ["license": "expired"]
            }

            if(supportExpiryDate && curDate> supportExpiryDate){
                return ["license": "supportExpired"]
            }


            return ["license": "valid",
                    "licStatus": licStatus,
                    "licensedTo":licensedTo]

        }
    }

    def getCurrentDate(){
        return new Date()
    }

    def getCurrentMAC(){
        try{
            InetAddress address = getCurrentIp()
            NetworkInterface ni = NetworkInterface.getByInetAddress(address)
            def macDeployed = ni.getHardwareAddress()
            StringBuilder sb = new StringBuilder()
            for (int i = 0; i < macDeployed.length; i++) {
                sb.append(String.format("%02X%s", macDeployed[i], (i < macDeployed.length - 1) ? ":" : ""))
            }
            return sb.toString()
        }catch (Exception ex){
            log.error("Error occurred while getting current machine's MAC address: ${ex.getMessage()}", ex)
            return null
        }
    }

    InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement()
                Enumeration<InetAddress> nias = ni.getInetAddresses()
                while(nias.hasMoreElements()) {
                    InetAddress ia= (InetAddress) nias.nextElement()
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && !ni.getDisplayName().contains("docker")
                            && ia instanceof Inet4Address) {
                        return ia
                    }
                }
            }
        } catch (SocketException e) {
            log.error("Exception occurred while getting current IP of the system: " + e.getMessage(), e)
        }
        return null
    }
}
