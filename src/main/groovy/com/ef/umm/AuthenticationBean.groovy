package com.ef.umm

/**
 * Created by saqib ahmad on 10/19/2017.
 */

import org.apache.commons.httpclient.Credentials
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.params.HttpClientParams

class AuthenticationBean {
    String username = null;
    String password = null;
    String baseUrl;
    String uccxServerIpAddr;
    String agentListUrl;
    String groupUrl;
    def method
    private static AuthenticationBean instance = null;
    HttpClient client

    def getMethod() {
        return method
    }

    void setMethod(method) {
        this.method = method
    }

    String getGroupUrl() {
        return groupUrl
    }

    void setGroupUrl(String groupUrl) {
        this.groupUrl = groupUrl
    }

    String getAgentListUrl() {
        return agentListUrl
    }

    void setAgentListUrl(String agentListUrl) {
        this.agentListUrl = agentListUrl
    }

    String getUsername() {
        return username
    }

    String getPassword() {
        return password
    }

    String getBaseUrl() {
        return baseUrl
    }

    String getUccxServerIpAddr() {
        return uccxServerIpAddr
    }

    void setUsername(String username) {

        this.username = username
    }

    void setPassword(String password) {
        this.password = password
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl
    }

    void setUccxServerIpAddr(String uccxServerIpAddr) {
        this.uccxServerIpAddr = uccxServerIpAddr
    }

    public  AuthenticationBean(String username, String password, String baseUrl, String uccxServerIpAddr) {
        this.username = username
        this.password = password
        this.baseUrl = baseUrl
        this.uccxServerIpAddr = uccxServerIpAddr
    }

    public  AuthenticationBean(String username, String password, String baseUrl) {
        this.username = username
        this.password = password
        this.baseUrl = baseUrl
    }
    public AuthenticationBean(){

    }

    public HttpClient getHttpClient(){
        Credentials credentials = new UsernamePasswordCredentials(this.username, this.password);

        HttpClientParams params = new HttpClientParams()
        params.setSoTimeout(3000)
        params.setConnectionManagerTimeout(3000L)
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(3000)
        client.getState().setCredentials(AuthScope.ANY, credentials);
        return client
    }

    public static AuthenticationBean getInstance(username, password, baseUrl, uccxServerIpAddr){
        if(instance == null){
            instance = new AuthenticationBean(username,password,baseUrl,uccxServerIpAddr)
        }
        return instance
    }

}

