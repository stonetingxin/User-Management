package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonBuilder

import static org.springframework.http.HttpStatus.*

class RoleController {

    static allowedMethods = [create: "POST", update: "PUT", delete: "DELETE", show: "GET"]

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
            def roleList = Role.list()
            resultSet.put("status", OK)
            resultSet.put("roles", roleList)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the list of the roles.")
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
            def roleInstance = Role.get(params?.id)
            if (!roleInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found.")
                response.status = 404
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("role", roleInstance)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the role.")
            log.error("Following is the stack trace along with the error message: ", ex)
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
            if(!jsonObject?.authority ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Role newRole = Role.findByAuthority(jsonObject?.authority)
            if(newRole){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Role with name: ${jsonObject?.authority} already exists." +
                              " Kindly provide either a new name, or call update API.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newRole = new Role(authority: jsonObject?.authority, description: jsonObject?.description)

            newRole.validate()
            if (newRole.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newRole.errors)
                response.status = 406
                render resultSet as JSON
                return
            }
            def permDef = Permission.findByExpression("default:*")

            newRole.save(flush: true, failOnError: true)
            newRole.addToPermissions(permDef)

            resultSet.put("status", OK)
            resultSet.put("message", "New Role: '${newRole.authority}' has been created successfully. ")
            resultSet.put("role", newRole)
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while creating new role: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
            return
        }
    }

    @Transactional
    def addRevokePermissions(){
        def resultSet = [:]
        def message = []
        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.addRevoke || !jsonObject?.permissions){
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
            Role roleInstance = Role.findById(jsonObject?.id as Long)
            if(!roleInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Invalid update request. " +
                                         "For creating new role, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }

            def perms = jsonObject?.permissions

            def perm
            perms?.each{
                if(!it?.id){
                    resultSet.put("status", NOT_ACCEPTABLE)
                    resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                    response.status = 406
                    render resultSet as JSON
                    return
                }
                perm = Permission.findById(it?.id as Long)
                if(perm){
                    if(addRevoke == 'add'){
                        if(!roleInstance?.permissions?.contains(perm)){
                            roleInstance.addToPermissions(perm)
                            def umr = UMR.findAllByRoles(roleInstance)
                            umr.each {
                                def user = it.users
                                def umrFull = UMR.findByUsersAndMicroservicesAndRoles(user, perm.micro, roleInstance)
                                if(!umrFull){
                                    UMR.create(user, roleInstance, perm.micro, true)
                                }
                            }
                            message.add("Permission: ${perm.name} has been successfully added.")
                        }
                        else{
                            message.add("Permission: ${perm.name} has already been added in the role.")
                        }
                    }

                    if(addRevoke == 'revoke'){
                        if(roleInstance.authority == "admin" && perm.expression == "*:*"){
                            resultSet.put("status", NOT_ACCEPTABLE)
                            resultSet.put("message", "Super user permissions cannot be revoked from admin role.")
                            response.status = 406
                            render resultSet as JSON
                            return
                        }
                        if(roleInstance?.permissions?.contains(perm)){
                            roleInstance.removeFromPermissions(perm)
                            def umr = UMR.findAllByRoles(roleInstance)
                            umr.each {
                                def user = it.users
                                def umrFull = UMR.findByUsersAndMicroservicesAndRoles(user, perm.micro, roleInstance)
                                if(umrFull && !roleInstance.permissions*.micro.contains(perm.micro)){
                                    umrFull.delete(flush:true, failOnError: true)
                                }
                            }
                            message.add("Permission: ${perm.name} has been successfully revoked.")
                        }
                        else{
                            message.add("Permission: ${perm.name} cannot be revoked since it's not assigned to the role.")
                        }
                    }
                }
                else{
                    message.add("Permission with id: ${it?.id} not found.")
                }

            }

            roleInstance.validate()
            if (roleInstance.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", roleInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            roleInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", message)
            resultSet.put("role", roleInstance)
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while adding permissions: ", ex)

            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
            return
        }
    }

