package umm1

import com.ef.umm.*
import grails.converters.JSON
import groovy.json.JsonBuilder

class BootStrap {

    def init = { servletContext ->
        if (Role.count() == 0) {
            try {
                def admin = new User(username: "admin", password: "admin")
                def hamid = new User(username: "hamid", password: "nothing")
                admin?.save(flush: true, failOnError: true)
                hamid?.save(flush: true, failOnError: true)

                def roleAdmin = new Role(authority: "ROLE_ADMIN", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)

                def role = new Role(authority: "ROLE_USER", description: "User role")
                role.addToPermissions(name: "com.app.ef.show", expression: "user:show")
                role.addToPermissions(name: "com.app.ef.update", expression: "user:update")
                role?.save(flush: true, failOnError: true)

                def umm = new Microservice(name: 'UMM', description: 'User Management MicroService')
                def pcs = new Microservice(name: 'PCS', description: 'Post Call Survey')
                def cbr = new Microservice(name: 'CBR', description: 'Caller Based Routing')
                pcs.addToRoles(roleAdmin)
                pcs.addToRoles(role)
                pcs?.save(flush: true, failOnError: true)
                cbr.addToRoles(roleAdmin)
                cbr.addToRoles(role)
                cbr?.save(flush: true, failOnError: true)
                umm.addToRoles(roleAdmin)
                umm.addToRoles(role)
                umm?.save(flush: true, failOnError: true)



                UMR.create admin, roleAdmin, pcs
                UMR.create admin, role, cbr
                UMR.create hamid, roleAdmin, cbr
                UMR.create admin, roleAdmin, umm

                UMR.withSession {
                    it.flush()
                    it.clear()
                }
            }
            catch (Exception ex){
                println ex.getMessage()
            }

        }

        JSON.registerObjectMarshaller(Permission){
            def output = [:]
            output['id'] = it?.id
            output['name'] = it?.name
            output['expression'] = it?.expression
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
            def micro = []
            JsonBuilder json = new JsonBuilder()

            output['id'] = it?.id
            output['username'] = it?.username
            output['email'] = it?.email
            output['firstName'] = it?.firstName
            output['lastName'] = it?.lastName

            umr = UMR.findAllByUsers(it)
            umr.eachWithIndex{value, index ->
                def map = json {
                    id value?.microservices?.id
                    name value?.microservices?.name
                    description value?.microservices?.description
                    roles value?.roles

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
