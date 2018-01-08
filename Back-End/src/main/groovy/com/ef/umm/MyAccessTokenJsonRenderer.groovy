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
    def authorizationService
    def userService
    def licensingService

    String generateJson(AccessToken accessToken) {
        Assert.isInstanceOf(UserDetails, accessToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = accessToken.principal as UserDetails
        def result = [:]
        def user

        result << populateLicense()

        user = User.findByUsername(userDetails.username)
        if(user){
            if(user?.type == "CC"){
                def resp = getTeams(userDetails.username)
                if(resp.size() != 0){
                    result.put("teams" , resp)
                } else{
                    result.put("teams", "noTeams")
                }
            }

            if(!user?.isActive){
                result.put("status" , "isNotActive")
            } else {
                if(result?.license != "agentLimitExceeded"
                        || result?.license != "supportExpired_agentLimitExceeded")
                {
                    user.lastLogin = new Date()
                    user.isLoggedIn = true
                    user.save(flush:true)
                }
            }
        } else {
            try{
                def adUser = new User(username: userDetails.username, password: authorizationService.maskIt(), type: "AD",
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

        def resp = userService.getAgentTeams(user)
        return resp
    }

    def populateLicense(){

        def validity = authorizationService.validateLicense()
        def loggedInUsers = User.countByIsLoggedIn(true)

        if(validity == "invalid"){
            return ["license": "invalid"]
        } else if(validity == "expired"){
            return ["license": "licenseExpired"]
        } else if(validity == "supportExpired" && loggedInUsers > licensingService.numberOfAgents){
            return ["license": "supportExpired_agentLimitExceeded", "agentLimit":licensingService.numberOfAgents]
        } else if(loggedInUsers > licensingService.numberOfAgents){
            return ["license": "agentLimitExceeded", "agentLimit":licensingService.numberOfAgents]
        } else if(validity == "validLicense"){
            return ["license": "validLicense", "licensedTo" : licensingService.licensedTo]
        }
    }
}