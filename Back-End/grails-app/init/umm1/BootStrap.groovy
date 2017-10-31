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
                        [name: "All Team Administration Operations", expression: "tam:*:*"],

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
                        [name: "Update Agents", expression: "agent:update"],
                        [name: "Update Agent's Profile Pic", expression: "agent:updateProfilePic"],


                        [name: "All queue Operations", expression: "queue:*",
                         preReqs: ["group:index",
                                   "skill:index"]],
                        //Requires group:index and skill:index
                        [name: "List queues", expression: "queue:index",
                         preReqs: ["group:index",
                                   "skill:index"]],
                        [name: "Create new queue", expression: "queue:save"],
                        [name: "Update queues", expression: "queue:update"],
                        [name: "Delete queues", expression: "queue:delete"],


                        [name: "All Team Operations", expression: "team:*",
                         preReqs: ["service:index",
                                   "agent:index",
                                   "queue:index",
                                   "application:index"]],
                        //Requires service:index, agent:index, queue:index and application:index
                        [name: "List Teams", expression: "team:index",
                         preReqs: ["service:index",
                                   "agent:index",
                                   "queue:index",
                                   "application:index"]],
                        [name: "Get a team's details", expression: "team:getTeam"],
                        [name: "Create new Teams", expression: "team:save"],
                        //Requires agent:index
                        [name: "Update Teams", expression: "team:update",
                         preReqs: "agent:index"],
                        [name: "Delete Teams", expression: "team:delete"],


                        [name: "All Application Operations", expression: "application:*",
                        preReqs:["prompt:getAllPrompts",
                                 "callControlGroup:index",
                                 "trigger:index",
                                 "script:getAllScripts",
                                 "script:index",
                                 "script:getScriptvariables"]],
                        //Requires prompt:getAllPrompts, callControlGroup:index, trigger:index, script:getAllScripts
                        [name: "List Applications", expression: "application:index",
                         preReqs:["prompt:getAllPrompts",
                                  "callControlGroup:index",
                                  "trigger:index",
                                  "script:getAllScripts"]],
                        [name: "Get an application's details", expression: "application:get"],
                        //Requires script:index, prompt:getAllPrompts, application:get, script:getScriptvariables
                        [name: "Create new Application", expression: "application:save",
                         preReqs:["prompt:getAllPrompts",
                                  "Application:get",
                                  "script:index",
                                  "script:getScriptvariables"]],
                        //Requires prompt:getAllPrompts, application:get and script:getScriptvariables
                        [name: "Update Applications", expression: "application:update",
                         preReqs: ["prompt:getAllPrompts",
                                   "application:get",
                                   "script:getScriptvariables"]],
                        [name: "Delete Applications", expression: "application:delete"],


                        //No cross domain prerequisite for scripts
                        [name: "All script Operations", expression: "script:*"],
                        [name: "List scripts", expression: "script:index"],
                        [name: "Upload new script", expression: "script:save"],
                        [name: "Update script's Name", expression: "script:update"],
                        [name: "Update script's Variables", expression: "script:updateVariables"],
                        [name: "Delete scripts or folders", expression: "script:delete"],
                        [name: "Create script Folder", expression: "script:createFolder"],
                        //Self prereqs
                        [name: "Update Script's folder", expression: "script:updateFolder", preReqs:["script:getAllScripts"]],
                        [name: "Get Scripts after folder update", expression: "script:getAllScripts"],

                        [name: "Get script's Variables", expression: "script:getScriptVariables"],


                        //No cross domain prerequisite for prompts
                        [name: "All prompt Operations", expression: "prompt:*"],
                        [name: "List prompts", expression: "prompt:index"],
                        [name: "Upload new prompt", expression: "prompt:save"],
                        //Self prereqs
                        [name: "Update a prompt", expression: "prompt:update", preReqs: ["script:getAllPrompts"]],
                        [name: "Get Prompts after folder update", expression: "script:getAllPrompts"],

                        [name: "Delete prompts or folders", expression: "prompt:delete"],
                        [name: "Create prompt Folder", expression: "prompt:createFolder"],
                        [name: "Download prompts", expression: "prompt:download"],


                        //No cross domain prerequisite for groups
                        [name: "All group Operations", expression: "group:*"],
                        [name: "List groups", expression: "group:index"],
                        [name: "Get a group's details", expression: "group:show"],
                        [name: "Create new group", expression: "group:save"],
                        [name: "Update groups", expression: "group:update"],
                        [name: "Delete groups", expression: "group:delete"],


                        //No cross domain prerequisite for skills
                        [name: "All skill Operations", expression: "skill:*"],
                        [name: "List skills", expression: "skill:index"],
                        [name: "Get a skill's details", expression: "skill:show"],
                        [name: "Create new skill", expression: "skill:save"],
                        [name: "Update skills", expression: "skill:update"],
                        [name: "Delete skills", expression: "skill:delete"],

                        //No cross domain prerequisite for directory numbers
                        [name: "All DN Operations", expression: "service:*"],
                        [name: "List DNs", expression: "service:index"],
                        [name: "Get a DN's details", expression: "service:show"],
                        [name: "Create new DN", expression: "service:save"],
                        [name: "Update DNs", expression: "service:update"],
                        [name: "Delete DNs", expression: "service:delete"],

                        //No cross domain prerequisite for triggers
                        [name: "All trigger Operations", expression: "trigger:*"],
                        [name: "List triggers", expression: "trigger:index"],
                        [name: "Get a trigger's details", expression: "trigger:get"],
                        [name: "Create new trigger", expression: "trigger:save"],
                        [name: "Update triggers", expression: "trigger:update"],
                        [name: "Delete triggers", expression: "trigger:delete"],

                        //No cross domain prerequisite for callControlGroup
                        [name: "All call control group Operations", expression: "callControllGroup:*"],
                        [name: "List call control group", expression: "callControllGroup:index"]

                ]

//                /*Permissions for Admin Panel Supervisor*/
                def permSupervisor = ["agent:*","application:*","queue:*","prompt:*","script:*",
                                      "trigger:*", "callControllGroup:*"]
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

                def roleJunior = new Role(authority: "junior supervisor", description: "Junior Spervisor Role")
                permJunior.each{
                    def perm = Permission.findByExpression(it)
                    roleJunior.addToPermissions(perm)
                }
                roleJunior?.save(flush: true, failOnError: true)

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
