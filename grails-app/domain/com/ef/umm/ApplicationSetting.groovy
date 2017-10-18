package com.ef.umm

class ApplicationSetting {
    static int CISCO_UCCX = 0
    static int CISCO_UCCE = 1

    Integer ciscoType
//    String applicationName // adminpanel, ecm, both

    // setting for the ucce prompt network
    String domain, username, password, machineIp, sharedFolder
    //setting for ucce Database
    String databaseMachineIp, databaseName, databaseUsername, databasePassword
    //Secondary and primary UCCX server
    String secondaryIp, secondaryUsername, secondaryPassword
    String primaryIp, primaryUsername, primaryPassword
    String webRequest

    static constraints = {
//        applicationName nullable: false, blank: false
        domain nullable: true, blank: true
        username nullable: true, blank: true
        password nullable: true, blank: true
        machineIp nullable: true, blank: true
        sharedFolder nullable: true, blank: true
        databaseMachineIp nullable: true, blank: true
        databaseName nullable: true, blank: true
        databaseUsername nullable: true, blank: true
        databasePassword nullable: true, blank: true
        primaryIp nullable: true, blank: true
        primaryUsername nullable: true, blank: true
        primaryPassword nullable: true, blank: true
        secondaryIp nullable: true, blank: true
        secondaryUsername nullable: true, blank: true
        secondaryPassword nullable: true, blank: true
        webRequest nullable: true, blank: true
    }

}
