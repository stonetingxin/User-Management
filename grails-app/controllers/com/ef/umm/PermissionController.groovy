package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional

class PermissionController {

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
            def permList = Permission.list()
            resultSet.put("status", OK)
            resultSet.put("Permissions", permList)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the list of the permissions.")
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
            def permInstance = Permission.get(params?.id)
            if (!permInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Permission not found.")
                response.status = 404
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("Permission", permInstance)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the permission.")
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
            if(!jsonObject?.name || !jsonObject?.expression ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Permission newPerm = Permission.findByNameOrExpression(jsonObject?.name, jsonObject?.expression)
            if(newPerm){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Permission with name: ${jsonObject?.name} and/or expression: " +
                                          "${jsonObject?.expression} already exists. Kindly provide either " +
                                          "a new name/expression, or call update API.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newPerm = new Permission(name: jsonObject?.name, expression: jsonObject?.expression,
                                     description: jsonObject?.description)

            newPerm.validate()
            if (newPerm.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newPerm.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newPerm.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "New Permission: '${newPerm.name}' has been created successfully.")
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while creating new permission: ", ex)
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
            if(!jsonObject?.id){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Permission permInstance = Permission.findById(jsonObject?.id as Long)
            if(!permInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Permission not found. Invalid update request. " +
                        "For creating new permission, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }

            permInstance.name = jsonObject?.name
            permInstance.expression = jsonObject?.expression
            permInstance.description = jsonObject?.description

            permInstance.validate()
            if (permInstance.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", permInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            permInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "${permInstance.name} has been updated successfully.")
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while updating permission: ", ex)

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
        def permInstance = Permission.findById(params?.id)
        if (!permInstance) {
            resultSet.put("status", NOT_FOUND)
            resultSet.put("message", "Permission not found. Provide a valid permission instance.")
            response.status = 404
            render resultSet as JSON
            return
        }
        try {
            def name = permInstance?.name
            permInstance?.delete(flush: true, failOnErrors:true)
            resultSet.put("status", OK)
            resultSet.put("message", "Successfully deleted permission: ${name}")
            render resultSet as JSON
            return
        }
        catch (Exception ex) {
            log.error("Exception occurred while deleting permission: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
            return
        }
    }
}
