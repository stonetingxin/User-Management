package umm1

import com.ef.umm.*
import grails.converters.JSON
import groovy.json.JsonBuilder

class BootStrap {

    def init = { servletContext ->
        if (Role.count() == 0) {
            try {
                //permissions related to user controller
                new Permission(name: "com.ef.umm.user.show", expression: "user:show:*")
                new Permission(name: "com.ef.umm.user.update", expression: "user:update:*")
                new Permission(name: "com.ef.umm.user.create", expression: "user:create:*")
                new Permission(name: "com.ef.umm.user.delete", expression: "user:delete:*")
                new Permission(name: "com.ef.umm.user.updatePassword", expression: "user:updatePassword:*")
                new Permission(name: "com.ef.umm.user.resetPassword", expression: "user:resetPassword:*")
                new Permission(name: "com.ef.umm.user.list", expression: "user:list:*")
                new Permission(name: "com.ef.umm.user.addRevokeMicroserviceRoles", expression: "user:addRevokeMicroserviceRoles:*")

                //permissions related to microservice controller
                new Permission(name: "com.ef.umm.microservice.show", expression: "microservice:show:*")
                new Permission(name: "com.ef.umm.microservice.update", expression: "microservice:update:*")
                new Permission(name: "com.ef.umm.microservice.create", expression: "microservice:create:*")
                new Permission(name: "com.ef.umm.microservice.delete", expression: "microservice:delete:*")
                new Permission(name: "com.ef.umm.microservice.list", expression: "microservice:list:*")
                new Permission(name: "com.ef.umm.microservice.addRemoveRoles", expression: "microservice:addRemoveRoles:*")

                //permissions related to role controller
                new Permission(name: "com.ef.umm.role.show", expression: "role:show:*")
                new Permission(name: "com.ef.umm.role.update", expression: "role:update:*")
                new Permission(name: "com.ef.umm.role.create", expression: "role:create:*")
                new Permission(name: "com.ef.umm.role.delete", expression: "role:delete:*")
                new Permission(name: "com.ef.umm.role.list", expression: "role:list:*")
                new Permission(name: "com.ef.umm.role.addRevokePermissions", expression: "role:addRevokePermissions:*")

                //permissions related to permission controller
                new Permission(name: "com.ef.umm.permission.show", expression: "permission:show:*")
                new Permission(name: "com.ef.umm.permission.update", expression: "permission:update:*")
                new Permission(name: "com.ef.umm.permission.create", expression: "permission:create:*")
                new Permission(name: "com.ef.umm.permission.delete", expression: "permission:delete:*")
                new Permission(name: "com.ef.umm.permission.list", expression: "permission:list:*")

                
                def admin = new User(username: "admin", password: "admin")
                def hamid = new User(username: "hamid", password: "nothing")
                admin?.save(flush: true, failOnError: true)
                hamid?.save(flush: true, failOnError: true)

                def roleAdmin = new Role(authority: "ROLE_ADMIN", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.ef.umm.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)

                def role = new Role(authority: "ROLE_USER", description: "User role")
                role.addToPermissions(name: "com.ef.umm.user.show", expression: "user:show:*")
                role.addToPermissions(name: "com.ef.umm.user.update", expression: "user:update:*")
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
