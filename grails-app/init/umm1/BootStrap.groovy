package umm1

import com.ef.umm.*
import grails.converters.JSON
import groovy.json.JsonBuilder

class BootStrap {

    def init = { servletContext ->
        if (Role.count() == 0) {
            try {
                def ahmed = new User(username: "ahmed", password: "admin", authProvider: "local")
                def hamid = new User(username: "hamid", password: "nothing", authProvider: "external")
                ahmed?.save(flush: true, failOnError: true)
                hamid?.save(flush: true, failOnError: true)

                def admin = new Role(authority: "ROLE_ADMIN", description: "Administrator Role")
                admin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
                admin?.save(flush: true, failOnError: true)

                def role = new Role(authority: "ROLE_USER", description: "User role")
                role.addToPermissions(name: "com.app.ef.show", expression: "show:*")
                role.addToPermissions(name: "com.app.ef.update", expression: "update:*")
                role?.save(flush: true, failOnError: true)

                def umm = new Microservice(name: 'UMM', description: 'User Management MicroService')
                def pcs = new Microservice(name: 'PCS', description: 'Post Call Survey')
                def cbr = new Microservice(name: 'CBR', description: 'Caller Based Routing')
                pcs.addToRoles(admin)
                pcs.addToRoles(role)
                pcs?.save(flush: true, failOnError: true)
                cbr.addToRoles(admin)
                cbr.addToRoles(role)
                cbr?.save(flush: true, failOnError: true)
                umm.addToRoles(admin)
                umm.addToRoles(role)
                umm?.save(flush: true, failOnError: true)



                UMR.create ahmed, admin, pcs
                UMR.create ahmed, role, cbr
                UMR.create hamid, admin, cbr
                UMR.create ahmed, admin, umm

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
                    role value?.roles

                }
                micro.add(map)
            }

            output['microServices'] = micro
            return output
        }
    }
    def destroy = {
    }
}
