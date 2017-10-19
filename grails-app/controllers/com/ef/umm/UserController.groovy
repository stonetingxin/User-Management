package com.ef.umm

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.rest.RestfulController
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovyx.net.http.HTTPBuilder
//import static groovyx.net.http.Method.GET

import static org.springframework.http.HttpStatus.*

class UserController extends RestfulController<User> {

    static allowedMethods = [create: "POST", update: "PUT", delete: "DELETE", show: "GET",
                             updatePassword: "PUT", addRevokeMicroserviceRoles: "PUT", changePassword: "PUT",
                             resetPassword: "PUT", usernameExists: "GET", deleteMulti: "DELETE"]

    def springSecurityService
    def restService
    def userService
    GrailsApplication grailsApplication

    def UserController(){
        super(User)
    }

    def index() {
        def resultSet = [:]
        resultSet.put("status", METHOD_NOT_ALLOWED)
        resultSet.put("message", "Please call a valid API.")
        response.status = 405
        render resultSet as JSON
    }

    def list(){
        restService.syncAgents()
        def resultSet = [:]
        try{
            def userList = User.list()
            resultSet.put("status", OK)
            resultSet.put("users", userList)
            response.status = 200
            render resultSet as JSON
        }catch (Exception ex){
            log.error("Couldn't retrieve the list of the users.")
            log.error("Following is the stack trace along with the error message: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    def show(){
        def resultSet = [:]
        try{
            def userInstance = User.get(params?.id)
            if (!userInstance) {
                resultSet.put("status", NOT_FOUND)
                response.status = 404
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("user", userInstance)
            render resultSet as JSON
        }catch (Exception ex){
            log.error("Couldn't retrieve the specific user." +
                    "The reason might be an invalid id provided" +
                    "in the URL." )
            log.error("Following is the stack trace along with the error message: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def resetPassword(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.newPassword){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            User userInstance = User.findById(jsonObject?.id as Long)
            if(!userInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Please provide a valid username.")
                response.status = 404
                render resultSet as JSON
                return
            }

            userInstance.password = jsonObject?.newPassword

            userInstance.validate()
            if (userInstance.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", userInstance?.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            userInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "Password has been reset successfully.")
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while resetting password: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def updatePassword(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.curPassword || !jsonObject?.newPassword){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            User userInstance = User.findById(jsonObject?.id as Long)
            if(!userInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Please provide a valid username.")
                response.status = 404
                render resultSet as JSON
                return
            }
            def activeUser = springSecurityService?.currentUser

            if(!springSecurityService?.passwordEncoder?.isPasswordValid(activeUser?.getPassword(), jsonObject?.curPassword, null)){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid password provided.")
                response.status = 406
                render resultSet as JSON
                return
            }

            userInstance.password = jsonObject?.newPassword

            userInstance.validate()
            if (userInstance.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", userInstance?.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            userInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "Password has been successfully updated.")
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while updating password: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def create(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.username || !jsonObject?.password){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }
            User newUser = User.findByUsername(jsonObject?.username)
            if(newUser){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "User with username: '${jsonObject?.username}' already exists.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newUser = new User(username: jsonObject?.username, fullName: jsonObject?.fullName,
                               password: jsonObject?.password, email: jsonObject?.email,
                               type: "DB", isActive: jsonObject?.isActive, profileExists: jsonObject?.profileExists )

            newUser.validate()
            if (newUser.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newUser.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newUser.save(flush: true, failOnError: true)
            def roleDefault= Role.findByAuthority("default")
            def adminpanel = grailsApplication.config.getProperty('names.adminPanel')
            def AP = Microservice.findByName(adminpanel)

            UMR.create newUser, roleDefault, AP, true

            resultSet.put("status", OK)
            resultSet.put("message", "New user: '${newUser.username}' has been created successfully.")
            resultSet.put("user", newUser)
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while creating new user: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def update() {
        def resultSet = [:]
        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }
            User userInstance = User.findById(jsonObject?.id as Long)

            if(!userInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Invalid update request." +
                                         " Call create API to create a new user.")
                response.status = 404
                render resultSet as JSON
                return
            }

            userInstance.fullName = jsonObject?.fullName
            userInstance.email = jsonObject?.email
            if(jsonObject.containsKey('isActive'))
                userInstance.isActive = jsonObject?.isActive

            if(jsonObject.containsKey('profileExists'))
                userInstance.profileExists = jsonObject?.profileExists

            userInstance.updatedBy = User.findById(jsonObject?.updatedBy?.id as Long)

            userInstance.validate()
            if (userInstance.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", userInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }
            userInstance.save(flush: true, failOnError: true)
//            resultSet.put("status", OK)
//            resultSet.put("username", userInstance.username)
//            resultSet.put("message", "User with username: '${userInstance.username}' has been updated successfully.")
            response.status = 200
            render userInstance as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while updating user: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def addRevokeMicroserviceRoles() {
        def resultSet = [:]
        def message = []
        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.microservices || !jsonObject?.addRevoke){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            if(!(jsonObject?.addRevoke == 'add' || jsonObject?.addRevoke == 'revoke')){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Only add or revoke is allowed in this method.")
                response.status = 406
                render resultSet as JSON
                return
            }

            def addRevoke = jsonObject?.addRevoke
            User userInstance = User.findById(jsonObject?.id as Long)
            if(!userInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Invalid add/revoke request")
                response.status = 404
                render resultSet as JSON
                return
            }

            def micros = jsonObject?.microservices

            def micro

            micros?.each{
                if(!it?.id){
                    resultSet.put("status", NOT_ACCEPTABLE)
                    resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                    response.status = 406
                    render resultSet as JSON
                    return
                }
                micro = Microservice.findById(it?.id as Long)
                if(micro){
                    def roles = it?.roles
                    if(!roles){
                        resultSet.put("status", NOT_ACCEPTABLE)
                        resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                        response.status = 406
                        render resultSet as JSON
                        return
                    }

                    def role
                    roles?.each{
                        if(!it?.id){
                            resultSet.put("status", NOT_ACCEPTABLE)
                            resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                            response.status = 406
                            render resultSet as JSON
                            return
                        }
                        role = Role.findById(it?.id as Long)
                        if(role){
                            if(addRevoke == "add"){
                                def umr = UMR.findByUsersAndMicroservicesAndRoles(userInstance, micro, role)
                               if(!umr){
                                   UMR.create userInstance, role, micro, true
                                   message.add("Successfully added ${role.authority} role in ${micro.name} for " +
                                           "user: ${userInstance.username}")
                               }
                               else{
                                   message.add("User: ${userInstance.username} already has ${role.authority} role" +
                                           " in ${micro.name}.")
                               }
                            }

                            if(addRevoke == "revoke"){
                                def umr = UMR.findByUsersAndMicroservicesAndRoles(userInstance, micro, role)
                                if(micro.name == "umm" && role.authority == "ROLE_ADMIN"
                                        && userInstance.username == "admin"){
                                    resultSet.put("status", NOT_ACCEPTABLE)
                                    resultSet.put("message", "Admin roles cannot be revoked for super user.")
                                    response.status = 406
                                    render resultSet as JSON
                                    return
                                }
                                if(umr){
                                    umr?.delete(flush: true, failOnError: true)
                                    message.add("Successfully revoked ${role.authority} role in ${micro.name} for " +
                                            "user: ${userInstance.username}")
                                }
                                else{
                                    message.add("User: ${userInstance.username} does not have ${role.authority} role" +
                                            " in ${micro.name}.")
                                }
                            }
                        }
                        else{
                            message.add("Role with id: ${it?.id} not found.")
                        }
                    }
                }
                else{
                    message.add("Microservice with id: ${it?.id} not found.")
                }
            }

            resultSet.put("status", OK)
            resultSet.put("message", message)
            resultSet.put("user", userInstance)
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while adding/revoking microservices and roles to a user: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def delete(){
        def resultSet = [:]

        try {
            def userInstance = User.findById(params?.id)
            if (!userInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Provide a valid user instance.")
                response.status = 404
                render resultSet as JSON
                return
            }
            if(userInstance.username == 'admin'){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "This user cannot be deleted. Permission denied.")
                response.status = 406
                render resultSet as JSON
                return
            }

            def umr = UMR.findAllByUsers(userInstance)
            umr.each{
                it?.delete(flush: true, failOnErrors:true)
            }
            def username = userInstance.username
            userInstance?.delete(flush: true, failOnErrors:true)
            resultSet.put("status", OK)
            resultSet.put("message", "Successfully deleted user: ${username}")
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while deleting user instance: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def deleteMulti(){
        def resultSet = [:]

        def jsonObject = params?.ids
        def message = []
        try {
            jsonObject.each{
                def json = new JsonSlurper().parseText(it)
                def userInstance = User.findById(json?.id)
                if(userInstance && userInstance.username != 'admin'){
                    def umr = UMR.findAllByUsers(userInstance)
                    umr.each{ value->
                        value?.delete(flush: true, failOnErrors:true)
                    }
                    def id = userInstance.id
                    userInstance?.delete(flush: true, failOnErrors:true)
                    message.add(id)
                }

            }

            resultSet.put("status", OK)
            resultSet.put("message", message)
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while deleting multiple user instances: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }


    @Transactional
    def updateProfilePic() {
        def file = params.file
        def agentId = params.agentId
        def response = userService.updateProfilePic(agentId, file)

        parseResponse(response)
    }

    def deleteProfilePic() {
        def response
        def agentId
        def params = request.getJSON()
        try {
            agentId = params.agentId
            response = userService.deleteProfilePic(agentId)
        } catch (Exception e) {
            log.error("Error occurred while deleting profile picture for agent ${agentId}")
            render status: 500
        }
        parseResponse(response)
    }

    def getProfilePic() {
        log.debug("Going to upload profile picture")
        try {
            def agentId = params?.id
            def response = userService.getProfilePic(agentId)
            if (response)
                render status: OK
            else
                render status: NOT_ACCEPTABLE

        } catch (Exception e) {
            log.error("Error occurred while uploading profile picture.." + e.getMessage())

        }


    }

    def downloadScript(){
        def http = new HTTPBuilder('https://192.168.1.100')
        http.ignoreSSLIssues()
        http.headers.Accept = 'application/json'
        http.headers.Authorization = 'Basic YWRtaW5pc3RyYXRvcjpFeHBlcnRmbG93NDY0'
        try{
            def resp = http.get(path: '/adminapi/script/download//default/Agent1.aef')
            println resp
        }catch (Exception e){
            println e.getMessage()
        }
    }

    def parseResponse(response) {
        if (response?.status)
            render response as JSON
        else
            respond response
    }
}
