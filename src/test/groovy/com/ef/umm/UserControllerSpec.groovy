package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.*
import grails.test.mixin.Mock

@TestFor(UserController)
@Mock([User, Role, Permission, UMR, Microservice])
class UserControllerSpec extends Specification {

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
            output['description'] = it?.description
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
            output['createdBy']= it?.createdBy?.id
            output['updatedBy']= it?.updatedBy?.id
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

    def cleanup() {
    }

    @Unroll
    void "test create api"() {
        when: 'save method is called with a json containing user details'
        request?.method = "POST"
        request?.json = input
        controller?.create()

        then: 'a new user should be created incrementing the total user count by 1'
        response?.status == status
        response?.json.message == output
        User.count() == count

        where:
        input << [$/{"username":"asif","password":"blahblah", "isActive":true, "type": "DB"}/$,
                  $/{"username":"ahmed", "password":"asdasd", "isActive":true, "type": "DB"}/$,
                  $/{"usernam":"asif"}/$]

        output<< ["New user: 'asif' has been created successfully.",
                  "User with username: 'ahmed' already exists.",
                  "Invalid JSON provided. Please read the API specifications."]

        count <<[4,
                 3,
                 3]

        status <<[200,
                  406,
                  406]
    }

//    void "test list api"() {
//        when: 'list function is called '
//        controller?.list()
//
//        then: 'a json response of complete list of users with corresponding microservices,' +
//                'roles and permissions should be returned.'
//        response?.status == 200
//        response?.json.users[0].username == "ahmed"
//        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStatus\",\"n" +
//                "ame\":\"OK\"},\"users\":[{\"id\":1,\"username\":\"ahmed\",\"email\":null,\"firs" +
//                "tName\":null,\"lastName\":null,\"microservices\":[{\"id\":1,\"name\":\"PCS\",\"" +
//                "description\":\"Post Call Survey\",\"roles\":[{\"id\":1,\"name\":\"ROLE_ADMIN\"," +
//                "\"description\":\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"com.app." +
//                "ef.admin\",\"expression\":\"*:*\"}]}]}]},{\"id\":2,\"username\":\"hamid\",\"email" +
//                "\":null,\"firstName\":null,\"lastName\":null,\"microservices\":[{\"id\":2,\"name\"" +
//                ":\"CBR\",\"description\":\"Caller Based Routing\",\"roles\":[{\"id\":1,\"name\":" +
//                "\"ROLE_ADMIN\",\"description\":\"Administrator\",\"permissions\":[{\"id\":1,\"name" +
//                "\":\"com.app.ef.admin\",\"expression\":\"*:*\"}]}]}]},{\"id\":3,\"username\":\"admi" +
//                "n\",\"email\":null,\"firstName\":null,\"lastName\":null,\"microservices\":[]}]}"
//    }

    void "test show api"() {
        when: 'show is called for a valid user id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with user having that id along with the ' +
                'corresponding microservices, roles and permissions'
        response?.status == 200
        response?.json?.user?.microservices[0]?.name == 'PCS'
        response?.text == "{\"status\":{\"enumType\":\"org.springframework.http.HttpStatus\",\"name\":\"OK\"}," +
                "\"user\":{\"id\":1,\"username\":\"ahmed\",\"email\":null,\"fullName\":null,\"type\":\"DB\"," +
                "\"isActive\":false,\"profileExists\":false,\"lastLogin\":null,\"createdBy\":null,\"updatedBy" +
                "\":null,\"microservices\":[{\"id\":1,\"name\":\"PCS\",\"ipAddress\":\"https://192.168.1.79:8080" +
                "\",\"description\":\"Post Call Survey\",\"roles\":[{\"id\":1,\"name\":\"ROLE_ADMIN\",\"description" +
                "\":\"Administrator\",\"permissions\":[{\"id\":1,\"name\":\"com.app.ef.admin\",\"expression\":" +
                "\"*:*\",\"description\":null}]}]}]}}"
    }

