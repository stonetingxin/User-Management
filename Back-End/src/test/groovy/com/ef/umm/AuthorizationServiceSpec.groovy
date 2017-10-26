package com.ef.umm

import grails.converters.JSON
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.*
import grails.test.mixin.Mock

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AuthorizationService)
@Mock([User, Role, Permission, UMR, Microservice])
class AuthorizationServiceSpec extends Specification {

    def setup() {
        try {
            def ahmed = new User(username: "admin", password: "admin", type: "DB")
            def hamid = new User(username: "saqib", password: "nothing", type: "DB")
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
    void "test authIntercept method"(){
        given:
        service.metaClass.makeRestCall{def p, def r -> return apiResp()}
        service.metaClass.authToken{def abc -> return token()}
        service.metaClass.getUsernameFromSpring{return null}

        when: 'authIntercept is called'
        def request = [:]
        def params = [:]
        request.forwardURI = uri

        def abc = service?.authIntercept(request, params)

        then: 'Value of the username extracted'
        abc.toString() == output

        where:
        uri << ["/umm/user/list",
                "/umm/efadminpanel/user/list",
                "/umm/PCS/user/list",
                "/umm/CBR/user/list"]

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

    @Unroll
    void "test extractUsername api"() {
        when: 'extract username from the JWT'
        def abc = service?.extractUsername(input)

        then: 'Value of the username extracted'
        abc == output

        where:
        input << ["eyJhbGciOiJIUzI1NiJ9.eyJwcmluY2lwYWwiOiJINHNJQUFBQUFBQUFBSlZTUDBcL2JRQlJcL0RvbEFRbENvVk" +
                          "NRR3VoUzJ5cEhhTVZOQmdJU3NwR3FhQlNUUXhYNjRCK2M3OSs0TXlZSXl3Y0FBb2tXcTJxXC9BTjRHR" +
                          "kQxQ1ZnWldabFhlRzRNQ0Nlb3V0ZHpcL1wvXC9qMmYzVURGYVBnWWE4YUY4Vk9SeFZ6Nkp0VmN4Z2JE" +
                          "VEhQYjlUT0RPa0tiSTVaellJc21jSCs4RW5nQmxIaGs0WFd3eFhaWVZUQVpWeHZ0TFF4dHJhUGhnOUx4" +
                          "QStPbVpnbnVLcjN0UDNLSFN1TVRnWUxhKzEyQzRWV1laR0dvTW1uclNpNTJVcTR4V29XSlloYW9jTnVO" +
                          "M29SMGc5SnlKc3dnZEJnbGF3dU1BaGhsbWYybVNKV2pzZkRxM214bXVhZzIwZFlDR0VtWk1lVHVXWktt" +
                          "ZGRiZHZiTXBLY0YzMklOeUpcL1hvVUhkekR1bzdIbjlCQ1VHcHVaSm10aVVURmZGTjdzU0p2emR6Y25uM" +
                          "HA5Y3FBVkFuNzFcLytwcGhQejBQdmZQMzJiVjYwRjFxWUdyQmV3R3FkbE54TUZzeGZOVHJsdjc4K1wvemk" +
                          "5T1ZnYkltV0hXUHJcL2ZjeCtlbWl1dTZDU2xHbG0xY0NPaUhhMzdONkpmUDVsOHY0V3VuNlRKNmxBK3FPa" +
                          "3hlaFJvaUNtdUdXdFJMOXZDMk5mR3NIaVJyMng0WjVOTjZtd0tPR1NoTWZ6M0c1aGZxQm9YWWZYeHhkSDdc" +
                          "LzRSeVFwVWRwaklrR3FmS0VEMUxHbWozajg3blJuOWVYV1loK2pcLzBIZG0rVnVGRkFNQUFBPT0iLCJzdWIi" +
                          "OiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfTk9fUk9MRVMiXSwiZXhwIjoxNTAyODMwMTI4LCJpYXQiOjE1MDI3" +
                          "OTQxMjh9.DfSQ7cKVYCCcSmZCbLT_j5EPcQk1IsagJFosvuvLfYo",
                  "eyJhbGciOiJIUzI1NiJ9.eyJwcmluY2lwYWwiOiJINHNJQUFBQUFBQUFBSlZTUDBcL2JRQlJcL1RoTlJDWlVcL2xhakV" +
                          "RQmRnUTQ1RXgwd0ZBUkt5a3FvaEM1VkFGXC92aEhwenZ6TjBaa2dWbG9nTURWVnVrQ3I0QzM2UmQrZ0VRSFZ" +
                          "pWldYbG5BZzRzaUZ0c3ZmdjU5K1wvNVwvQm9xUnNPSFdETXVqSitLTE9iU042bm1NallZWnByYnJwOFoxQkhh" +
                          "SExHU0ExczBnYnZqbGNBTG9NUWpDMitEYmJiSHFvTEp1TnBvYjJOb2F4ME44MHJIZmNZdHpSTGNWM3JIZitBT" +
                          "2xjWkhBZ1cxZDFxQ29YVVlaMkdvTW1uclNpNTFVcTR4V29leFloYW9jTWVOSmtLNlFXazVFMllRT29TU3RRVk" +
                          "dBUXl6ekg1VnBNclJXQmk5TTV0WkxxcE50TFVBWHFmTUdITDNKRW5UT3V2dTN0bVVsR0FYRHFEY1NUMDYxTjJ" +
                          "zZ1wvcU94MTlVUWxCcXJxU1phY2xFUlh5TE8zSGk3MDM5K0hkODFtdVZBS2lUdWVlXC9LZWFUQzlEN3MzSHpQ" +
                          "aVwvYUN5MjhHN0Jld0dxZGxOeU1GOHhyR3AzeXhlOVBQMCt1djMxNVJjb09zZnp5ZmN4ODdEZlhYVlJKeWpTem" +
                          "FtQkhSTHRmZHU5RXZ2QTgrZjBXdW42VEo2bEErcU9reGVoQm9pQ211R1d0eEgzZkZ0NThiZ1JMbVwvWEdwbnMy" +
                          "M2FSaTJDNXZrXC9CSW50c3R6QThVcmV2bzZ2dmY0K2xMSWxtRnloNFRHVkx0WXdXb25pVnQxSWZuSjFQRHZcLz" +
                          "RmNVNINlAzVGxGaHpzejZzVUF3QUEiLCJzdWIiOiJzYXFpYiIsInJvbGVzIjpbIlJPTEVfTk9fUk9MRVMiXSwiZ" +
                          "XhwIjoxNTAyODMyMjkxLCJpYXQiOjE1MDI3OTYyOTF9.mAnwWIwcojYPsm15Gx19mMLJ_PuRGChye1QZB5SorBg"]

        output<< ["admin",
                  "saqib"]
    }

    @Unroll
    void "test extractURI method" (){
        when: 'extract URI from the input'
        def abc = service?.extractURI(input)

        then: 'Value of the username extracted'
        abc.toString() == output

        where:
        input << ["/umm/user/list",
                  "/umm/efadminpanel/user/isAuthentic",
                  "/umm/abc/def/eiq/2"]

        output<< ["[umm, user, list]",
                "[efadminpanel, user, isAuthentic]",
                "[abc, def, eiq]"
                ]
    }

    @Unroll
    void "test hasPermission method" (){
        when: 'check permission of a user'
        def user = User.findById(userID)
        def micro = Microservice.findById(microID)
        def permission = Permission.findById(permissionID)
        def abc = service?.hasPermission(user, micro, permission)

        then: 'Whether the user has required permission in said microservice'
        abc == output

        where:
        userID << [1,1,1,1,1,1,2,2,2,2,2,2]

        microID << [1,1,1,2,2,2,1,1,1,2,2,2]

        permissionID << [1,2,3,1,2,3,1,2,3,1,2,3]

        output<< [true,false,false,false,false,false,false,false,false,false,true,true]
    }

    private token(){
        return "Bearer eyJhbGciOiJIUzI1NiJ9.eyJwcmluY2lwYWwiOiJINHNJQUFBQUFBQUFBSlZTUDBcL2JRQlJc" +
                "L0RvbEFRbENvVkNRR3VoUzJ5cEhhTVZOQmdJU3NwR3FhQlNUUXhYNjRCK2M3OSs0TXlZSXl3Y0FBb2tXc" +
                "TJxXC9BTjRHRkQxQ1ZnWldabFhlRzRNQ0Nlb3V0ZHpcL1wvXC9qMmYzVURGYVBnWWE4YUY4Vk9SeFZ6Nk" +
                "p0VmN4Z2JEVEhQYjlUT0RPa0tiSTVaellJc21jSCs4RW5nQmxIaGs0WFd3eFhaWVZUQVpWeHZ0TFF4dH" +
                "JhUGhnOUx4QStPbVpnbnVLcjN0UDNLSFN1TVRnWUxhKzEyQzRWV1laR0dvTW1uclNpNTJVcTR4V29XSl" +
                "loYW9jTnVOM29SMGc5SnlKc3dnZEJnbGF3dU1BaGhsbWYybVNKV2pzZkRxM214bXVhZzIwZFlDR0VtWk1" +
                "lVHVXWkttZGRiZHZiTXBLY0YzMklOeUpcL1hvVUhkekR1bzdIbjlCQ1VHcHVaSm10aVVURmZGTjdzU0p2e" +
                "mR6Y25uMHA5Y3FBVkFuNzFcLytwcGhQejBQdmZQMzJiVjYwRjFxWUdyQmV3R3FkbE54TUZzeGZOVHJsdjc" +
                "4K1wvemk5T1ZnYkltV0hXUHJcL2ZjeCtlbWl1dTZDU2xHbG0xY0NPaUhhMzdONkpmUDVsOHY0V3VuNlRKN" +
                "mxBK3FPa3hlaFJvaUNtdUdXdFJMOXZDMk5mR3NIaVJyMng0WjVOTjZtd0tPR1NoTWZ6M0c1aGZxQm9YWWZ" +
                "YeHhkSDdcLzRSeVFwVWRwaklrR3FmS0VEMUxHbWozajg3blJuOWVYV1loK2pcLzBIZG0rVnVGRkFNQUFBP" +
                "T0iLCJzdWIiOiJhZG1pbiIsInJvbGVzIjpbIlJPTEVfTk9fUk9MRVMiXSwiZXhwIjoxNTAyOTA4MTE1LCJ" +
                "pYXQiOjE1MDI4NzIxMTV9.88nQZf9TgeSDtY2i_hpkx7eFdF-nNLHcUMTCvx2dElM"
    }
    private apiResp(){
        def resp = [:]
        resp.responseEntity= [statusCode:[value:200], body:["abc": "def"]]
        resp.json = ["self":"http://192.168.1.100/adminapi/application/workspace_application",
                     "ScriptApplication":
                             ["script":"SCRIPT[sss/service_status.aef]",
                              "scriptParams":[
                                      [
                                              "name":"AppServerIP",
                                              "value":"\"192.168.1.88\"",
                                              "type":"java.lang.String"
                                      ]
                              ]
                             ],
                     "id":"1551",
                     "applicationName":"workspace_application",
                     "type":"Cisco Script Application",
                     "description":"workspace_application",
                     "maxsession":2345,
                     "enabled":"true"]

        return resp

    }
}
