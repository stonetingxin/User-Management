package com.ef.umm

import grails.converters.JSON
import grails.transaction.Transactional
import static org.springframework.http.HttpStatus.*

class MicroserviceController {

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
            def microList = Microservice.list()
            resultSet.put("status", OK)
            resultSet.put("microservices", microList)
            render resultSet as JSON
        }catch (Exception ex){
            log.error("Couldn't retrieve the list of the microservices.")
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
            def microInstance = Microservice.get(params?.id)
            if (!microInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "icroservice not found. Provide a valid microservice.")
                response.status = 404
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("microservice", microInstance)
            render resultSet as JSON
            return
        }catch (Exception ex){
            log.error("Couldn't retrieve the microservice.")
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

            if(!jsonObject?.name){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Microservice newMicro = Microservice.findByName(jsonObject?.name)
            if(newMicro){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Microservice: ${jsonObject?.name} already exists." +
                              "Kindly provide either a new name, or call update API.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newMicro = new Microservice(name: jsonObject?.name, description: jsonObject?.description)

            newMicro.validate()
            if (newMicro.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", newMicro.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            newMicro.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "New Microservice: '${newMicro.name}' has been created successfully.")
            render resultSet as JSON
            return

        }catch(Exception ex){
            log.error("Exception occured while creating new microservice: ", ex)
            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def update(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.name){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Microservice microInstance = Microservice.findById(jsonObject?.id)
            if(!microInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Microservice not found. Invalid update request. " +
                              "For registering new Microservice, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }
            microInstance.name = jsonObject?.name
            microInstance.description = jsonObject?.description

            microInstance.validate()
            if (microInstance.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", microInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            microInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", "${microInstance.name} has been updated successfully.")
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while updating microservice: ", ex)

            resultSet.put("status", INTERNAL_SERVER_ERROR)
            resultSet.put("message", ex.getMessage())
            response.status = 500
            render resultSet as JSON
        }
    }

    @Transactional
    def addRemoveRoles(){
        def resultSet = [:]
        def message = []
        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.id || !jsonObject?.roles || !jsonObject?.addRemove){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            if(!(jsonObject?.addRemove == 'add' || jsonObject?.addRemove == 'remove')){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Only add or remove is allowed in this method.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Microservice microInstance = Microservice.findById(jsonObject?.id)
            if(!microInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Microservice not found. Invalid update request. " +
                        "For registering new Microservice, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }

            def addRemove = jsonObject?.addRemove
            def roles = jsonObject?.roles

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
                    if(addRemove == 'add'){
                        if(!microInstance.roles.contains(role)){
                            microInstance.addToRoles(role)
                            message.add("Role: ${role.authority} has been successfully added.")
                        }
                        else{
                            message.add("Role: ${role.authority} has already been assigned in the microservice.")
                        }
                    }
                    if(addRemove == 'remove'){
                        if(microInstance.roles.contains(role)){
                            microInstance.removeFromRoles(role)
                            message.add("Role: ${role.authority} has been successfully removed.")
                        }
                        else{
                            message.add("Role: ${role.authority} cannot be removed since it's not been assigned.")
                        }
                    }
                }
                else{
                    message.add("Role with id: ${it?.id} not found.")
                }
            }

            microInstance.validate()
            if (microInstance.hasErrors()) {
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("error", microInstance.errors)
                response.status = 406
                render resultSet as JSON
                return
            }

            microInstance.save(flush: true, failOnError: true)

            resultSet.put("status", OK)
            resultSet.put("message", message)
            render resultSet as JSON
            return

        }catch (Exception ex){
            log.error("Exception occured while add/remove roles in microservice: ", ex)
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
            def microInstance = Microservice.findById(params?.id)
            if (!microInstance) {
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "MicroService not found. Provide a valid microservice instance.")
                response.status = 404
                render resultSet as JSON
                return
            }
            def name = microInstance.name
            def umr = UMR.findAllByMicroservices(microInstance)
            umr.each{
                it?.delete(flush: true, failOnErrors:true)
            }

            microInstance?.delete(flush: true, failOnErrors:true)
            resultSet.put("status", OK)
            resultSet.put("message", "Successfully deleted microservice: ${name}")
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
