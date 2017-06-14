package com.ef.umm

import grails.compiler.GrailsCompileStatic

/**
 * Created by saqib ahmad on 6/6/2017.
 */

import grails.converters.JSON
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert
import grails.plugin.springsecurity.rest.token.rendering.*

@Slf4j
@Transactional
class MyAccessTokenJsonRenderer implements AccessTokenJsonRenderer {

    String usernamePropertyName = "username"
    String authoritiesPropertyName = "role"

    String generateJson(AccessToken accessToken) {
        Assert.isInstanceOf(UserDetails, accessToken.principal, "A UserDetails implementation is required")
        UserDetails userDetails = accessToken.principal as UserDetails
        def result = [:]
        def user
        user = User.findByUsername(userDetails.username)

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
}