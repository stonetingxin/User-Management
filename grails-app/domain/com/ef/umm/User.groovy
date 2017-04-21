package com.ef.umm

//class User {
//    String email
//    String firstName
//    String lastName
//    String username
//    String password
//    String authProvider
//
//    static constraints = {
//        email(email: true, blank: true, nullable: true, matches: "^[a-zA-Z0-9!#\$%&'*+/=?^_`{|}~.-]+@[a-zA-Z]+\\.com\$")
//        firstName(maxSize: 100, blank: true, nullable: true)
//        lastName(maxSize: 100, blank: true, nullable: true)
//        username(nullable: false, blank: false, size: 2..20, matches: "^[a-zA-Z0-9][0-9a-zA-Z_.]*\$", unique: true)
//        password(minSize: 5, blank: false, nullable: false)
//        authProvider (blank:true)
//    }
//
//    String toString() {
//        return username
//    }
//}

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class User implements Serializable {

    private static final long serialVersionUID = 1

    transient springSecurityService

    String username
    String password
    String email
    String firstName
    String lastName
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    User(String username, String password) {
        this()
        this.username = username
        this.password = password
    }

    Set<Role> getAuthorities() {
        [new Role(authority: 'ROLE_NO_ROLES')]
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(password) : password
    }

    static transients = ['springSecurityService']

    static constraints = {
        username blank: false, unique: true
        password blank: false
        email nullable: true
        firstName nullable: true
        lastName nullable: true
    }

    static mapping = {
        password column: '`password`'
    }
}
