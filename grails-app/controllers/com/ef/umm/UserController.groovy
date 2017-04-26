package com.ef.umm

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

class UserController extends RestfulController<User> {

    static allowedMethods = [create: "POST", update: "PUT", delete: "DELETE", show: "GET",
                             updatePassword: "PUT", addRevokeMicroserviceRoles: "PUT", changePassword: "PUT",
                             resetPassword: "PUT", usernameExists: "GET"]

    def springSecurityService

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
        def resultSet = [:]
        try{
            def userList = User.list()
            resultSet.put("status", OK)
            resultSet.put("users", userList)
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
            newUser = new User(username: jsonObject?.username, firstName: jsonObject?.firstName,
                               password: jsonObject?.password, lastName: jsonObject?.lastName,
                               email: jsonObject?.email)

            newUser.validate()
            if (newUser.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newUser.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newUser.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "New user: '${newUser.username}' has been created successfully.")
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
            userInstance.firstName = jsonObject?.firstName
            userInstance.lastName = jsonObject?.lastName
            userInstance.email = jsonObject?.email

            userInstance.validate()
            if (userInstance.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", userInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }
            userInstance.save(flush: true, failOnError: true)
            resultSet.put("status", OK)
            resultSet.put("message", "User with username: '${userInstance.username}' has been updated successfully.")
            render resultSet as JSON
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
        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.microservice?.id || !jsonObject?.role?.id || !jsonObject?.addRevoke){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
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

            Microservice micro = Microservice.findById(jsonObject?.microservice?.id as Long)

            if(!micro){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Microservice not found. Provide a valid microservice.")
                response.status = 404
                render resultSet as JSON
                return
            }

            Role role = Role.findById(jsonObject?.role?.id as Long)

            if(!role){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Provide a valid role.")
                response.status = 404
                render resultSet as JSON
                return
            }

            UMR umr = UMR.findByUsersAndMicroservices(userInstance, micro)

            if(addRevoke == 'add'){
                if(umr){
                    if(umr.roles !=role){
                        umr.setRoles(role)
                        umr.save(flush: true, failOnError: true)
                        resultSet.put("status", OK)
                        resultSet.put("message", "Role has been changed for ${userInstance.username} in ${micro.name}")
                        render resultSet as JSON
                        return
                    }
                    else{
                        resultSet.put("status", OK)
                        resultSet.put("message", "${userInstance.username} already has ${role.authority} role for ${micro.name}")
                        render resultSet as JSON
                        return
                    }
                }
                else{
                    umr = UMR.create userInstance, role, micro
                    umr.save(flush:true)
                    resultSet.put("status", OK)
                    resultSet.put("message", "New role: '${role.authority}' has been added for " +
                            "${userInstance.username} in ${micro.name}")
                    render resultSet as JSON
                    return
                }
            }
            else if(addRevoke == 'revoke'){
                if(umr){
                    if(umr.roles !=role){
                        resultSet.put("status", OK)
                        resultSet.put("message", "Role cannot be revoked since it's not been assigned." +
                                " ${userInstance.username} has ${umr?.roles?.authority} role in ${micro.name}")
                        render resultSet as JSON
                        return
                    }
                    else{
                        umr.delete(flush: true, failOnErrors:true)
                        resultSet.put("status", OK)
                        resultSet.put("message", "Role: '${role.authority}' has been revoked for " +
                                "${userInstance.username} in ${micro.name}")
                        render resultSet as JSON
                        return
                    }
                }
                else{
                    resultSet.put("status", OK)
                    resultSet.put("message", "Role cannot be revoked since it's not been assigned." +
                            " ${userInstance.username} does not have any role in ${micro.name}")
                    render resultSet as JSON
                    return
                }
            }
            else{
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Only add or revoke is allowed in this method.")
                response.status = 406
                render resultSet as JSON
                return
            }

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
            log.error("Exception occurred while deleting Instance: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }
}
