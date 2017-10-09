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
                        [name: "Agent(Side Menu)", expression: "agents:*"],
                        [name: "Full Access: Agent", expression: "agent:*"],
                        [name: "Full Access: Skill", expression: "skill:*"],
                        [name: "Full Access: Group", expression: "group:*"],
                        [name: "List Access: Team", expression: "team:index"],
                        [name: "GetTeam Access: Team", expression: "team:getTeam"],
                        [name: "Update Access: Team", expression: "team:update"],

                        // Application related permissions in Admin Panel
                        [name: "Application(Side Menu)", expression: "applications:*"],
                        [name: "Full Access: Application", expression: "application:*"],

                        // Permissions related to EasyAnnouncements
//                        [name: "Full Access: Easy Announcement", expression: "easyAnnouncement:*"],
//                        [name: "Full Access: General Announcement", expression: "generalAnnouncement:*"],
//                        [name: "Full Access: Service", expression: "service:*"],
//                        [name: "Full Access: Region", expression: "region:*"],

                        // Permissions related to business calendar
//                        [name: "Full Access: Business Calendar", expression: "businessCalendar:*"],
//                        [name: "Full Access: Working Events(BC)", expression: "workingEvents:*"],
//                        [name: "Full Access: Holiday Profile(BC)", expression: "holidayProfile:*"],
//                        [name: "Full Access: Agency(BC)", expression: "agency:*"],
//                        [name: "Full Access: BC Service", expression: "businessCalendarService:*"],

                        // Queue related permissions in Admin Panel
                        [name: "Queue(Side Menu)", expression: "queues:*"],
                        [name: "Full Access: Queue", expression: "queue:*"],

                        // Permissions related to prompt in Admin Panel
                        [name: "Prompts(Side Menu)", expression: "prompts:*"],
                        [name: "Full Access: Prompt", expression: "prompt:*"],

                        // CallerList related permissions for adminpanel
//                        [name: "Full Access: Caller List", expression: "todo:*"],
//                        [name: "CallerList(Side Menu)", expression: "callerlist:*"],
//                        [name: "Full Access: Caller", expression: "caller:*"],

                        // Script related permissions
                        [name: "Scripts(Side Menu)", expression: "scripts:*"],
                        [name: "Full Access: Script", expression: "script:*"],
                        [name: "Full Access: callControlGroup", expression: "callControlGroup:*"],
                        [name: "Full Access: Trigger", expression: "trigger:*"],

                        // Permission for user validation API in admin panel
                        [name: "Full Access: User", expression: "user:*"],

                        // Permission to perform application settings
                        [name: "Full Access: Application Setting", expression: "applicationSetting:*"]
                ]

                def permDefaultAP= [
                    // Permission for user validation API in admin panel
                    [name: "Full Access: User", expression: "user:*"],

                    // Permission to perform application settings
                    [name: "Full Access: Application Setting", expression: "applicationSetting:*"]
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

                def permAdmin = new Permission(name: "Super User Access(Admin)", expression: "*:*")
                def roleAdmin = new Role(authority: "admin", description: "Administrator Role")
                roleAdmin.addToPermissions(permAdmin)
                umm.addToPermissions(permAdmin)
                roleAdmin?.save(flush: true, failOnError: true)
//                umm?.save(flush: true, failOnError: true)

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
            output['microservice'] = it.micro?.id
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
                uniqueRole.roles
            }

            def arr=[]
            uniqueRoles.each{value->
                def map = json {
                    id value?.roles.id
                    name value?.roles.authority
                    description value?.roles.description
                    permissions value?.roles.permissions
                }
                arr.add(map)
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
