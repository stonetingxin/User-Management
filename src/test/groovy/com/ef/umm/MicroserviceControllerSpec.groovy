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

            def admin = new Role(authority: "ROLE_ADMIN", description: "Administrator")
            admin.addToPermissions(name: "com.app.ef.admin", expression: "*:*")
            admin?.save()

            def role = new Role(authority: "Supervisor", description: "Supervisor role")
            role.addToPermissions(name: "com.app.ef.show", expression: "show:*")
            role.addToPermissions(name: "com.app.ef.update", expression: "update:*")
            role?.save()


            def pcs = new Microservice(name: 'PCS', ipAddress: "https://192.168.1.79:8080", description: 'Post Call Survey')
            def cbr = new Microservice(name: 'CBR', ipAddress: "http://192.168.1.79:8080", description: 'Caller Based Routing')
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
        Microservice.count() == count
        response?.json.message == output

        where:
        input << [$/{"name":"UMM", "ipAddress":"http://127.0.0.1:8080","description":"User Management Microservice"}/$,
                  $/{"name":"CBR", "ipAddress":"http://192.168.1.79:8080", "password":"asdasd"}/$,
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
        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStatus" +
                "\",\"name\":\"OK\"},\"microservices\":[{\"id\":1,\"name\":\"PCS\",\"descr" +
                "iption\":\"Post Call Survey\",\"roles\":[{\"id\":1,\"name\":\"ROLE_ADMIN\"" +
                ",\"description\":\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"" +
                "com.app.ef.admin\",\"expression\":\"*:*\"}]}]},{\"id\":2,\"name\":\"CBR\"," +
                "\"description\":\"Caller Based Routing\",\"roles\":[{\"id\":1,\"name\":\"R" +
                "OLE_ADMIN\",\"description\":\"Administrator\",\"permissions\":[{\"id\":1,\"" +
                "name\":\"com.app.ef.admin\",\"expression\":\"*:*\"}]}]}]}"
    }

    void "test show api"() {
        when: 'show is called for a valid microservice id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with microservice having that id along with the ' +
                'corresponding roles and permissions'
        response?.status == 200
        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStatus\",\"" +
                "name\":\"OK\"},\"microservice\":{\"id\":1,\"name\":\"PCS\",\"description\":\"P" +
                "ost Call Survey\",\"roles\":[{\"id\":1,\"name\":\"ROLE_ADMIN\",\"description\":" +
                "\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"com.app.ef.admin\",\"" +
                "expression\":\"*:*\"}]}]}}"
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
                 "3",
                 "2"]

        output<< ["Successfully deleted microservice: PCS",
                  "MicroService not found. Provide a valid microservice instance.",
                  "Successfully deleted microservice: CBR"]

        count <<[1,
                 2,
                 0]

        status <<[200,
                  404,
                  200]
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

        input <<[$/{"id":"1", "name":"UMM", "ipAddress":"http://127.0.0.1:8080","description":"User Management Microservice"}/$,
                 $/{"id":"2", "name":"CBR","ipAddress":"http://127.0.0.1:8080"}/$,
                 $/{"id":"3", "name":"CBR"}/$]

        output<< ["UMM has been updated successfully.",
                  "CBR has been updated successfully.",
                  "Invalid JSON provided. Please read the API specifications."]


        status <<[200,
                  200,
                  406]
    }

    @Unroll
    void "test add Roles api"() {
        when: 'addRemovePermissions is called with add functionality'
        request?.method = "PUT"
        request?.json = input
        controller?.addRemovePermissions()

        then: 'the corresponding permission should be added for the role'
        response?.status == status
        def message =response?.json.message
        message.toString() == output
        def out = null
        if(findRoles(id)){
            out = findRoles(id).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","roles":[{"id":"1"}, {"id":"2"}], "addRemove":"add"}/$,
                  $/{"id":"2","roles":[{"id":"2"}], "addRemove":"add"}/$,
                  $/{"id":"2","roles":[{"id":"3"}], "addRemove":"add"}/$]

        output << ["[\"Role: ROLE_ADMIN has already been assigned in the microservice." +
                           "\",\"Role: Supervisor has been successfully added.\"]",
                   $/["Role: Supervisor has been successfully added."]/$,
                   $/["Role with id: 3 not found."]/$]
        status << [200,
                   200,
                   200]
        id <<["1",
              "2",
              "2"]

        umrOutput <<["[Role(authority:ROLE_ADMIN), Role(authority:Supervisor)]",
                     "[Role(authority:ROLE_ADMIN), Role(authority:Supervisor)]",
                     "[Role(authority:ROLE_ADMIN)]"]
    }

    @Unroll
    void "test remove Roles api"() {
        when: 'addRemovePermissions is called'
        request?.method = "PUT"
        request?.json = input
        controller?.addRemovePermissions()

        then: 'the corresponding roles should be removed from the microservice'
        response?.status == status
        def message =response?.json.message
        message.toString() == output
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

        output << ["[\"Role: ROLE_ADMIN has been successfully removed.\",\"" +
                           "Role: Supervisor cannot be removed since it's not been assigned.\"]",
                   "[\"Role: ROLE_ADMIN has been successfully removed.\"]",
                   "Only add or remove is allowed in this method.",
                   $/["Role: Supervisor cannot be removed since it's not been assigned."]/$]
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
                     "[Role(authority:ROLE_ADMIN)]",
                     "[Role(authority:ROLE_ADMIN)]"]
    }

    private findRoles(String id){
        def micro = Microservice.findById(id)
        return micro.roles.sort{it?.authority}
    }
}