    @Unroll
    void "test delete api"() {
        when: 'delete is called with a valid user id'
        request?.method = "DELETE"
        request?.setParameter("id", input)
        controller?.delete()

        then: 'user having that id should be deleted decrementing the user count by 1'
        response?.status == status
        User.count() == count
        response?.json.message == output

        where:

        input <<["1",
                 "2",
                 "3"]

        output<< ["Successfully deleted user: ahmed",
                  "Successfully deleted user: hamid",
                  "This user cannot be deleted. Permission denied."]

        count <<[2,
                 2,
                 3]

        status <<[200,
                  200,
                  406]
    }

    void "test deleteMulti api"() {
        when: 'delete is called with an array of valid user ids'
        request?.method = "DELETE"
        request?.json = $/[{"id":"1"},{"id":"2"},{"id":"3"},{"id":"4"}]/$
        controller?.deleteMulti()

        then: 'user having that id should be deleted decrementing the user count by 1'
        response?.status == status
        response?.json.message.toString() == "[\"Successfully deleted user: ahmed\",\"Successfully " +
                "deleted user: hamid\",\"User: admin cannot be deleted. Permission denied.\",\"User" +
                " with ID: 4 not found. Provide a valid user id.\"]"

    }

    @Unroll
    void "test update api"() {

        when: 'update is called for an existing user'
        request?.method = "PUT"
        request?.json = input
        controller?.update()

        then: 'User information should be updated according to the given JSON'
        response?.status == status
        response?.json.toString() == output

        where:
        input << [$/{"id":"2","fullName":"blahblah", "description":"bleh blehbleh"}/$,
                  $/{"id":"1","fullName":"blahblah", "isActive":true}/$,
                  $/{"id":"4"}/$]

        output << ["{\"lastLogin\":null,\"updatedBy\":null,\"createdBy\":null,\"fullName\":\"blahblah\"" +
                           ",\"id\":2,\"type\":\"DB\",\"isActive\":false,\"email\":null,\"profileExists" +
                           "\":false,\"username\":\"hamid\",\"microservices\":[{\"roles\":[{\"permissions" +
                           "\":[{\"expression\":\"*:*\",\"name\":\"com.app.ef.admin\",\"description\":null," +
                           "\"id\":1}],\"name\":\"ROLE_ADMIN\",\"description\":\"Administrator\",\"id\":1}]," +
                           "\"name\":\"CBR\",\"ipAddress\":\"http://192.168.1.79:8080\",\"description\":" +
                           "\"Caller Based Routing\",\"id\":2}]}",
                   "{\"lastLogin\":null,\"updatedBy\":null,\"createdBy\":null,\"fullName\":\"blahblah\"," +
                           "\"id\":1,\"type\":\"DB\",\"isActive\":true,\"email\":null,\"profileExists\":false," +
                           "\"username\":\"ahmed\",\"microservices\":[{\"roles\":[{\"permissions\":[{\"expression" +
                           "\":\"*:*\",\"name\":\"com.app.ef.admin\",\"description\":null,\"id\":1}],\"name\":" +
                           "\"ROLE_ADMIN\",\"description\":\"Administrator\",\"id\":1}],\"name\":\"PCS\",\"ipAddress" +
                           "\":\"https://192.168.1.79:8080\",\"description\":\"Post Call Survey\",\"id\":1}]}",
                   "{\"message\":\"User not found. Invalid update request. Call create API to create a new user.\"," +
                           "\"status\":{\"enumType\":\"org.springframework.http.HttpStatus\",\"name\":\"NOT_FOUND\"}}"]

        status << [200,
                   200,
                   404]

    }

