package umm1

import com.ef.umm.*
import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.json.JsonBuilder

class BootStrap {
    GrailsApplication grailsApplication
    def CCSettingsService
    def init = { servletContext ->
        CCSettingsService.initialize()
        if (Role.count() == 0) {
            try {

                def permTAM = [

                        //All team administartion operations
//                        [name: "All Team Administration Operations", expression: "tam:*:*"],

                        // Agent related permissions for TAM
                        [name: "All Agent Operations", expression: "agent:*",
                         preReqs:["group:index",
                                  "skill:index",
                                  "team:index"]],
                        //Requires group:index, skill:index and team:index
                        [name: "List Agents", expression: "agent:index",
                         preReqs:["group:index",
                                  "skill:index",
                                  "team:index"]],
                        [name: "Update Agents", expression: "agent:update", preReqs: ["agent:index"]],
                        [name: "Update Agent's Profile Pic", expression: "agent:updateProfilePic"],


                        [name: "All Queue Operations", expression: "queue:*",
                         preReqs: ["group:index",
                                   "skill:index",
                                   "agent:index"]],
                        //Requires group:index and skill:index
                        [name: "List Queues", expression: "queue:index",
                         preReqs: ["group:index",
                                   "skill:index",
                                    "agent:index"]],
                        [name: "Create new Queues", expression: "queue:save"],
                        [name: "Update Queues", expression: "queue:update", preReqs: ["queue:index"]],
                        [name: "Delete Queues", expression: "queue:delete"],


                        [name: "All Workspace Operations", expression: "team:*",
                         preReqs: ["service:index",
                                   "agent:index",
                                   "queue:index",
                                   "application:index"]],
                        //Requires service:index, agent:index, queue:index and application:index
                        [name: "List Workspace", expression: "team:index",
                         preReqs: ["service:index",
                                   "agent:index",
                                   "queue:index",
                                   "application:index"]],
                        [name: "Show any Workspace's details", expression: "team:getTeam"],
                        [name: "Create a new Workspace", expression: "team:save"],
                        //Requires agent:index
                        [name: "Update Workspace", expression: "team:update",
                         preReqs: ["agent:index", "team:index"]],
                        [name: "Delete Workspace", expression: "team:delete"],


                        [name: "All Application Operations", expression: "application:*",
                         preReqs:["script:getAllPrompts",
                                  "callControllGroup:index",
                                  "trigger:index",
                                  "script:getAllScripts",
                                  "script:index",
                                  "script:getScriptvariables"]],
                        //Requires script:getAllPrompts, callControllGroup:index, trigger:index, script:getAllScripts
                        [name: "List Application", expression: "application:index",
                         preReqs:["script:getAllPrompts",
                                  "callControllGroup:index",
                                  "trigger:index",
                                  "script:getAllScripts"]],
                        [name: "Get an Application's Details", expression: "application:get"],
                        //Requires script:index, script:getAllPrompts, application:get, script:getScriptvariables
                        [name: "Create a new Application", expression: "application:save",
                         preReqs:["script:getAllPrompts",
                                  "Application:get",
                                  "script:index",
                                  "script:getScriptvariables"]],
                        //Requires script:getAllPrompts, application:get and script:getScriptvariables
                        [name: "Update Application", expression: "application:update",
                         preReqs: ["script:getAllPrompts",
                                   "application:get",
                                   "script:getScriptvariables",
                                   "application:index"]],
                        [name: "Delete Application", expression: "application:delete"],


                        //No cross domain prerequisite for scripts
                        [name: "All Script Operations", expression: "script:*"],
                        [name: "List Script", expression: "script:index"],
                        [name: "Upload a new Script", expression: "script:save"],
                        [name: "Update Script Name", expression: "script:update"],
                        [name: "Update Script Variables", expression: "script:updateVariables"],
                        [name: "Delete Script or Folder", expression: "script:delete"],
                        [name: "Create Script Folder", expression: "script:createFolder"],
                        //Self prereqs
                        [name: "Update Script Folder", expression: "script:updateFolder", preReqs:["script:getAllScripts", "script:index"]],
                        [name: "Get all scripts", expression: "script:getAllScripts"],

                        [name: "Get Script's Variables", expression: "script:getScriptVariables"],


                        //No cross domain prerequisite for prompts
                        [name: "All Prompt Operations", expression: "prompt:*"],
                        [name: "List Prompts", expression: "prompt:index"],
                        [name: "Upload a new Prompt", expression: "prompt:save"],
                        //Self prereqs
                        [name: "Update a Prompt", expression: "prompt:update", preReqs: ["script:getAllPrompts", "prompt:index"]],
                        [name: "Get all prompts", expression: "script:getAllPrompts"],

                        [name: "Delete Prompts or Folders", expression: "prompt:delete"],
                        [name: "Create Prompt Folder", expression: "prompt:createFolder"],
                        [name: "Download Prompts", expression: "prompt:download"],


                        //No cross domain prerequisite for groups
                        [name: "All Group Operations", expression: "group:*"],
                        [name: "List Groups", expression: "group:index"],
                        [name: "Get a Group's Details", expression: "group:show"],
                        [name: "Create a new Group", expression: "group:save"],
                        [name: "Update Group", expression: "group:update", preReqs: ["group:index"]],
                        [name: "Delete Group", expression: "group:delete"],


                        //No cross domain prerequisite for skills
                        [name: "All Skill Operations", expression: "skill:*"],
                        [name: "List Skills", expression: "skill:index"],
                        [name: "Get a Skill's Details", expression: "skill:show"],
                        [name: "Create a new Skill", expression: "skill:save"],
                        [name: "Update Skill", expression: "skill:update", preReqs: ["skill:index"]],
                        [name: "Delete Skill", expression: "skill:delete"],

                        //No cross domain prerequisite for directory numbers
                        [name: "All DN Operations", expression: "service:*"],
                        [name: "List DNs", expression: "service:index"],
                        [name: "Get a DN's Details", expression: "service:show"],
                        [name: "Create a new DN", expression: "service:save"],
                        [name: "Update DN", expression: "service:update", preReqs: ["service:index"]],
                        [name: "Delete DN", expression: "service:delete"],

                        //No cross domain prerequisite for triggers
                        [name: "All Trigger Operations", expression: "trigger:*"],
                        [name: "List Triggers", expression: "trigger:index"],
                        [name: "Get a Trigger's Details", expression: "trigger:get"],
                        [name: "Create a new Trigger", expression: "trigger:save"],
                        [name: "Update Trigger", expression: "trigger:update", preReqs: ["trigger:index"]],
                        [name: "Delete Trigger", expression: "trigger:delete"],

                        //No cross domain prerequisite for callControllGroup
                        [name: "All call control group Operations", expression: "callControllGroup:*"],
                        [name: "List call control group", expression: "callControllGroup:index"]

                ]


                println(permTAM as JSON)
//                /*Permissions for Admin Panel Supervisor*/
                def permSupervisor = ["agent:*","group:index",
                                      "skill:index",
                                      "application:*", "queue:*","group:index",
                                      "prompt:*","script:*",
                                      "trigger:*", "callControllGroup:*"]
                def permJunior = ["agent:*","queue:*","group:index", "skill:index"]


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
                def roleAdmin = new Role(authority: "admin", description: "Administrator Role")
                roleAdmin.addToPermissions(permAdmin)
                umm.addToPermissions(permAdmin)
                roleAdmin?.save(flush: true, failOnError: true)
//                umm?.save(flush: true, failOnError: true)


                permTAM.each{
                    def perm = new Permission(it)
//                    roleSupervisor.addToPermissions(perm)
                    efadminpanel.addToPermissions(perm)
                    perm.save(flush:true, failOnError: true)
                }

                def roleSupervisor = new Role(authority: "supervisor", description: "Supervisor Role")
                permSupervisor.each{
                    def perm = Permission.findByExpression(it)
                    roleSupervisor.addToPermissions(perm)
                }
                roleSupervisor?.save(flush: true, failOnError: true)
//                efadminpanel?.save(flush: true, failOnError: true)

                def roleJunior = new Role(authority: "juniorSupervisor", description: "Junior Supervisor Role")
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
//                def roleDefaultAP = new Role(authority: "default", description: "Default role for admin panel")
//                permDefaultAP.each{
//                    def perm = Permission.findByExpression(it?.expression)
//                    if(perm){
//                        roleDefaultAP.addToPermissions(perm)
//                        efadminpanel.addToPermissions(perm)
//                    } else {
//                        def perm2 = new Permission(it)
//                        roleDefaultAP.addToPermissions(perm2)
//                        efadminpanel.addToPermissions(perm2)
//                    }
//                }
//                roleDefaultAP.save(flush: true, failOnError: true)

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
