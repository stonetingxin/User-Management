package com.ef.umm

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RestService)
@Mock([User, Role, Permission, UMR, Microservice])
class RestServiceSpec extends Specification {

    def setup() {
        try {
            def ahmed = new User(username: "admin", password: "admin", type: "DB")
            def hamid = new User(username: "saqib", password: "nothing", type: "CC")
            def userAdmin = new User(username: "admin", password: "admin", type: "AD")
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
            def umm = new Microservice(name: 'umm', ipAddress: "http://127.0.0.1:9091",description: 'UserManagement Microservice')
            pcs?.save(failOnError: true)
            cbr?.save(failOnError: true)
            umm?.save(failOnError: true)


            UMR.create ahmed, admin, pcs, true
            UMR.create hamid, role, cbr, true
            UMR.create ahmed, admin, umm, true
            UMR.create hamid, role, umm, true

            UMR.withSession {
                it.flush()
                it.clear()
            }
        }
        catch (Exception ex){
            println ex.getMessage()
        }

    }

    def cleanup() {
    }

    @Unroll
    void "test APUserSync"() {
        given:
        service.metaClass.getAPName(){return 'efadminpanel'}

        when: 'authIntercept is called'
        service.metaClass.getCCXUserList{def p-> return userAPI(index)}
        def abc = service?.APUserSync()

        then: 'Value of the username extracted'
        abc == output
        def user = findUser(u)
        user.type == "CC"

        where:
        index << [1,2,3,4]
        output<< [200, 400, 600, 800]
        u<<["1", "2", "3", "4"]
    }

    @Unroll
    void "test CallApi method"(){
        given:
        service.metaClass.makeRestCall{def p, def r -> return apiResp()}
        service.metaClass.authToken{def abc -> return token()}
        service.metaClass.getUsernameFromSpring{return null}

        when: 'authIntercept is called'
        def request = [:]
        def params = [:]
        request.forwardURI = uri
        request.json = json

        def abc = service?.callAPI(params, request)

        then: 'Value of the username extracted'
        abc.toString() == output

        where:
        uri << ["/umm/user/list",
                "/umm/efadminpanel/user/list",
                "/umm/PCS/user/list",
                "/umm/CBR/user/list"]

        json << ["","",""]

        output<< ["[auth:true]",

                  "[status:403, resultSet:[status:403, message:Microservice: 'efadminpanel' " +
                          "does not exist. Contact system admin.], auth:false]",

                  "[status:200, resultSetJSON:[self:http://192.168.1.100/adminapi/application/workspace_application" +
                          ", ScriptApplication:[script:SCRIPT[sss/service_status.aef], scriptParams:" +
                          "[[name:AppServerIP, value:\"192.168.1.88\", type:java.lang.String]]], id:1551, " +
                          "applicationName:workspace_application, type:Cisco Script Application, description:" +
                          "workspace_application, maxsession:2345, enabled:true], auth:false]",

                  "[status:403, resultSet:[status:403, message:Access forbidden. User not " +
                          "authorized to request this resource.], auth:false]"]
    }


    private userAPI(def ind){
        def resp =[:]
        resp.json = [[username:ind, fullName: "cdf", profileExists: true],
                     [username:ind+1, fullName: "cdf", profileExists: false],
                     [username:ind+2, fullName: "cdf", profileExists: true],
                     [username:ind+3, fullName: "cdf", profileExists: false],
                     [username:ind+4, fullName: "cdf", profileExists: true]]
        resp.responseEntity = [statusCode: [value: ind * 200]]
        return resp
    }

    private User findUser(String user){
        def u = User.findByUsername(user)
        return u
    }
}
