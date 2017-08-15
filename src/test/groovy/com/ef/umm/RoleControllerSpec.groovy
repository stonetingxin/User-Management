package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RoleController)
@Mock([User, Role, Permission, UMR, Microservice])
class RoleControllerSpec extends Specification {

    def setup() {
        try {
            def ahmed = new User(username: "ahmed", password: "admin", type: "DB")
            def hamid = new User(username: "hamid", password: "nothing", type: "DB")
            def userAdmin = new User(username: "admin", password: "admin", type: "DB")
            ahmed?.save(failOnError: true)
            hamid?.save(failOnError: true)
            userAdmin.save(failOnError: true)

            def admin = new Role(authority: "ROLE_ADMIN", description: "Administrator")
            admin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
            admin?.save(failOnError: true)

            def role = new Role(authority: "Supervisor", description: "Supervisor role")
            role.addToPermissions(name: "com.app.ef.show", expression: "show:*")
            role.addToPermissions(name: "com.app.ef.update", expression: "update:*")
            role?.save(failOnError: true)

            def pcs = new Microservice(name: 'PCS', ipAddress: "https://192.168.1.79:8080",description: 'Post Call Survey')
            def cbr = new Microservice(name: 'CBR', ipAddress: "http://192.168.1.79:8080",description: 'Caller Based Routing')
            pcs?.save(failOnError: true)
            cbr?.save(failOnError: true)


            UMR.create ahmed, admin, pcs, true
            UMR.create hamid, admin, cbr, true

            UMR.withSession {
                it.flush()
                it.clear()
            }
        }
        catch (Exception ex){
            println ex.getMessage()
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

    def cleanup() {
    }

    @Unroll
    void "test create api"() {
        when: 'save method is called with a json containing user, microservice and role details'
        request?.method = "POST"
        request?.json = input
        controller?.create()

        then: 'a new role should be created incrementing the total role count by 1'
        response?.status == status
        Role.count() == count
        response?.json.message == output

        where:
        input << [$/{"authority":"ROLE_NEW"}/$,
                  $/{"autho=rity":"ROLE_NEW"}/$,
                  $/{"authority":"ROLE_ADMIN"}/$]

        output<< ["New Role: 'ROLE_NEW' has been created successfully. ",
                  "Invalid JSON provided. Please read the API specifications.",
                  "Role with name: ROLE_ADMIN already exists. Kindly provide either a new name, or call update API."]

        count <<[3,
                 2,
                 2]

        status <<[200,
                  406,
                  406]
    }

    void "test list api"() {
        when: 'list function is called '
        controller?.list()

        then: 'a json response of complete list of roles with corresponding permissions,' +
                'should be returned.'
        response?.status == 200
        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStatus" +
                "\",\"name\":\"OK\"},\"roles\":[{\"id\":1,\"name\":\"ROLE_ADMIN\",\"descrip" +
                "tion\":\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"com.app.ef." +
                "admin\",\"expression\":\"*:*\"}]},{\"id\":2,\"name\":\"Supervisor\",\"desc" +
                "ription\":\"Supervisor role\",\"permissions\":[{\"id\":2,\"name\":\"com.app" +
                ".ef.show\",\"expression\":\"show:*\"}]}]}"
    }

    void "test show api"() {
        when: 'show is called for a valid user id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with user having that id along with the ' +
                'corresponding microservices, roles and permissions'
        response?.status == 200
        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStat" +
                "us\",\"name\":\"OK\"},\"role\":{\"id\":1,\"name\":\"ROLE_ADMIN\",\"des" +
                "cription\":\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"com." +
                "app.ef.admin\",\"expression\":\"*:*\"}]}}"
    }

    @Unroll
    void "test delete api"() {
        when: 'delete is called with a valid role id'
        request?.method = "DELETE"
        request?.setParameter("id", input)
        controller?.delete()

        then: 'role having that id should be deleted decrementing the role count by 1'
        response?.status == status
        Role.count() == count
        response?.json.message == output

        where:
        input <<["1",
                 "2",
                 "3"]

        output<< ["Cannot delete the role. Role is assigned to following user(s):",
                  "Successfully deleted role: Supervisor",
                  "Role not found. Provide a valid role instance."]

        count <<[2,
                 1,
                 2]

        status <<[406,
                  200,
                  404]
    }


    void "test deleteMulti api"() {
        when: 'delete is called with an array of valid role ids'
        request?.method = "DELETE"
        request?.json = $/[{"id":"1"},{"id":"2"},{"id":"3"},{"id":"4"}]/$
        controller?.deleteMulti()

        then: 'role having that id should be deleted if it is not assigned to any user'
        response?.status == status
        response?.json.message.toString() == "[\"Cannot delete role: ROLE_ADMIN. It is assigned to following user(s): " +
                "[ahmed as ROLE_ADMIN in PCS, hamid as ROLE_ADMIN in CBR]\"," +
                "\"Successfully deleted role: Supervisor\"," +
                "\"Role with ID: 3 not found. Provide a valid role id.\"," +
                "\"Role with ID: 4 not found. Provide a valid role id.\"]"

    }

    @Unroll
    void "test update role api"() {

        when: 'update is called with existing user-microservice association'
        request?.method = "PUT"
        request?.json = input
        controller?.update()

        then: 'the role of user for corresponding microservice should be updated as provided in the request json'
        response?.status == status
        response?.json.message == output

        where:
        input << [$/{"id":"2","authority":"blahblah", "description":"bleh blehbleh"}/$,
                  $/{"id":"1","authority":"blahblah"}/$,
                  $/{"id":"3","authority":"blahblah"}/$]

        output << ["Role has been updated successfully.",
                   "Role has been updated successfully.",
                   "Role not found. Invalid update request. For creating new role, use create API instead."]

        status << [200,
                   200,
                   404]
    }

    @Unroll
    void "test add permissions api"() {
        when: 'addRevokePermissions is called with add functionality'
        request?.method = "PUT"
        request?.json = input
        controller?.addRevokePermissions()

        then: 'the corresponding permission should be added for the role'
        response?.status == status
        def message = response?.json.message
        message.toString() == output
        def out = null
        if(findPerms(id)){
            out = findPerms(id).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","permissions":[{"id":"1"}, {"id":"2"}], "addRevoke":"add"}/$,
                  $/{"id":"2","permissions":[{"id":"2"}], "addRevoke":"add"}/$,
                  $/{"id":"2","permissions":[{"id":"1"}], "addRevoke":"add"}/$]

        output << [$/["Permission: com.app.ef.admin has already been added in the role."/$+
                   $/,"Permission: com.app.ef.show has been successfully added."]/$,
                   $/["Permission: com.app.ef.show has already been added in the role."]/$,
                   $/["Permission: com.app.ef.admin has been successfully added."]/$]
        status << [200,
                   200,
                   200]
        id <<["1",
             "2",
             "2"]

        umrOutput <<["[Name: com.app.ef.admin, Expression: *:*, Name: com.app.ef.show, Expression: show:*]",
                     "[Name: com.app.ef.show, Expression: show:*]",
                     "[Name: com.app.ef.admin, Expression: *:*, Name: com.app.ef.show, Expression: show:*]"]
    }

    @Unroll
    void "test revoke permissions api"() {
        when: 'addRevokePermissions is called with revoke functionality'
        request?.method = "PUT"
        request?.json = input
        controller?.addRevokePermissions()

        then: 'the corresponding permission should be revoked from the role'
        response?.status == status
        def message = response?.json.message
        message.toString() == output
        def out = null
        if(findPerms(id)){
            out = findPerms(id).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","permissions":[{"id":"1"}, {"id":"2"}], "addRevoke":"revoke"}/$,
                  $/{"id":"2","permissions":[{"id":"1"}], "addRevoke":"revoke"}/$,
                  $/{"id":"2","permissions":[{"id":"2"}], "addRevoke":"asdf"}/$,
                  $/{"id":"1","permissions":[{"id":"2"}], "addRevoke":"revoke"}/$]

        output << ["Super user permissions cannot be revoked from admin role.",
                   $/["Permission: com.app.ef.admin cannot be revoked since it's not assigned to the role."]/$,
                   "Only add or revoke is allowed in this method.",
                   $/["Permission: com.app.ef.show cannot be revoked since it's not assigned to the role."]/$]
        status << [406,
                   200,
                   406,
                   200]
        id <<["1",
              "2",
              "2",
              "1"]

        umrOutput <<[  "[Name: com.app.ef.admin, Expression: *:*]",
                      "[Name: com.app.ef.show, Expression: show:*]",
                      "[Name: com.app.ef.show, Expression: show:*]",
                      "[Name: com.app.ef.admin, Expression: *:*]"]
    }

    private findPerms(String id){
        def role = Role.findById(id)
        return role.permissions.sort{it?.expression}
    }
}
