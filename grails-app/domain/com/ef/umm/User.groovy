package com.ef.umm

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
    String fullName
    String type
    String lastLogin
    String dateCreated
    String lastUpdated
    User createdBy
    User updatedBy
    boolean isActive
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    boolean profileExists

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
        email nullable: true, matches: /^[a-z0-9!#$%&'*+\/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/
        fullName(maxSize: 100, blank: true, nullable: true)
        lastLogin(nullable: true)
        createdBy(nullable: true)
        updatedBy(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
    }

    static mapping = {
        table('db_user')
        password column: '`password`'
    }
}
