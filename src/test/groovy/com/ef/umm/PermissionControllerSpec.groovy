package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(PermissionController)
@Mock([User, Role, Permission, UMR, Microservice])
class PermissionControllerSpec extends Specification {

    def setup() {
        try {
            def ahmed = new User(username: "ahmed", password: "admin")
            def hamid = new User(username: "hamid", password: "nothing")
            def userAdmin = new User(username: "admin", password: "admin")
            ahmed?.save()
            hamid?.save()
            userAdmin.save()

            def admin = new Role(authority: "ROLE_ADMIN", description: "Administrator")
            admin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
            admin?.save()

            def role = new Role(authority: "Supervisor", description: "Supervisor role")
            role.addToPermissions(name: "com.app.ef.show", expression: "show:*")
            role.addToPermissions(name: "com.app.ef.update", expression: "update:*")
            role?.save()


            def pcs = new Microservice(name: 'PCS', ipAddress: "192.168.1.79:8080",description: 'Post Call Survey')
            def cbr = new Microservice(name: 'CBR', ipAddress: "192.168.1.79:8080",description: 'Caller Based Routing')
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
        when: 'create method is called with a json containing microservice details'
        request?.method = "POST"
        request?.json = input
        controller?.create()

        then: 'a new user should be created incrementing the total user count by 1'
        response?.status == status
        Permission.count() == count
        response?.json.message == output

        where:
        input << [$/{"name":"com.ef.umm.user.update","expression":"user:update"}/$,
                  $/{"name":"com.ef.umm.user.delete","expression":"user:delete", "description":"asd"}/$,
                  $/{"name":"com.app.ef.show","expression":"user:delete", "description":"asd"}/$,
                  $/{"name":"com.app.ef.show","expression":"show:*", "description":"asd"}/$,
                  $/{"nam":"asif"}/$]

        output<< ["New Permission: 'com.ef.umm.user.update' has been created successfully.",
                  "New Permission: 'com.ef.umm.user.delete' has been created successfully.",
                  "Permission with name: com.app.ef.show already exists. Kindly provide a new name or call create api.",
                  "Permission with name: com.app.ef.show and expression: show:* already exists.",
                  "Invalid JSON provided. Please read the API specifications."]

        count <<[4,
                 4,
                 3,
                 3,
                 3]

        status <<[200,
                  200,
                  406,
                  406,
                  406]
    }

    void "test list api"() {
        when: 'list function is called '
        controller?.list()

        then: 'a json response of complete list of permissions should be returned.'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                $/"permissions":[{"id":1,"name":"com.app.ef.admin","expression":"*:*"},{"id":2,"name"/$+
                $/:"com.app.ef.show","expression":"show:*"},{"id":3,"name":"com.app.ef.update","express/$+
                $/ion":"update:*"}]}/$
    }

    void "test show api"() {
        when: 'show is called for a valid permission id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with permission having that id.'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                          $/"permission":{"id":1,"name":"com.app.ef.admin","expression":"*:*"}}/$
    }

    @Unroll
    void "test delete api"() {
        when: 'delete is called with a valid permission id'
        request?.method = "DELETE"
        request?.setParameter("id", input)
        controller?.delete()

        then: 'permission having that id should be deleted decrementing the permission count by 1'
        response?.status == status
        Permission.count() == count
        response?.json.message == output

        where:

        input <<["1",
                 "2",
                 "5"]

        output<< ["Cannot delete admin's permissions.",
                  "Successfully deleted permission: com.app.ef.show",
                  "Permission not found. Provide a valid permission instance."]

        count <<[3,
                 2,
                 3]

        status <<[406,
                  200,
                  404]
    }

    @Unroll
    void "test update api"() {
        when: 'update is called with existing permission'
        request?.method = "PUT"
        request?.json = input
        controller?.update()

        then: 'corresponding permission should be updated as provided in the request json'
        response?.status == status
        response?.json.message == output

        where:

        input <<[$/{"id":"1", "name":"com.ef.umm.admin","expression":"*:*"}/$,
                 $/{"id":"5", "name":"com.ef.umm.update", "expression":"update:*"}/$,
                 $/{"d":"3", "name":"asif"}/$]

        output<< ["com.ef.umm.admin has been updated successfully.",
                  "Permission not found. Invalid update request. For creating new permission, use create API instead.",
                  "Invalid JSON provided. Please read the API specifications."]


        status <<[200,
                  404,
                  406]
    }

}
