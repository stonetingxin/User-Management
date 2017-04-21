package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RoleController)
@Mock([User, Role, Permission, UMR, Microservice])
class RoleControllerSpec extends Specification {

    def setup() {
        try {
            def ahmed = new User(username: "ahmed", password: "admin", authProvider: "local")
            def hamid = new User(username: "hamid", password: "nothing", authProvider: "external")
            ahmed?.save()
            hamid?.save()

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


            UMR.create ahmed, admin, pcs
            UMR.create hamid, admin, cbr

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

    void "test create api"() {
        when: 'save method is called with a json containing user, microservice and role details'
        request?.method = "POST"
        request?.json = $/{"authority":"ROLE_NEW","permissions":[{"name":"com.app.ef.queue",/$+
                $/"expression":"nothing:*"},{"name":"com.app.ef.blue","expression":"something:*"}]}/$
        Role.count() == 2
        controller?.create()

        then: 'a new role should be created incrementing the total role count by 1'
        response?.status == 200
        Role.count() == 3
        response?.json.message == "New Role: 'ROLE_NEW' has been created with permissions:" +
                " [Name: com.app.ef.queue, Expression: nothing:*, Name: com.app.ef.blue, Expression: something:*]"
    }

    void "test list api"() {
        when: 'list function is called '
        controller?.list()

        then: 'a json response of complete list of roles with corresponding permissions,' +
                'should be returned.'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"}/$+
                $/,"Roles":[{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1/$+
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
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},"Role":/$+
                $/{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app/$+
                $/.ef.admin","expression":"*:*"}]}}/$
    }

    void "test delete api (successfull)"() {
        when: 'delete is called with a valid role id'
        request?.method = "DELETE"
        request?.setParameter("id", "2")
        Role.count()==2
        controller?.delete()

        then: 'role having that id should be deleted decrementing the role count by 1'
        response?.status == 200
        Role.count() == 1
        response?.json.message == "Successfully deleted role: Supervisor"
    }

    void "test delete api (unsuccessfull)"() {
        when: 'delete is called with a valid role id which is assigned to users'
        request?.method = "DELETE"
        request?.setParameter("id", "1")
        Role.count()==2
        controller?.delete()

        then: 'role having that id should not be deleted.'
        response?.status == 406
        Role.count() == 2
        response?.json.message == "Cannot delete the role. Role is already assigned to following user(s):"
    }

    void "test update role api"() {

        when: 'update is called with existing user-microservice association'
        request?.method = "POST"
        request?.json = $/{"authority":"ROLE_NEW","permissions":[{"name":"com.app.ef.queue",/$+
                $/"expression":"nothing:*"},{"name":"com.app.ef.blue","expression":"something:*"}]}/$
        controller?.create()
        request?.method = "PUT"
        request?.json = $/{"authority":"ROLE_NEW","permissions":[{"name":"com.app.ef.queue",/$+
                $/"expression":"BlahBlah:*"},{"name":"com.app.ef.blue","expression":"onething:*"}]}/$
        controller?.update()

        then: 'the role of user for corresponding microservice should be updated as provided in the request json'
        response?.status == 200
        response?.json.message == "Role has been changed for ahmed in PCS"
        def umr = UMR.findByUsersAndMicroservices(User.findByUsername("ahmed"), Microservice.findByName("PCS"))
        umr.toString() == "ahmed as Supervisor in PCS"
    }

    void "test update microservice-role api"() {
        when: 'update is called for a new user-microservice association'
        request?.method = "PUT"
        request?.json = $/{"username":"ahmed","microservice":"CBR","role":"Admin"}/$
        UMR.findByUsersAndMicroservices(User.findByUsername("ahmed"), Microservice.findByName("CBR")) == null
        controller?.update()

        then: 'the corresponding role microservice should be added for the user'
        response?.status == 200
        response?.json.message == "New role: 'Admin' has been added for ahmed in CBR"
        def umr = UMR.findByUsersAndMicroservices(User.findByUsername("ahmed"), Microservice.findByName("CBR"))
        umr.toString() == "ahmed as Admin in CBR"
    }
}
