package umm1


import com.ef.umm.*
import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.json.JsonBuilder

class BootStrap {

    GrailsApplication grailsApplication
    def CCSettingsService
    def licensingService

    def init = { servletContext ->
        String path = "/home/saqib/dev/tripledes-dll/output/libTrippleDes.so"
        Runtime.getRuntime().load0(groovy.lang.GroovyClassLoader.class, path)

        CCSettingsService.initialize()
        licensingService.init()

        if (Role.count() == 0) {
            try {
                def permTAM = [

                        //All team administartion operations
//                        [name: "All Team Administration Operations", expression: "tam:*:*"],

                        // Agent related permissions for TAM
                        [name: "All Agent Operations", expression: "agent:*"],
                        //Requires group:index, skill:index and team:index
                        [name: "Update Agent", expression: "agent:update"],
                        [name: "Update Agent's Profile Pic", expression: "agent:updateProfilePic"],


                        [name: "All Queue Operations", expression: "queue:*",  preReqs: ["agent:update", "team:getTeam"]],
                        [name: "Create new Queues", expression: "queue:save", preReqs: ["team:getTeam"]],
                        [name: "Update Queues", expression: "queue:update", preReqs: ["agent:update", "team:getTeam"]],
                        [name: "Delete Queues", expression: "queue:delete",  preReqs: ["agent:update"]],


                        [name: "All Workspace Operations", expression: "team:*"],
                        [name: "Show Workspace details", expression: "team:getTeam"],
                        [name: "Create new Workspace", expression: "team:save"],
                        [name: "Update Workspace", expression: "team:update", preReqs: ["team:getTeam"]],
                        [name: "Delete Workspace", expression: "team:delete"],


                        [name: "All Application Operations", expression: "application:*",
                         preReqs:["script:getScriptVariables",
                                  "trigger:*",
                                  "application:get",]],
                        [name: "Get an Application's Details", expression: "application:get", preReqs: ["script:getScriptVariables"]],
                        //Requires script:index, prompt:getAllPrompts, application:get, script:getScriptVariables
                        [name: "Create a new Application", expression: "application:save",
                         preReqs:["application:get",
                                  "script:getScriptVariables",
                                  "trigger:*"]],
                        //Requires prompt:getAllPrompts, application:get and script:getScriptVariables
                        [name: "Update Application", expression: "application:update",
                         preReqs: ["application:get",
                                   "script:getScriptVariables",
                                   "trigger:*"]],
                        [name: "Delete Application", expression: "application:delete"],


                        //No cross domain prerequisite for scripts
                        [name: "All Script Operations", expression: "script:*"],
                        [name: "Update Scripts or Folders", expression: "script:update"],
                        [name: "Delete Scripts or Folders", expression: "script:delete"],
                        [name: "Create Script Folders", expression: "script:createFolder"],
//                        [name: "Update Script Folder", expression: "script:updateFolder"],

                        [name: "Upload a new Script", expression: "script:save"],
                        [name: "Download a script", expression: "script:download"],

//                        [name: "Update Script Variables", expression: "script:updateVariables"],
                        [name: "Get Script's Variables", expression: "script:getScriptVariables"],


                        //No cross domain prerequisite for prompts
                        [name: "All Prompt Operations", expression: "prompt:*"],
                        [name: "Upload a new Prompt", expression: "prompt:save"],
                        //Self prereqs
                        [name: "Update Prompts or Folders", expression: "prompt:update"],
//                        [name: "Get all prompts", expression: "prompt:getAllPrompts"],

                        [name: "Delete Prompts or Folders", expression: "prompt:delete"],
                        [name: "Create Prompt Folder", expression: "prompt:createFolder"],
                        [name: "Download Prompts", expression: "prompt:download"],


                        //No cross domain prerequisite for groups
//                        [name: "All Group Operations", expression: "group:*"],
//                        [name: "Get a Group's Details", expression: "group:show"],
//                        [name: "Create a new Group", expression: "group:save"],
//                        [name: "Update Group", expression: "group:update"],
//                        [name: "Delete Group", expression: "group:delete"],


                        //No cross domain prerequisite for skills
//                        [name: "All Skill Operations", expression: "skill:*"],
//                        [name: "Get a Skill's Details", expression: "skill:show"],
//                        [name: "Create a new Skill", expression: "skill:save"],
//                        [name: "Update Skill", expression: "skill:update"],
//                        [name: "Delete Skill", expression: "skill:delete"],

                        //No cross domain prerequisite for directory numbers
//                        [name: "All DN Operations", expression: "service:*"],
//                        [name: "Get a DN's Details", expression: "service:show"],
//                        [name: "Create a new DN", expression: "service:save"],
//                        [name: "Update DN", expression: "service:update"],
//                        [name: "Delete DN", expression: "service:delete"],

                        //No cross domain prerequisite for triggers
                        [name: "All Trigger Operations", expression: "trigger:*"]
//                        [name: "Get a Trigger's Details", expression: "trigger:get"],
//                        [name: "Create a new Trigger", expression: "trigger:save"],
//                        [name: "Update Trigger", expression: "trigger:update"],
//                        [name: "Delete Trigger", expression: "trigger:delete"],

                        //No cross domain prerequisite for callControlGroup
//                        [name: "All call control group Operations", expression: "callControlGroup:*"]
                ]

                def permEABC = [

                        //All team administartion operations
//                        [name: "All Team Administration Operations", expression: "tam:*:*"],

                        // Agent related permissions for TAM
                        [name: "All Business Calendar Operations", expression: "businessCalendar:*",
                         preReqs: ["agency:list",
                                   "businessCalendarService:list",
                                   "holidayProfile:list",
                                   "workingEventLabel:list",
                                   "workingEvents:save"]],
                        //Requires group:index, skill:index and team:index
                        [name: "List Business Calendars", expression: "businessCalendar:list"],
                        [name: "Create Business Calendars", expression: "businessCalendar:save",
                         preReqs: ["businessCalendar:list",
                                   "agency:list",
                                   "businessCalendarService:list",
                                   "holidayProfile:list",
                                   "workingEventLabel:list",
                                   "workingEvents:save"]],
                        [name: "Update Business Calendars", expression: "businessCalendar:update",
                         preReqs: ["businessCalendar:list",
                                   "agency:list",
                                   "businessCalendarService:list",
                                   "holidayProfile:list",
                                   "workingEventLabel:list",
                                   "workingEvents:save"]],
                        [name: "Delete Business Calendars", expression: "businessCalendar:delete", 
                         preReqs: ["businessCalendar:list"]],

                        
                        [name: "All Holiday Profile Operations", expression: "holidayProfile:*"],
                        //Requires group:index, skill:index and team:index
                        [name: "List Holiday Profile", expression: "holidayProfile:list"],
                        [name: "Create Holiday Profile", expression: "holidayProfile:save",
                         preReqs: ["holidayProfile:list"]],
                        [name: "Update Holiday Profile", expression: "holidayProfile:update",
                         preReqs: ["holidayProfile:list"]],
                        [name: "Delete Holiday Profile", expression: "holidayProfile:delete",
                         preReqs: ["holidayProfile:list"]],

                        [name: "All Easy Announcement Operations", expression: "easyAnnouncement:*"],
                        //Requires group:index, skill:index and team:index
                        [name: "List Business Calendars", expression: "easyAnnouncement:index", preReqs: ["region:list"]],
                        [name: "Create Business Calendars", expression: "easyAnnouncement:save",
                         preReqs: ["easyAnnouncement:index",
                                    ""]],
                        [name: "Update Business Calendars", expression: "easyAnnouncement:update",
                         preReqs: ["easyAnnouncement:index"]],
                        [name: "Delete Business Calendars", expression: "easyAnnouncement:delete",
                         preReqs: ["easyAnnouncement:index"]],
                        
                ]


                println(permTAM as JSON)
                println(permEABC as JSON)
//                /*Permissions for Admin Panel Supervisor*/
                def permSupervisor = ["agent:*",
                                      "application:*", "queue:*",
                                      "prompt:*","script:*",
                                      "trigger:*", "team:*"]

                def permJunior = ["agent:*","queue:*"]


                def admin
                def umm

                // Microservices
                if(!Microservice.findByName("umm")){
                    umm = new Microservice(name: 'umm', ipAddress: "http://127.0.0.1:9091", description: 'User Management MicroService')
                    umm?.save(flush: true, failOnError: true)
                }

                def adminPanel = grailsApplication.config.getProperty('names.adminPanel')
                def efadminpanel = new Microservice(name: adminPanel, ipAddress: "http://127.0.0.1:8080", description: 'Team Administration Module')
                efadminpanel?.save(flush: true, failOnError: true)

                // Roles

                def permAdmin = new Permission(name: "Super User Access(Admin)", expression: "*:*")
                def roleAdmin = new Role(authority: "Administrator", description: "Administrator Role")
                roleAdmin.addToPermissions(permAdmin)
                umm.addToPermissions(permAdmin)
                roleAdmin?.save(flush: true, failOnError: true)


                permTAM.each{
                    def perm = new Permission(it)
                    efadminpanel.addToPermissions(perm)
                    perm.save(flush:true, failOnError: true)
                }

                def roleSupervisor = new Role(authority: "Supervisor", description: "Supervisor Role")
                permSupervisor.each{
                    def perm = Permission.findByExpression(it)
                    roleSupervisor.addToPermissions(perm)
                }
                roleSupervisor?.save(flush: true, failOnError: true)

                def roleJunior = new Role(authority: "Junior_Supervisor", description: "Junior Supervisor Role")
                permJunior.each{
                    def perm = Permission.findByExpression(it)
                    roleJunior.addToPermissions(perm)
                }
                roleJunior?.save(flush: true, failOnError: true)

                //This is a default permission for every newly created role.
                //It won't show on interface or in the APIs.
                def permDefault = new Permission(name: "Default Dummy permission", expression: "default:*")
                umm.addToPermissions(permDefault)
                permDefault.save(flush:true, failOnError: true)

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
            output['preReqs'] = it.preReqs
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
