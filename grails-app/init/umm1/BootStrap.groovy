package umm1

import com.ef.umm.*
import grails.converters.JSON
import groovy.json.JsonBuilder

class BootStrap {

    def init = { servletContext ->
        if (Role.count() == 0) {
            try {
                def admin
                def umm
                def roleAdmin = new Role(authority: "ROLE_ADMIN", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.ef.umm.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)


                if (!User.findByUsername("admin")) {
                    admin = new User(username: "admin", password: "admiN123!")
                    admin?.save(flush: true, failOnError: true)
                }

                if(!Microservice.findByName("umm")){
                    umm = new Microservice(name: 'umm', description: 'User Management MicroService')
                    umm.addToRoles(roleAdmin)
                    umm?.save(flush: true, failOnError: true)
                }

                UMR.create admin, roleAdmin, umm

                UMR.withSession {
                    it.flush()
                    it.clear()
                }
            }
            catch (Exception ex){
                log.error("Exception occured in bootstrap while creating permissions: ", ex)
                println ex.getMessage()
            }

        }

        JSON.registerObjectMarshaller(Permission){
            def output = [:]
            output['id'] = it?.id
            output['name'] = it?.name
            output['expression'] = it?.expression
            output['description'] = it?.description
            return output
        }

        JSON.registerObjectMarshaller(Role){
            def output = [:]
            output['id'] = it?.id
            output['name'] = it?.authority
            output['description'] = it?.description
            output['permissions'] = it?.permissions
            return output
        }

        JSON.registerObjectMarshaller(Microservice){
            def output = [:]
            output['id'] = it?.id
            output['name'] = it?.name
            output['description'] = it?.description
            output['roles'] = it?.roles
            return output
        }

        JSON.registerObjectMarshaller(User) {
            def output = [:]
            def umr
            def umr1
            def micro = []
            JsonBuilder json = new JsonBuilder()

            output['id'] = it?.id
            output['username'] = it?.username
            output['email'] = it?.email
            output['firstName'] = it?.firstName
            output['lastName'] = it?.lastName

            umr = UMR.findAllByUsers(it)

            def uniqueUmr = umr.unique { uniqueMicro ->
                uniqueMicro.microservices
            }
            uniqueUmr.each{value ->
                umr1=UMR.findAllByUsersAndMicroservices(it, value?.microservices)
                def map = json {
                    id value?.microservices?.id
                    name value?.microservices?.name
                    description value?.microservices?.description
                    roles umr1*.roles
                }
                micro.add(map)
            }

            output['microservices'] = micro
            return output
        }
    }
    def destroy = {
    }
}
