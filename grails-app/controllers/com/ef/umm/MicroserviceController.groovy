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
            resultSet.put("Microservices", microList)
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
                resultSet.put("message", "Microservice not found. Provide a valid microservice.")
                response.status = 404
                render resultSet as JSON
                return
            }

            resultSet.put("status", OK)
            resultSet.put("Microservice", microInstance)
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
            Microservice newMicro = Microservice.findByName(jsonObject?.name)
            if(newMicro){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Microservice: ${jsonObject?.name} already exists." +
                              "Kindly provide either a new name, or call update API.")
                response.status = 406
                render resultSet as JSON
                return
            }
            newMicro = new Microservice(name: jsonObject?.name)
            def roles = jsonObject?.roles
            def role
            roles?.each{
                role = Role.findByAuthority(it?.authority)
                if(!role){
                    role = new Role(authority: it?.authority)
                }

                newMicro.addToRoles(role)
            }

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
            resultSet.put("message", "New Microservice: '${newMicro.name}' has been created with " +
                    "Roles: ${newMicro.roles}")
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
    def update(){
        def resultSet = [:]

        try{
            def jsonObject = request.getJSON()
            if(!jsonObject?.name ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            Microservice microInstance = Role.findByAuthority(jsonObject?.name)
            if(!microInstance){
                resultSet.put("status", NOT_FOUND)
                resultSet.put("message", "Microservice not found. Invalid update request. " +
                              "For registering new Microservice, use create API instead.")
                response.status = 404
                render resultSet as JSON
                return
            }

            def roles = jsonObject?.roles
            if(!jsonObject?.roles ){
                resultSet.put("status", NOT_ACCEPTABLE)
                resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                response.status = 406
                render resultSet as JSON
                return
            }

            def role
            roles?.each{
                if(!it?.authority){
                    resultSet.put("status", NOT_ACCEPTABLE)
                    resultSet.put("message", "Invalid JSON provided. Please read the API specifications.")
                    response.status = 406
                    render resultSet as JSON
                    return
                }

                role = Role.findByAuthority(it?.authority)
                if(!role) {
                    microInstance.addToRoles(authority: it?.authority, description: it?.description)
                }
                else{
                    microInstance.addToRoles(role)
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
            resultSet.put("message", "${microInstance.name} has been updated with role: ${microInstance.roles}")
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
        def microInstance = Microservice.findById(params?.id)
        if (!microInstance) {
            resultSet.put("status", NOT_FOUND)
            resultSet.put("message", "MicroService not found. Provide a valid microservice instance.")
            response.status = 404
            render resultSet as JSON
            return
        }

        try {
            def name = microInstance.name
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