    @Unroll
    void "test add Microservice Roles api"() {
        when: 'addRevokeMicroserviceRoles is called for a new user-microservice association'
        request?.method = "PUT"
        request?.json = input
        controller?.addRevokeMicroserviceRoles()

        then: 'the corresponding role microservice should be added for the user'
        response?.status == status
        def message = response?.json.message
        message.toString() == output
        def out = null
        if(findUMR(u, m)){
            out = findUMR(u, m).toString()
        }
        out == umrOutput

        where:
        input << [$/{"id":"1","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"2","roles":[{"id":"2"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"2"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"3","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"1","roles":[{"id":"22"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"2","roles":[{"id":"54"},{"id":"2"}]},/$+
                          $/{"id":"2"}],"addRevoke":"add"}/$]

        output << ["[\"User: ahmed already has ROLE_ADMIN role in PCS.\",\"Successfully added " +
                           "Supervisor role in PCS for user: ahmed\",\"Successfully added ROLE_" +
                           "ADMIN role in CBR for user: ahmed\",\"Successfully added Supervisor " +
                           "role in CBR for user: ahmed\"]",
                   "[\"Successfully added Supervisor role in CBR for user: ahmed\",\"User: ahmed " +
                           "already has Supervisor role in CBR.\",\"User: ahmed already has Supervisor" +
                           " role in CBR.\",\"User: ahmed already has Supervisor role in CBR.\"]",
                   "[\"Microservice with id: 3 not found.\",\"Successfully added ROLE_ADMIN role in CBR " +
                           "for user: ahmed\",\"Successfully added Supervisor role in CBR for user: ahmed\"]",
                   "[\"Role with id: 22 not found.\",\"Successfully added Supervisor role in PCS for " +
                           "user: ahmed\",\"Successfully added ROLE_ADMIN role in CBR for user: ahmed\"," +
                           "\"Successfully added Supervisor role in CBR for user: ahmed\"]",
                   "Invalid JSON provided. Please read the API specifications."]
        status << [200,
                   200,
                   200,
                   200,
                   406]
        u <<["ahmed",
             "ahmed",
             "ahmed",
             "ahmed",
             "ahmed"]

        m <<  ["CBR",
               "CBR",
               "CBR",
               "CBR",
               "CBR"]

        umrOutput <<["ahmed as ROLE_ADMIN in CBR",
                     "ahmed as Supervisor in CBR",
                     "ahmed as ROLE_ADMIN in CBR",
                     "ahmed as ROLE_ADMIN in CBR",
                     "ahmed as Supervisor in CBR"]
    }

    @Unroll
    void "test revoke Microservice Roles api"() {
        when: 'addRevokeMicroserviceRoles is called'
        request?.method = "PUT"
        request?.json = input
        controller?.addRevokeMicroserviceRoles()

        then: 'the corresponding role-microservice should be revoked for the user'
        response?.status == status
        def message = response?.json.message
        message.toString() == output
        findUMR(u, m) == umrOutput


        where:
        input << [$/{"id":"1","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"revoke"}/$,
                  $/{"id":"1","microservices":[{"id":"3","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "4"}]}],"addRevoke":"revoke"}/$,
                  $/{"id":"1","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2"}],"addRevoke":"revoke"}/$,
                  $/{"id":"4","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"revoke"}/$,
                  $/{"id":"1","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"": "2"}]}],"addRevoke":"revoke"}/$,
                  $/{"id":"1","microservices":[{"id":"1","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"remove"}/$]

        output << [$/["Successfully revoked ROLE_ADMIN role in PCS for user: ahmed","User: ahmed/$+
                           $/ does not have Supervisor role in PCS.","User: ahmed does not have /$+
                           $/ROLE_ADMIN role in CBR.","User: ahmed does not have Supervisor role in CBR."]/$,
                   $/["Microservice with id: 3 not found.","User: ahmed does not have /$+
                           $/ROLE_ADMIN role in CBR.","Role with id: 4 not found."]/$,
                   "Invalid JSON provided. Please read the API specifications.",
                   "User not found. Invalid add/revoke request",
                   "Invalid JSON provided. Please read the API specifications.",
                   "Only add or revoke is allowed in this method."]
        status << [200,
                   200,
                   406,
                   404,
                   406,
                   406]
        u <<["ahmed",
             "ahmed",
             "ahmed",
             "ahmed",
             "ahmed",
             "ahmed"]

        m <<  ["PCS",
               "CBR",
               "CBR",
               "CBR",
               "CBR",
               "CBR"]

        umrOutput <<[null,
                     null,
                     null,
                     null,
                     null,
                     null]
    }

    private UMR findUMR(String user, String micro){
        def u = User.findByUsername(user)
        def m = Microservice.findByName(micro)
        def umr = UMR.findByUsersAndMicroservices(u, m)
        return umr
    }

}
