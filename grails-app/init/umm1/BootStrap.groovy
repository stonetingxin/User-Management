package umm1

import com.ef.umm.*
import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.json.JsonBuilder

class BootStrap {
    GrailsApplication grailsApplication
    def init = { servletContext ->
        if (Role.count() == 0) {
            try {

                /*Permissions for Admin Panel Supervisor*/
                def permSupervisor = [
                        // Agent related permissions for Admin Panel
                        [name: "com.ef.efadminpanel.agents.*", expression: "agents:*"],
                        [name: "com.ef.efadminpanel.agent.*", expression: "agent:*"],
                        [name: "com.ef.efadminpanel.skill.*", expression: "skill:*"],
                        [name: "com.ef.efadminpanel.group.*", expression: "group:*"],
                        [name: "com.ef.efadminpanel.team.index", expression: "team:index"],
                        [name: "com.ef.efadminpanel.team.getTeam", expression: "team:getTeam"],
                        [name: "com.ef.efadminpanel.team.update", expression: "team:update"],

                        // Application related permissions in Admin Panel
                        [name: "com.ef.efadminpanel.applications.*", expression: "applications:*"],
                        [name: "com.ef.efadminpanel.application.*", expression: "application:*"],

                        // Permissions related to EasyAnnouncements
                        [name: "com.ef.easyannouncement.easyAnnouncement.*", expression: "easyAnnouncement:*"],
                        [name: "com.ef.easyannouncement.generalAnnouncement.*", expression: "generalAnnouncement:*"],
                        [name: "com.ef.efadminpanel.service.*", expression: "service:*"],
                        [name: "com.ef.efadminpanel.region.*", expression: "region:*"],

                        // Permissions related to business calendar
                        [name: "com.ef.businessCalendar.businessCalendar.*", expression: "businessCalendar:*"],
                        [name: "com.ef.businessCalendar.workingEvents.*", expression: "workingEvents:*"],
                        [name: "com.ef.businessCalendar.holidayProfile.*", expression: "holidayProfile:*"],
                        [name: "com.ef.efadminpanel.agency.*", expression: "agency:*"],
                        [name: "com.ef.efadminpanel.businessCalendarService.*", expression: "businessCalendarService:*"],

                        // Queue related permissions in Admin Panel
                        [name: "com.ef.efadminpanel.queues.*", expression: "queues:*"],
                        [name: "com.ef.efadminpanel.queue.*", expression: "queue:*"],

                        // Permissions related to prompt in Admin Panel
                        [name: "com.ef.efadminpanel.prompts.*", expression: "prompts:*"],
                        [name: "com.ef.efadminpanel.prompt.*", expression: "prompt:*"],

                        // CallerList related permissions for adminpanel
                        [name: "com.ef.efadminpanel.todo.*", expression: "todo:*"],
                        [name: "com.ef.efadminpanel.callerList.*", expression: "callerlist:*"],
                        [name: "com.ef.efadminpanel.caller.*", expression: "caller:*"],

                        // Script related permissions
                        [name: "com.ef.efadminpanel.scripts.*", expression: "scripts:*"],
                        [name: "com.ef.efadminpanel.script.*", expression: "script:*"],
                        [name: "com.ef.efadminpanel.callControlGroup.*", expression: "callControlGroup:*"],
                        [name: "com.ef.efadminpanel.trigger.*", expression: "trigger:*"],

                        // Permission for user validation API in admin panel
                        [name: "com.ef.efadminpanel.user.*", expression: "user:*"],

                        // Permission to perform application settings
                        [name: "com.ef.efadminpanel.applicationSetting.*", expression: "applicationSetting:*"]
                ]

                def permDefaultAP= [
                    // Permission for user validation API in admin panel
                    [name: "com.ef.efadminpanel.user.*", expression: "user:*"],

                    // Permission to perform application settings
                    [name: "com.ef.efadminpanel.applicationSetting.*", expression: "applicationSetting:*"]
                ]

                def admin
                def umm

                // Microservices
                if(!Microservice.findByName("umm")){
                    umm = new Microservice(name: 'umm', ipAddress: "http://127.0.0.1:9090", description: 'User Management MicroService')
                    umm?.save(flush: true, failOnError: true)
                }

                def adminPanel = grailsApplication.config.getProperty('names.adminPanel')
                def efadminpanel = new Microservice(name: adminPanel, ipAddress: "http://127.0.0.1:8080", description: 'Admin panel')
                efadminpanel?.save(flush: true, failOnError: true)

                def ecm = new Microservice(name: 'ecm', ipAddress: "http://192.168.1.92:8080", description: 'ECM')
                ecm?.save(flush: true, failOnError: true)


                // Roles

                def roleAdmin = new Role(authority: "admin", description: "Administrator Role")
                roleAdmin.addToPermissions(name: "com.ef.umm.admin", expression: "*:*")
                roleAdmin?.save(flush: true, failOnError: true)

                def roleSupervisor = new Role(authority: "supervisor", description: "Supervisor Role")

                permSupervisor.each{
                    def perm = new Permission(it)
                    roleSupervisor.addToPermissions(perm)
                    efadminpanel.addToPermissions(perm)
                }

                roleSupervisor?.save(flush: true, failOnError: true)
                efadminpanel?.save(flush: true, failOnError: true)

                def roleJunior = new Role(authority: "junior supervisor", description: "Junior Spervisor Role")
                roleJunior.addToPermissions(Permission.findByExpression("agent:*"))
                roleJunior.addToPermissions(Permission.findByExpression("queue:*"))
                roleSupervisor.addToPermissions(Permission.findByExpression("user:*"))
                roleJunior.addToPermissions(Permission.findByExpression("applicationSetting:*"))
                roleJunior?.save(flush: true, failOnError: true)

                def roleDefaultAP = new Role(authority: "default", description: "Default role for admin panel")
                permDefaultAP.each{
                    def perm = Permission.findByExpression(it?.expression)
                    if(perm){
                        roleDefaultAP.addToPermissions(perm)
                        efadminpanel.addToPermissions(perm)
                    } else {
                        def perm2 = new Permission(it)
                        roleDefaultAP.addToPermissions(perm2)
                        efadminpanel.addToPermissions(perm2)
                    }
                }
                roleDefaultAP.save(flush: true, failOnError: true)

                if (!User.findByUsername("admin")) {
                    admin = new User(username: "admin", fullName: "Administrator", password: "admiN123!", isActive: true, type: "DB",
                            dateCreated: new Date(), lastUpdated: new Date())
                    admin.validate()
                    if (admin.hasErrors()){
                        println admin.errors
                    }
                    admin?.save(flush: true, failOnError: true)
                }


                UMR.create admin, roleAdmin, umm
                UMR.create admin, roleAdmin, efadminpanel
                UMR.create admin, roleDefaultAP, efadminpanel
                UMR.create admin, roleAdmin, ecm

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
            output['permissions'] = it?.permissions
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
            output['profileExists']= it?.profileExists
            output['lastLogin']=it?.lastLogin
            output['lastUpdated'] = it?.lastUpdated
            output['dateCreated'] = it?.dateCreated
            output['createdBy']= it?.createdBy?.id
            output['updatedBy']= it?.updatedBy?.id
            umr = UMR.findAllByUsers(it)

            def uniqueRoles = umr.unique { uniqueRole ->
                uniqueRole.roles.authority
            }

            def arr=[]
            uniqueRoles.each{
                arr.add(it.roles.authority)
            }
            output['roles'] = arr

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
