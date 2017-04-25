package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(MicroserviceController)
@Mock([User, Role, Permission, UMR, Microservice])
class MicroserviceControllerSpec extends Specification {

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
            role.addToPermissions(name: "com.app.ef.update", expression: "update:*")
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
        when: 'create method is called with a json containing microservice details'
        request?.method = "POST"
        request?.json = input
        controller?.create()

        then: 'a new user should be created incrementing the total user count by 1'
        response?.status == status
        Microservice.count() == count
        response?.json.message == output

        where:
        input << [$/{"name":"UMM","description":"User Management Microservice"}/$,
                  $/{"name":"CBR", "password":"asdasd"}/$,
                  $/{"nam":"asif"}/$]

        output<< ["New Microservice: 'UMM' has been created successfully.",
                  "Microservice: CBR already exists.Kindly provide either a new name, or call update API.",
                  "Invalid JSON provided. Please read the API specifications."]

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

        then: 'a json response of complete list of users with corresponding microservices,' +
                'roles and permissions should be returned.'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                $/"Microservices":[{"id":1,"name":"PCS","description":"Post Call Survey","roles":/$+
                $/[{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1,/$+
                $/"name":"com.app.ef.admin","expression":"*:*"}]}]},{"id":2,"name":"CBR","description"/$+
                $/:"Caller Based Routing","roles":[{"id":1,"name":"Admin","description":"Administrator"/$+
                $/,"permissions":[{"id":1,"name":"com.app.ef.admin","expression":"*:*"}]}]}]}/$
    }

    void "test show api"() {
        when: 'show is called for a valid microservice id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with microservice having that id along with the ' +
                'corresponding roles and permissions'
        response?.status == 200
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                $/"Microservice":{"id":1,"name":"PCS","description":"Post Call Survey","roles":[{"id":1/$+
                $/,"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app.ef/$+
                $/.admin","expression":"*:*"}]}]}}/$
    }

    @Unroll
    void "test delete api"() {
        when: 'delete is called with a valid microservice id'
        request?.method = "DELETE"
        request?.setParameter("id", input)
        controller?.delete()

        then: 'microservice having that id should be deleted decrementing the microservice count by 1'
        response?.status == status
//        def micro = Microservice.findById("1" as Long)
//        println micro
//        def umr = UMR.findByMicroservices(micro)
//        println umr
//        umr == null
        Microservice.count() == count
        response?.json.message == output

        where:

        input <<["1",
                 "2",
                 "3"]

        output<< ["Successfully deleted microservice: PCS",
                  "Successfully deleted microservice: CBR",
                  "MicroService not found. Provide a valid microservice instance."]

        count <<[1,
                 1,
                 2]

        status <<[200,
                  200,
                  404]
    }

    @Unroll
    void "test update api"() {
        when: 'update is called with existing microservice'
        request?.method = "PUT"
        request?.json = input
        controller?.update()

        then: 'corresponding microservice should be updated as provided in the request json'
        response?.status == status
        response?.json.message == output

        where:

        input <<[$/{"id":"1", "name":"UMM","description":"User Management Microservice"}/$,
                 $/{"id":"2", "name":"CBR"}/$,
                 $/{"id":"3", "name":"asif"}/$]

        output<< ["UMM has been updated successfully.",
                  "CBR has been updated successfully.",
                  "Microservice not found. Invalid update request. For registering new Microservice, use create API instead."]


        status <<[200,
                  200,
                  404]
    }

    @Unroll
    void "test add Roles api"() {
        when: 'addRemoveRoles is called with add functionality'
        request?.method = "PUT"
        request?.json = input
        controller?.addRemoveRoles()

        then: 'the corresponding permission should be added for the role'
        response?.status == status
        response?.json.message == output
        def out = null
        if(findRoles(id)){
            out = findRoles(id).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","roles":[{"id":"1"}, {"id":"2"}], "addRemove":"add"}/$,
                  $/{"id":"2","roles":[{"id":"2"}], "addRemove":"add"}/$,
                  $/{"id":"2","roles":[{"id":"3"}], "addRemove":"add"}/$]

        output << ["Roles have been successfully added.",
                   "Roles have been successfully added.",
                   "Role not found. Please provide a valid role id."]
        status << [200,
                   200,
                   404]
        id <<["1",
              "2",
              "2"]

        umrOutput <<["[Role(authority:Admin), Role(authority:Supervisor)]",
                     "[Role(authority:Admin), Role(authority:Supervisor)]",
                     "[Role(authority:Admin)]"]
    }

    @Unroll
    void "test remove Roles api"() {
        when: 'addRemoveRoles is called'
        request?.method = "PUT"
        request?.json = input
        controller?.addRemoveRoles()

        then: 'the corresponding roles should be removed from the microservice'
        response?.status == status
        response?.json.message == output
        def out = null
        if(findRoles(id)){
            out = findRoles(id).toString()
        }
        out == umrOutput


        where:
        input << [$/{"id":"1","roles":[{"id":"1"}, {"id":"2"}], "addRemove":"remove"}/$,
                  $/{"id":"2","roles":[{"id":"1"}], "addRemove":"remove"}/$,
                  $/{"id":"2","roles":[{"id":"2"}], "addRemove":"asdf"}/$,
                  $/{"id":"1","roles":[{"id":"2"}], "addRemove":"remove"}/$]

        output << ["Roles have been successfully removed.",
                   "Roles have been successfully removed.",
                   "Only add or remove is allowed in this method.",
                   "Roles cannot be removed since they're not assigned to the microservice."]
        status << [200,
                   200,
                   406,
                   200]
        id <<["1",
              "2",
              "2",
              "1"]

        umrOutput <<[null,
                     null,
                     "[Role(authority:Admin)]",
                     "[Role(authority:Admin)]"]
    }

    private findRoles(String id){
        def micro = Microservice.findById(id)
        return micro.roles.sort{it?.authority}
    }
}
