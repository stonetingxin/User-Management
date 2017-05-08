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
        when: 'save method is called with a json containing user details'
        request?.method = "POST"
        request?.json = input
        controller?.create()

        then: 'a new user should be created incrementing the total user count by 1'
        response?.status == status
        User.count() == count
        response?.json.message == output

        where:
        input << [$/{"username":"asif","password":"blahblah"}/$,
                  $/{"username":"ahmed", "password":"asdasd"}/$,
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

    void "test list api"() {
        when: 'list function is called '
        controller?.list()

        then: 'a json response of complete list of users with corresponding microservices,' +
                'roles and permissions should be returned.'
        response?.status == 200
        response?.json.users[0].username == "ahmed"
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                $/"users":[{"id":1,"username":"ahmed","email":null,"firstName":null,"lastName":null,/$+
                $/"microServices":[{"id":1,"name":"PCS","description":"Post Call Survey","role":{"id":1,/$+
                $/"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app.ef.admin",/$+
                $/"expression":"*:*"}]}}]},{"id":2,"username":"hamid","email":null,"firstName":null,"lastName"/$+
                $/:null,"microServices":[{"id":2,"name":"CBR","description":"Caller Based Routing","role":/$+
                $/{"id":1,"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app./$+
                $/ef.admin","expression":"*:*"}]}}]},{"id":3,"username":"admin","email":null,"firstName":null,/$+
                $/"lastName":null,"microServices":[]}]}/$
    }

    void "test show api"() {
        when: 'show is called for a valid user id'
        request?.setParameter("id", "1")
        controller?.show()

        then: 'a json should be returned with user having that id along with the ' +
                'corresponding microservices, roles and permissions'
        response?.status == 200
        response?.json?.user?.microServices[0]?.name == 'PCS'
        response?.text == $/{"status":{"enumType":"org.springframework.http.HttpStatus","name":"OK"},/$+
                $/"user":{"id":1,"username":"ahmed","email":null,"firstName":null,"lastName":null,/$+
                $/"microServices":[{"id":1,"name":"PCS","description":"Post Call Survey","role":{"id":1,/$+
                $/"name":"Admin","description":"Administrator","permissions":[{"id":1,"name":"com.app.ef./$+
                $/admin","expression":"*:*"}]}}]}}/$
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

    void "test update api"() {
        when: 'update is called with existing user-microservice association'
        request?.method = "PUT"
        request?.json = $/{"id":"1", "firstName":"Ahmed", "lastName":"Khan", "email":"ahmed_khan@ahmed.com"}/$
        def ahmed = User.findByUsername("ahmed")
        controller?.update()

        then: 'the role of user for corresponding microservice should be updated as provided in the request json'
        response?.status == 200
        ahmed.firstName == "Ahmed"
        ahmed.lastName == "Khan"
        ahmed.email == "ahmed_khan@ahmed.com"
        response?.json.message == "User with username: 'ahmed' has been updated successfully."

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
                  $/{"id":"1","microservices":[{"id":"2","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"4"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"3","roles":[{"id":"1"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"1","roles":[{"id":"22"},{"id":"2"}]},/$+
                          $/{"id":"2","roles":[{"id":"1"},{"id": "2"}]}],"addRevoke":"add"}/$,
                  $/{"id":"1","microservices":[{"id":"2","roles":[{"id":"54"},{"id":"2"}]},/$+
                          $/{"id":"2"}],"addRevoke":"add"}/$]

        output << ["[\"User: ahmed already has Admin role in PCS.\",\"Successfully " +
                           "added Supervisor role in PCS for user: ahmed\",\"Successfully" +
                           " added Admin role in CBR for user: ahmed\",\"Successfully added" +
                           " Supervisor role in CBR for user: ahmed\"]",
                   "[\"Successfully added Admin role in CBR for user: ahmed\",\"Successfully" +
                           " added Supervisor role in CBR for user: ahmed\",\"Role with id: 4 not" +
                           " found.\",\"User: ahmed already has Supervisor role in CBR.\"]",
                   "[\"Microservice with id: 3 not found.\",\"Successfully added Admin role in CBR" +
                           " for user: ahmed\",\"Successfully added Supervisor role in CBR for user: ahmed\"]",
                   "[\"Role with id: 22 not found.\",\"Successfully added Supervisor role in PCS for " +
                           "user: ahmed\",\"Successfully added Admin role in CBR for user: ahmed\"" +
                           ",\"Successfully added Supervisor role in CBR for user: ahmed\"]",
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

        umrOutput <<["ahmed as Admin in CBR",
                     "ahmed as Admin in CBR",
                     "ahmed as Admin in CBR",
                     "ahmed as Admin in CBR",
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

        output << [$/["Successfully revoked Admin role in PCS for user: ahmed","User: ahmed /$+
                           $/does not have Supervisor role in PCS.","User: ahmed does not have /$+
                           $/Admin role in CBR.","User: ahmed does not have Supervisor role in CBR."]/$,
                   $/["Microservice with id: 3 not found.","User: ahmed does not have Admin role in /$+
                           $/CBR.","Role with id: 4 not found."]/$,
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
