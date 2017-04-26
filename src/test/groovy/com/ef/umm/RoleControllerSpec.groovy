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
            def ahmed = new User(username: "ahmed", password: "admin")
            def hamid = new User(username: "hamid", password: "nothing")
            def userAdmin = new User(username: "admin", password: "admin")
            ahmed?.save()
            hamid?.save()
            userAdmin.save()

            def admin = new Role(authority: "Admin", description: "Administrator")
            admin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
            admin?.save()

            def role = new Role(authority: "Supervisor", description: "Supervisor role")
            role.addToPermissions(name: "com.app.ef.show", expression: "show:*")
            role?.save()

            def pcs = new Microservice(name: 'PCS', description: 'Post Call Survey')
            def cbr = new Microservice(name: 'CBR', description: 'Caller Based Routing')
            pcs.addToRoles(admin)
            pcs?.save()
            cbr.addToRoles(admin)
            cbr?.save()


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
                  $/{"authority":"Admin"}/$]

        output<< ["New Role: 'ROLE_NEW' has been created successfully. ",
                  "Invalid JSON provided. Please read the API specifications.",
                  "Role with name: Admin already exists. Kindly provide either a new name, or call update API."]

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
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"}/$+
                $/,"roles":[{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1/$+
                $/,"name":"com.app.ef.admin","expression":"*:*"}]},{"id":2,"name":"Supervisor","description"/$+
                $/:"Supervisor role","permissions":[{"id":2,"name":"com.app.ef.show","expression":"show:*"}/$+
                $/]}]}/$
    }

    void "test show api"() {
        when: 'show is called for a valid user id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with user having that id along with the ' +
                'corresponding microservices, roles and permissions'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},"role":/$+
                $/{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app/$+
                $/.ef.admin","expression":"*:*"}]}}/$
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
        response?.json.message == output
        def out = null
        if(findPerms(id)){
            out = findPerms(id).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","permissions":[{"id":"1"}, {"id":"2"}], "addRevoke":"add"}/$,
                  $/{"id":"2","permissions":[{"id":"2"}], "addRevoke":"add"}/$,
                  $/{"id":"2","permissions":[{"id":"1"}], "addRevoke":"add"}/$]

        output << ["[Permission: com.app.ef.admin have already been added in the role., " +
                           "Permission: com.app.ef.show has been successfully added.]",
                   "[Permission: com.app.ef.show have already been added in the role.]",
                   "[Permission: com.app.ef.admin has been successfully added.]"]
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
        response?.json.message == output
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

        output << ["[Permission: com.app.ef.admin have been successfully revoked., " +
                           "Permission: com.app.ef.show cannot be revoked since it's not assigned to the role.]",
                   "[Permission: com.app.ef.admin cannot be revoked since it's not assigned to the role.]",
                   "Only add or revoke is allowed in this method.",
                   "[Permission: com.app.ef.show cannot be revoked since it's not assigned to the role.]"]
        status << [200,
                   200,
                   406,
                   200]
        id <<["1",
              "2",
              "2",
              "1"]

        umrOutput <<[  null,
                      "[Name: com.app.ef.show, Expression: show:*]",
                      "[Name: com.app.ef.show, Expression: show:*]",
                      "[Name: com.app.ef.admin, Expression: *:*]"]
    }

    private findPerms(String id){
        def role = Role.findById(id)
        return role.permissions.sort{it?.expression}
    }
}
