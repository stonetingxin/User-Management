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
                def roleAdmin = new Role(authority: "admin", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.ef.umm.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)

                def roleSupervisor = new Role(authority: "supervisor", description: "Spervisor Role")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.agent.*", expression: "agents:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.agent.*", expression: "agent:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.skill.*", expression: "skill:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.group.*", expression: "group:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.team.index", expression: "team:index")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.team.getTeam", expression: "team:getTeam")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.team.update", expression: "team:update")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.applications.*", expression: "applications:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.application.*", expression: "application:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.easyAnnouncement.*", expression: "easyAnnouncement:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.generalAnnouncement.*", expression: "generalAnnouncement:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.serviceStatus.*", expression: "serviceStatus:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.service.*", expression: "service:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.region.*", expression: "region:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.businessCalendar.*", expression: "businessCalendar:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.workingEvents.*", expression: "workingEvents:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.holidayProfile.*", expression: "holidayProfile:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.agency.*", expression: "agency:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.businessCalendarService.*", expression: "businessCalendarService:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.queues.*", expression: "queues:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.queue.*", expression: "queue:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.prompts.*", expression: "prompts:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.prompt.*", expression: "prompt:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.todo.*", expression: "todo:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.callerList.*", expression: "callerlist:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.caller.*", expression: "caller:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.scripts.*", expression: "scripts:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.script.*", expression: "script:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.callControlGroup.*", expression: "callControlGroup:*")
                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.trigger.*", expression: "trigger:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.user.*", expression: "user:*")

                roleSupervisor.addToPermissions(name: "com.ef.efadminpanel.applicationSetting.*", expression: "applicationSetting:*")
                roleSupervisor?.save(flush: true, failOnError: true)

                def roleJunior = new Role(authority: "junior supervisor", description: "Junior Spervisor Role")
                roleJunior.addToPermissions(Permission.findByExpression("agent:*"))
                roleJunior.addToPermissions(Permission.findByExpression("queue:*"))
                roleSupervisor.addToPermissions(Permission.findByExpression("user:*"))
                roleJunior.addToPermissions(Permission.findByExpression("applicationSetting:*"))
                roleJunior?.save(flush: true, failOnError: true)

                def roleUser = new Role(authority: "ROLE_USER", description: "User Role")
                roleUser.addToPermissions(name: "com.ef.efadminpanel.businessCalendar.list", expression: "businessCalendar:list")
                roleUser?.save(flush: true, failOnError: true)

                if (!User.findByUsername("admin")) {
                    admin = new User(username: "admin", password: "admiN123!", isActive: true, type: "DB")
                    admin.validate()
                    if (admin.hasErrors()){
                        println admin.errors
                    }
                    admin?.save(flush: true, failOnError: true)
                }


                if(!Microservice.findByName("umm")){
                    umm = new Microservice(name: 'umm', ipAddress: "http://127.0.0.1:9090", description: 'User Management MicroService')
                    umm.addToRoles(roleAdmin)
                    umm?.save(flush: true, failOnError: true)
                }

                def efadminpanel = new Microservice(name: 'efadminpanel', ipAddress: "http://127.0.0.1:8080", description: 'Admin panel')
                efadminpanel.addToRoles(roleAdmin)
                efadminpanel?.save(flush: true, failOnError: true)

                def ecm = new Microservice(name: 'ecm', ipAddress: "http://192.168.1.92:8080", description: 'ECM')
                ecm.addToRoles(roleAdmin)
                ecm.addToRoles(roleUser)
                ecm?.save(flush: true, failOnError: true)

                UMR.create admin, roleAdmin, umm
                UMR.create admin, roleSupervisor, umm
                UMR.create admin, roleUser, umm
                UMR.create admin, roleUser, efadminpanel
                UMR.create admin, roleAdmin, efadminpanel
                UMR.create admin, roleSupervisor, efadminpanel


                UMR.withSession {
                    it.flush()
                    it.clear()
                }
            }
            catch (Exception ex){
                log.error("Exception occured in bootstrap while initiating database: ", ex)
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
            output['ipAddress'] = it?.ipAddress
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
            output['fullName'] = it?.fullName
            output['type'] = it?.type
            output['isActive'] = it?.isActive
            umr = UMR.findAllByUsers(it)

            def uniqueUmr = umr.unique { uniqueMicro ->
                uniqueMicro.microservices
            }
            uniqueUmr.each{value ->
                umr1=UMR.findAllByUsersAndMicroservices(it, value?.microservices)
                def map = json {
                    id value?.microservices?.id
                    name value?.microservices?.name
                    ipAddress value?.microservices?.ipAddress
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
