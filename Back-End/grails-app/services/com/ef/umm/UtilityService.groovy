package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import org.apache.commons.httpclient.ConnectTimeoutException
import org.apache.commons.httpclient.HttpException
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part

import javax.naming.AuthenticationException

@Transactional
class UtilityService {


    def callApi(AuthenticationBean bean, method, String jsonString, file, download) throws HttpException, IOException, UnknownHostException, ConnectException, AuthenticationException, ConnectTimeoutException, SocketException, SocketTimeoutException {
        String forXML = jsonString?.substring(0, 14)
        def client

        if (download && !forXML.equals("{\"campaignName"))
            method.addRequestHeader("accept", "application/xml")
        else
            method.addRequestHeader("accept", "application/json")

        if (file != null) {
            Part[] parts = new FilePart("file", convertMultipartFileToFile(file))
            method.addRequestHeader("mediaType", "multipart/form-data")
            method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()))
        }
        if ((method instanceof PostMethod || method instanceof PutMethod) && file == null) {
            StringRequestEntity requestEntity = new StringRequestEntity(jsonString, "application/json", "UTF-8");
            method?.setRequestEntity(requestEntity)
        }
        client = bean?.getHttpClient()
        client?.executeMethod(method);
        if (method instanceof GetMethod) {
            if (download)
                return method.getResponseBody()
            return JSON.parse(method.getResponseBodyAsString())
        }
        if (forXML.equals("{\"campaignName"))
            return [campaign: method.getResponseBodyAsString(), status: method.getStatusCode()]
        return [status: method.getStatusCode(), message: method.getResponseBodyAsString()]

    }

}

