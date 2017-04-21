package com.ef.umm

import grails.converters.JSON
import grails.rest.RestfulController
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.*

class UserController extends RestfulController<User> {

    static allowedMethods = [create: "POST", update: "PUT", delete: "DELETE", show: "GET", updatePassword: "PUT"]

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
            resultSet.put("Users", userList)
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
            resultSet.put("User", userInstance)
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
    def updatePassword(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.username || !jsonObject?.curPassword || !jsonObject?.newPassword){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            User userInstance = User.findByUsername(jsonObject?.username)
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
            if(!jsonObject?.username || !jsonObject?.microservice || !jsonObject?.role ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }
            User newUser = User.findByUsername(jsonObject?.username)
            if(newUser){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "User with username: ${jsonObject?.username} already exists.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newUser = new User(username: jsonObject?.username, firstName: jsonObject?.firstName,
                               password: jsonObject?.password, lastName: jsonObject?.lastName,
                               authProvider: jsonObject?.authProvider)

            newUser.validate()
            if (newUser.hasErrors()){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newUser.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newUser.save(flush: true, failOnError: true)
            Microservice micro = Microservice.findByName(jsonObject?.microservice?.name)
            if(!micro){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "MicroService not found. Provide a valid microservice.")
                response.status = 404
                render resultSet as JSON
                return
            }

            Role role = Role.findByAuthority(jsonObject?.role?.name)
            if(!role){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Provide a valid role.")
                response.status = 404
                render resultSet as JSON
                return
            }

            UMR.create newUser, role, micro, true
            resultSet.put("status", OK)
            resultSet.put("message", "New user: '${newUser.username}' has been created as " +
                          "${role.authority} in ${micro.name}")
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
            if(!jsonObject?.username || !jsonObject?.microservice || !jsonObject?.role ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }
            User userInstance = User.findByUsername(jsonObject?.username)
            println userInstance
            if(!userInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "User not found. Invalid update request")
                response.status = 404
                render resultSet as JSON
                return
            }

            Microservice micro = Microservice.findByName(jsonObject?.microservice)

            if(!micro){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "MicroService not found. Provide a valid microservice.")
                response.status = 404
                render resultSet as JSON
                return
            }

            Role role = Role.findByAuthority(jsonObject?.role)

            if(!role){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Provide a valid role.")
                response.status = 404
                render resultSet as JSON
                return
            }

            UMR umr = UMR.findByUsersAndMicroservices(userInstance, micro)

            if(!umr){
                umr = UMR.create userInstance, role, micro
                umr.save(flush:true)
                resultSet.put("status", OK)
                resultSet.put("message", "New role: '${jsonObject?.role}' has been added for " +
                              "${userInstance.username} in ${micro.name}")
                render resultSet as JSON
                return
            }

            if(umr.roles != role){
                umr.setRoles(role)
                umr.save(flush: true)
                resultSet.put("status", OK)
                resultSet.put("message", "Role has been changed for ${userInstance.username} in ${micro.name}")
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("message", "${userInstance.username} already has ${jsonObject?.role} role for ${micro.name}")
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
    def delete(){
        def resultSet = [:]
        def userInstance = User.findById(params?.id)
        if (!userInstance) {
            resultSet.put("status", NOT_FOUND)
            resultSet.put("message", "User not found. Provide a valid user instance.")
            response.status = 404
            render resultSet as JSON
            return
        }
        try {
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
