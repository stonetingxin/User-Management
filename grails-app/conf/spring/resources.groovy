// Place your Spring DSL code here
import grails.plugin.springsecurity.SpringSecurityUtils
beans = {
//    def config = SpringSecurityUtils.securityConfig
//    myLdapAuthenticator(org.springframework.security.ldap.authentication.BindAuthenticator, ref("contextSource")) {
//        userDnPatterns = ['uid={0},dc=example,dc=com']
//    }
//    SpringSecurityUtils.loadSecondaryConfig 'DefaultLdapSecurityConfig'
//    config = SpringSecurityUtils.securityConfig
//
//    initialDirContextFactory(org.springframework.security.ldap.DefaultSpringSecurityContextSource,
//            config.ldap.context.server){
//        userDn = config.ldap.context.managerDn
//        password = config.ldap.context.managerPassword
//    }
//
//    ldapUserSearch(org.springframework.security.ldap.search.FilterBasedLdapUserSearch,
//            config.ldap.search.base,
//            config.ldap.search.filter,
//            initialDirContextFactory){
//    }
//
//    ldapAuthoritiesPopulator(org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator,
//            initialDirContextFactory,
//            config.ldap.authorities.groupSearchBase){
//        groupRoleAttribute = config.ldap.authorities.groupRoleAttribute
//        groupSearchFilter = config.ldap.authorities.groupSearchFilter
//        searchSubtree = config.ldap.authorities.searchSubtree
//        rolePrefix = "ROLE_"
//        convertToUpperCase = config.ldap.mapper.convertToUpperCase
//        ignorePartialResultException = config.ldap.authorities.ignorePartialResultException
//    }
//
//    userDetailsService(org.springframework.security.ldap.userdetails.LdapUserDetailsService,
//            ldapUserSearch,
//            ldapAuthoritiesPopulator){
//    }

}
