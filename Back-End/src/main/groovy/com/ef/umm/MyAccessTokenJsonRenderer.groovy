package com.ef.umm

/**
 * Created by saqib ahmad on 6/6/2017.
 */

import grails.converters.JSON
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.transaction.Transactional
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert
import grails.plugin.springsecurity.rest.token.rendering.*
import grails.plugins.rest.client.RestBuilder
import grails.core.GrailsApplication

@Slf4j
@Transactional
class MyAccessTokenJsonRenderer implements AccessTokenJsonRenderer {
    String usernamePropertyName = "username"
    String authoritiesPropertyName = "role"
    def AuthorizationService
    def userService

    String generateJson(AccessToken accessToken) {
        Assert.isInstanceOf(UserDetails, accessToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = accessToken.principal as UserDetails
        def result = [:]
        def user
        user = User.findByUsername(userDetails.username)
        if(user){
            if(user?.type == "CC"){
                def resp = getTeams(userDetails.username)
                if(resp){
                    result.put("teams" , resp)
                }
            }

            if(!user?.isActive){
                result.put("status" , "isNotActive")
            } else {
                user.lastLogin = new Date()
                user.save(flush:true)
            }
        } else {
            try{
                def adUser = new User(username: userDetails.username, password: AuthorizationService.maskIt(), type: "AD",
                        isActive: true)
                adUser.save(flush: true, failOnError: true)
            }catch (Exception ex){
                log.error("Exception occured while saving active directory user in DB.", ex)
            }
        }


        result.put("userDetails" , user)
        result.put("token_type", 'Bearer')
        result.put("token", accessToken.accessToken)
        result.put("expires_in" , accessToken.expiration)
        result.put("refresh_token" , accessToken.refreshToken)



        if (userDetails instanceof OauthUser) {
            CommonProfile profile = (userDetails as OauthUser).userProfile
            result.email = profile.email
            result.displayName = profile.displayName
        }

        def jsonResult = result as JSON

        log.debug "Generated JSON:\n${jsonResult.toString(true)}"

        return jsonResult.toString()
    }

    def getTeams(String user){
//        def rest = new RestBuilder()
//        def config = Holders.config
//        def resp
//        def adminPanel = config.getProperty('names.adminPanel')
//        def serv = Microservice.findByName(adminPanel)
//        if(serv)
//            resp = rest.get("${serv?.ipAddress}/${serv.name}/agent/getAgentTeam?id=${user}")
        def resp = userService.getAgentTeams(user)
        return resp
    }
}