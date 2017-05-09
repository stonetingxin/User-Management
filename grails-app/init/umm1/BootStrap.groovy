package umm1

import com.ef.umm.*
import grails.converters.JSON
import groovy.json.JsonBuilder

class BootStrap {

    def init = { servletContext ->
        if(Permission.count() == 0){
            try{
                //permissions related to user controller
                new Permission(name: "com.ef.umm.user.show", expression: "user:show:*").save()
                new Permission(name: "com.ef.umm.user.update", expression: "user:update:*").save()
                new Permission(name: "com.ef.umm.user.create", expression: "user:create:*").save()
                new Permission(name: "com.ef.umm.user.delete", expression: "user:delete:*").save()
                new Permission(name: "com.ef.umm.user.updatePassword", expression: "user:updatePassword:*").save()
                new Permission(name: "com.ef.umm.user.resetPassword", expression: "user:resetPassword:*").save()
                new Permission(name: "com.ef.umm.user.list", expression: "user:list:*").save()
                new Permission(name: "com.ef.umm.user.addRevokeMicroserviceRoles", expression: "user:addRevokeMicroserviceRoles:*").save()

                //permissions related to microservice controller
                new Permission(name: "com.ef.umm.microservice.show", expression: "microservice:show:*").save()
                new Permission(name: "com.ef.umm.microservice.update", expression: "microservice:update:*").save()
                new Permission(name: "com.ef.umm.microservice.create", expression: "microservice:create:*").save()
                new Permission(name: "com.ef.umm.microservice.delete", expression: "microservice:delete:*").save()
                new Permission(name: "com.ef.umm.microservice.list", expression: "microservice:list:*").save()
                new Permission(name: "com.ef.umm.microservice.addRemoveRoles", expression: "microservice:addRemoveRoles:*").save()

                //permissions related to role controller
                new Permission(name: "com.ef.umm.role.show", expression: "role:show:*").save()
                new Permission(name: "com.ef.umm.role.update", expression: "role:update:*").save()
                new Permission(name: "com.ef.umm.role.create", expression: "role:create:*").save()
                new Permission(name: "com.ef.umm.role.delete", expression: "role:delete:*").save()
                new Permission(name: "com.ef.umm.role.list", expression: "role:list:*").save()
                new Permission(name: "com.ef.umm.role.addRevokePermissions", expression: "role:addRevokePermissions:*").save()

                //permissions related to permission controller
                new Permission(name: "com.ef.umm.permission.show", expression: "permission:show:*").save()
                new Permission(name: "com.ef.umm.permission.update", expression: "permission:update:*").save()
                new Permission(name: "com.ef.umm.permission.create", expression: "permission:create:*").save()
                new Permission(name: "com.ef.umm.permission.delete", expression: "permission:delete:*").save()
                new Permission(name: "com.ef.umm.permission.list", expression: "permission:list:*").save()
            }catch (Exception ex){
                log.error("Exception occured in bootstrap while creating permissions: ", ex)
                println ex.getMessage()
            }
        }
        if (Role.count() == 0) {
            try {
                def admin
                def umm
                def roleAdmin = new Role(authority: "ROLE_ADMIN", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.ef.umm.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)

                def role = new Role(authority: "ROLE_USER", description: "User role")
                role?.save(flush: true, failOnError: true)

                if (!User.findByUsername("admin")) {
                    admin = new User(username: "admin", password: "admiN123!")
                    admin?.save(flush: true, failOnError: true)
                }

                if(!Microservice.findByName("UMM")){
                    umm = new Microservice(name: 'UMM', description: 'User Management MicroService')
                    umm.addToRoles(roleAdmin)
                    umm.addToRoles(role)
                    umm?.save(flush: true, failOnError: true)
                }

                UMR.create admin, roleAdmin, umm
                UMR.create admin, role, umm

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
