package com.ef.umm

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import org.apache.commons.httpclient.HttpException
import org.apache.commons.httpclient.methods.DeleteMethod
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair
import org.springframework.web.client.ResourceAccessException;

import static org.springframework.http.HttpStatus.*

@Transactional(noRollbackFor=[ResourceAccessException, ConnectException])
class CCSettingsService {
    int ccType //0 for CCE, 1 for CCX
    // setting for the ucce prompt network
    String domain,username,password,machineIp,sharedFolder
    //setting for ucce Database
    String databaseMachineIp,databaseName,databaseUsername,databasePassword
    //Secondary and primary UCCX server
    String secondaryIp,secondaryUsername,secondaryPassword
    String primaryIp,primaryUsername,primaryPassword
    String webRequest

    def utilityService

    def initialize(){
        if(ApplicationSetting.count > 0){
            def applicationSetting = ApplicationSetting.first()

            primaryIp = applicationSetting?.primaryIp;
            primaryUsername = applicationSetting?.primaryUsername
            primaryPassword = applicationSetting?.primaryPassword
            secondaryIp = applicationSetting?.secondaryIp
            secondaryPassword = applicationSetting?.secondaryPassword
            secondaryUsername = applicationSetting?.secondaryUsername
            webRequest = applicationSetting?.webRequest
            domain = applicationSetting?.domain
            username = applicationSetting?.username
            password = applicationSetting?.password
            machineIp = applicationSetting?.machineIp
            sharedFolder = applicationSetting?.sharedFolder
            databaseMachineIp = applicationSetting?.databaseMachineIp
            databaseName = applicationSetting?.databaseName
            databasePassword = applicationSetting?.databasePassword
            databaseUsername = applicationSetting?.databaseUsername

//            checkUccxStatusService.initializeBeans()
        }
    }