    @Transactional
    def update(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.authority){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Role roleInstance = Role.findById(jsonObject?.id as Long)
            if(!roleInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Invalid update request. " +
                              "For creating new role, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }

            roleInstance.authority = jsonObject?.authority
            roleInstance.description = jsonObject?.description
            roleInstance.validate()
            if (roleInstance.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", roleInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            roleInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "Role has been updated successfully.")
            resultSet.put("role", roleInstance)
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while updating role: ", ex)

            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
            return
        }
    }

    @Transactional
    def delete(){
        def resultSet = [:]

        try {
            def roleInstance = Role.findById(params?.id)
            if (!roleInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found. Provide a valid role instance.")
                response.status = 404
                render resultSet as JSON
                return
            }
            if(roleInstance.authority == "admin"){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Cannot delete the admin role.")
                response.status = 406
                render resultSet as JSON
                return
            }
            def umr = UMR.findAllByRoles(roleInstance)
//            if(umr){
//                resultSet.put("status", NOT_ACCEPTABLE)
//                resultSet.put("message", "Cannot delete the role. Role is assigned to following user(s):")
//                resultSet.put("Users", umr*.toString())
//                response.status = 406
//                render resultSet as JSON
//                return
//            }
            if(umr){
                umr.each {
                    it.delete()
                }
            }

            def auth = roleInstance?.authority
            roleInstance?.delete(flush: true, failOnErrors:true)
            resultSet.put("status", OK)
            resultSet.put("message", "Successfully deleted role: ${auth}")
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while deleting role: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
            return
        }
    }

    def roleUser(){
        def resultSet = [:]
        def userArr = []
        try{
            def roleInstance = Role.get(params?.id)
            if (!roleInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Role not found.")
                response.status = 404
                render resultSet as JSON
                return
            }
            def users = UMR.findAllByRoles(roleInstance).users.unique{user-> user.id}
            resultSet.put("status", OK)
            JsonBuilder json = new JsonBuilder()
            users.each{value ->
                def map = json {
                    id value?.id
//                    username value?.username
//                    email value?.email
//                    fullName value?.fullName
//                    type value?.type
//                    isActive value?.isActive
//                    profileExists value?.profileExists
//                    lastLogin value?.lastLogin
//                    lastUpdated value?.lastUpdated
//                    dateCreated value?.dateCreated
//                    createdBy value?.createdBy?.id
//                    updatedBy value?.updatedBy?.id
                }
                userArr.add(map)
            }
            resultSet.put("users", userArr)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the role.")
            log.error("Following is the stack trace along with the error message: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def userAssign(){
        def resultSet = [:]
        def jsonObject = request.getJSON()
        def message= []
        try {

            def roleInstance = Role.findById(jsonObject?.role)
            def userInstance = User.findById(jsonObject?.user)
            if (!roleInstance) {
                message.add("Role with ID: ${jsonObject?.role} not found. Provide a valid role id.")
                response.status = 406
                resultSet.put("status", NOT_ACCEPTABLE)
            } else{
                if (!userInstance) {
                    message.add("User with ID: ${jsonObject?.user} not found. Provide a valid user id.")
                    response.status = 406
                    resultSet.put("status", NOT_ACCEPTABLE)
                }else{
                    jsonObject.microservices.each{
                        def micro = Microservice.findById(it?.id)
                        if(!micro){
                            message.add("Microservice with ID: ${it?.id} not found. Provide a valid microservice id.")
                            response.status = 406
                            resultSet.put("status", NOT_ACCEPTABLE)
                        }else {
                            if(jsonObject?.addRevoke == "add"){
                                UMR.create userInstance, roleInstance, micro, true
                                message.add("Successfully added ${roleInstance.authority} role for user: ${userInstance.username}")
                                resultSet.put("status", OK)
                                response.status = 200
                            } else if(jsonObject?.addRevoke == "revoke"){
                                def umr = UMR.findByUsersAndMicroservicesAndRoles(userInstance,micro, roleInstance)
                                if(umr){
                                    umr.delete(flush:true,failOnErrors:true)
                                    message.add("Successfully revoked ${roleInstance.authority} role for user: ${userInstance.username}")
                                    resultSet.put("status", OK)
                                    response.status = 200
                                } else {
                                    message.add("${roleInstance.authority} role has not assigned to user: ${userInstance.username}")
                                    response.status = 406
                                    resultSet.put("status", NOT_ACCEPTABLE)
                                }
                            }

                        }
                    }
                }
            }


            resultSet.put("message", message)
            resultSet.put("user", userInstance)
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while assigning users to the role: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }
    @Transactional
    def deleteMulti(){
        def resultSet = [:]
        def jsonObject = request.getJSON()
        def message = []
        try {
            jsonObject.each{
                def roleInstance = Role.findById(it?.id)
                if (!roleInstance) {
                    message.add("Role with ID: ${it?.id} not found. Provide a valid role id.")
                } else{
                    if(roleInstance.authority == "admin"){
                        message.add("Role: ${roleInstance.authority} cannot be deleted. Permission denied.")
                    } else {
                        def umr = UMR.findAllByRoles(roleInstance)
                        if(umr){
                            message.add("Cannot delete role: ${roleInstance.authority}. It is assigned to following user(s): ${umr*.toString()}")
                        } else{
                            def role = roleInstance.authority
                            roleInstance?.delete(flush: true, failOnErrors:true)
                            message.add("Successfully deleted role: ${role}")
                        }
                    }
                }

            }

            resultSet.put("status", OK)
            resultSet.put("message", message)
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while deleting multiple role instances: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }
}
