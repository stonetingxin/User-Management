package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional

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
            resultSet.put("Roles", roleList)
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
            resultSet.put("Role", roleInstance)
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
                              "Kindly provide either a new name, or call update API.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newRole = new Role(authority: jsonObject?.authority)
            def perms = jsonObject?.permissions
            if(!jsonObject?.permissions ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }
            def perm
            perms?.each{
                if(!it?.expression || !it?.name){
                    resultSet.put("status", NOT_ACCEPTABLE)
                    resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                    response.status = 406
                    render resultSet as JSON
                    return
                }
                perm = Permission.findByExpressionOrName(it?.expression, it?.name)
                if(!perm){
                    perm = new Permission(expression: it?.expression, name: it?.name)
                    newRole.addToPermissions(perm)
                }
                else{
                    newRole.addToPermissions(perm)
                }

            }

            newRole.validate()
            if (newRole.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newRole.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newRole.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "New Role: '${newRole.authority}' has been created with " +
                    "permissions: ${newRole.permissions}")
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while creating new user: ", ex)
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

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || jsonObject?.addRevoke != 'add' || jsonObject?.addRevoke != 'revoke'){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
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
            if(!jsonObject?.permissions ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

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
                if(!perm){
                    resultSet.put("status", NOT_ACCEPTABLE)
                    resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                    response.status = 406
                    render resultSet as JSON
                    return
                }
                if(!Role.findByPermissions(perm) && addRevoke == 'add')
                    roleInstance.addToPermissions(perm)

                if(Role.findByPermissions(perm) && addRevoke == 'revoke')
                    roleInstance.removeFromPermissions(perm)
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
            resultSet.put("message", "${roleInstance.authority} has following permission(s):" +
                                     " ${roleInstance.permissions}")
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
            if(!jsonObject?.authority || !jsonObject?.id ){
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
            resultSet.put("message", "${roleInstance.authority} has been updated successfully.")
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
        def roleInstance = Role.findById(params?.id)
        if (!roleInstance) {
            resultSet.put("status", NOT_FOUND)
            resultSet.put("message", "Role not found. Provide a valid role instance.")
            response.status = 404
            render resultSet as JSON
            return
        }
        try {

            def umr = UMR.findAllByRoles(roleInstance)
            if(umr)
            {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Cannot delete the role. Role is assigned to following user(s):")
                resultSet.put("Users", umr*.toString())
                response.status = 406
                render resultSet as JSON
                return
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
}