    def setSettings(settings){
        ccType = settings.ccType
        if(ccType ==0){ // Contact Center is of CCX type
            primaryIp = settings?.primaryIp;
            primaryUsername = settings?.primaryUsername
            primaryPassword = settings?.primaryPassword
            secondaryIp = settings?.secondaryIp
            secondaryPassword = settings?.secondaryPassword
            secondaryUsername = settings?.secondaryUsername
            webRequest = settings?.webRequest
        }
        if(ccType == 1){ // Contact Center is of CCE type
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
        if(ccType == 0){
            renderJson.put("primaryIp", primaryIp)
            renderJson.put("primaryUsername", primaryUsername)
            renderJson.put("primaryPassword", primaryPassword)
            renderJson.put("secondaryIp", secondaryIp)
            renderJson.put("secondaryPassword", secondaryPassword)
            renderJson.put("secondaryUsername", secondaryUsername)
            renderJson.put("webRequest", webRequest)
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

    def flushSettings(){
        ccType = null
        primaryIp = null
        primaryUsername = null
        primaryPassword = null
        secondaryIp = null
        secondaryPassword = null
        secondaryUsername = null
        webRequest = null
        domain = null
        username = null
        password = null
        machineIp = null
        sharedFolder =null
        databaseMachineIp = null
        databaseName = null
        databasePassword = null
        databaseUsername = null
    }

    def checkAuthentication(username, password) {

        try {
            AuthenticationBean authenticationBean = new AuthenticationBean()
            authenticationBean?.setUsername(username)
            authenticationBean?.setPassword(password)
            authenticationBean?.setBaseUrl(webRequest+"://"+primaryIp+"/adminapi")

            def result =callAPI(authenticationBean, "GET", "/skill/", null, null, null)
            if(result.available){
                if (result?.containsKey("apiError")) {
                    def checkingStatus = result?.get("apiError")
                    if (checkingStatus?.get('errorType')?.equals("Unauthorized")) {
                        return false
                    }
                }
            }else{
                authenticationBean?.setUsername(username)
                authenticationBean?.setPassword(password)
                authenticationBean?.setBaseUrl(webRequest+"://"+secondaryIp+"/adminapi")
                if (result?.containsKey("apiError")) {
                    def checkingStatus = result?.get("apiError")
                    if (checkingStatus?.get('errorType')?.equals("Unauthorized")) {
                        return false
                    }
                }
            }

            return true
        } catch (Exception ex) {
            log.error("Exception while authenticate user in uccx and exception is " + ex.getMessage())
            return false
        }

    }

    def publish(applicationSetting){
        def micros = Microservice.list()
        def result = []
        def resp
        micros.each{
            if(it?.name!= "umm"){
                try{
                    resp = updateMicro("post", it?.ipAddress,
                            "/${it?.name}/applicationSetting/SetCCSettings",applicationSetting)
                    result.push("Publish response for ${it?.name} is: ${resp.responseEntity.statusCode.value}")
                }catch(ResourceAccessException ex){
                    log.error "Exception occured while publishing application settings: ${ex}"
                } catch(ConnectException ex){
                    log.error "Exception occured while publishing application settings: ${ex}"
                }
            }
        }
        return result
    }

    def updateMicro(method, ip, req, apInstance){
        def rest = new RestBuilder()
        def result = [
                value: true,
                *:extractProperties(apInstance)
        ]
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>()
        result.each{k, v->
            params.add(new BasicNameValuePair(k as String, v as String))
        }
        params.subList(20, params.size()).clear();
        def qp= URLEncodedUtils.format(params, "UTF-8")
        def resp = rest."${method}"("${ip}${req}?${qp}")
        return resp
    }

    //Method to convert an object into a map
    def extractProperties(obj) {
        obj.getClass()
                .declaredFields
                .findAll { !it.synthetic }
                .collectEntries { field ->
            [field.name, obj."$field.name"]
        }
    }
    def verifySetting(paramsConfig) {
        def resultSet = [:]
        try {

            log.info "Going to verify the config Setting  for these parameters " + paramsConfig
            if (!paramsConfig.machineIp || !paramsConfig.sharedFolder || !paramsConfig.username || !paramsConfig.password) {
                resultSet = ["message": "error", "errorMessage": "Configurations not been defined "]
                return resultSet
            }

            def se = paramsConfig.domain + ";" + paramsConfig.username + ":" + paramsConfig.password
            log.info("Get property value ....................${se}")

            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("${se}");

            //log.info("SERVER:::::::::::::: ${settings.promptLocation}")
            SmbFile folder = new SmbFile("smb://" + paramsConfig.machineIp + "/" + paramsConfig.sharedFolder + "/", auth);


            if (folder.exists()) {
                resultSet = ["message": "OK"]
            } else {
                resultSet = ["message": "NOExist"]
            }


        } catch (MalformedURLException mFEx) {
            log.info "Error while fetching file from the server. and error is " + mFEx.getMessage()
            log.error "Error while fetching file from the server. and error is " + mFEx.getMessage()
            resultSet = ["message": "errorUrl", "errorMessage": mFEx.getMessage(), status: NOT_FOUND]
        } catch (Exception ex) {
            log.info "Error while fetching file from the server. and error is " + ex.getMessage()
            log.error "Error while fetching file from the server. and error is " + ex.getMessage()
            resultSet = ["message": "error", "errorMessage": ex.getMessage(), status: NOT_FOUND]
        }

        return resultSet
    }

    def checkAuthenticationForOne(jsonData) {
        AuthenticationBean authenticationBean = new AuthenticationBean()
        authenticationBean?.setUsername(jsonData?.username)
        authenticationBean?.setPassword(jsonData?.password)
        authenticationBean?.setBaseUrl(jsonData?.webBase+"://"+jsonData?.ip+"/adminapi")
        try {
            def result = callAPI(authenticationBean, "GET", "/skill/", null, null, null)
            def checkingStatus = result?.get("data")
            if (checkingStatus?.containsKey("apiError")) {
                if (checkingStatus?.get("apiError")?.get('errorType')?.equals("Unauthorized")) {

                    return  ["message":"error","errorMessage":"Credentials are not correct","status":NOT_FOUND]

                }else{
                    return  ["message":"error","errorMessage":checkingStatus?.get("apiError")?.get('errorMessage'),"status":NOT_FOUND]
                }
            }
            else if(checkingStatus?.size()==0){
                return ["message":"error","errorMessage":"Server Not found","status":NOT_FOUND]
            }
            else return ["message":"OK"]

        }catch (Exception ex){
            log.error("Exception while authenticate user in uccx and exception is " + ex.getMessage())
            return   ["message":"error","errorMessage":ex.getMessage(),"status":NOT_FOUND]
        }


    }

    def callAPI( bean, method, url, String jsonString, file, download) {
        def httpMethod
        try {
            def urlToCall
            urlToCall = bean.getBaseUrl() + url.replace('//', '/')
            log.info("Sending request to UCCX: ${method}, ${urlToCall} with JSON: \n ${jsonString}")
            switch (method) {
                case "GET":
                    httpMethod = new GetMethod(urlToCall)
                    break
                case "PUT":
                    httpMethod = new PutMethod(urlToCall)
                    break
                case "POST":
                    httpMethod = new PostMethod(urlToCall)
                    break
                case "DELETE":
                    httpMethod = new DeleteMethod(urlToCall)
                    break
                default:
                    httpMethod = new GetMethod(urlToCall)
            }
            def result = utilityService.callApi(bean, httpMethod, jsonString, file, download)
            return [available: true, data: result]
        } catch (HttpException e) {
            log.error("UnknownHostException occurred while executing http method for url ${method?.getPath()}")
            log.error "Exception is " + e.getMessage()
            return [status:BAD_REQUEST, available: false]
        } catch (UnknownHostException e) {
            log.error("UnknownHostException occurred while executing http method for url ${method?.getPath()}, invalid host url")
            log.error "Exception is " + e.getMessage()
            return [status: BAD_REQUEST, available: false]
        } catch (ConnectException e) {
            log.error("ConnectException occurred while executing http method for url ${method?.getPath()}, CCX is not accessible, most probably it is down")
            log.error "Exception is " + e.getMessage()
            return [status: BAD_REQUEST, available: false]//service unavailable
        } catch (SocketException sc) {
            log.error("Exception while calling UCCX API")
            log.error "Exception is " + sc.getMessage()
            return [status: BAD_REQUEST, available: false]
        } catch (SocketTimeoutException sc) {
            log.error("SocketTimeoutException while calling UCCX API")
            log.error "SocketTimeoutException is " + sc.getMessage()
            return [status: BAD_REQUEST, available: false]
        }
        catch (Exception ex) {
            log.error("Exception while calling UCCX API")
            log.error "Exception is " + ex.getMessage()
            return [status: BAD_REQUEST, available: false]//service unavailable
        }

        finally {
            httpMethod?.releaseConnection()
        }
    }


}
