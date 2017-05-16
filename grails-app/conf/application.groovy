/**
 * Created by saqib ahmad on 4/18/2017.
 */

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.ef.umm.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.ef.umm.UMR'
grails.plugin.springsecurity.authority.className = 'com.ef.umm.Role'
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.sch.strategyName = org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL
grails.plugin.springsecurity.interceptUrlMap = [
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/error',          access: ['permitAll']],
        [pattern: '/index',          access: ['permitAll']],
        [pattern: '/index.gsp',      access: ['permitAll']],
        [pattern: '/shutdown',       access: ['permitAll']],
        [pattern: '/assets/**',      access: ['permitAll']],
        [pattern: '/**/js/**',       access: ['permitAll']],
        [pattern: '/**/css/**',      access: ['permitAll']],
        [pattern: '/**/images/**',   access: ['permitAll']],
        [pattern: '/**/favicon.ico', access: ['permitAll']],
        [pattern: '/login',          access: ['permitAll']],
        [pattern: '/login/**',       access: ['permitAll']],
        [pattern: '/logout',         access: ['permitAll']],
        [pattern: '/logout/**',      access: ['permitAll']],
        [pattern: '/console/**',     access: ["hasIpAddress(\'127.0.0.1\') || hasIpAddress(\'::1\')"]],
        [pattern: '/static/console/**', access: ["hasIpAddress(\'127.0.0.1\') || hasIpAddress(\'::1\')"]],
        [pattern: '/**',             access: ['ROLE_NO_ROLES']]
]


grails.plugin.springsecurity.filterChain.chainMap = [
        [pattern: '/assets/**',      filters: 'none'],
        [pattern: '/**/js/**',       filters: 'none'],
        [pattern: '/**/css/**',      filters: 'none'],
        [pattern: '/**/images/**',   filters: 'none'],
        [pattern: '/**/favicon.ico', filters: 'none'],
        [pattern: '/console/**',     filters: 'none'],
        [pattern: '/static/console/**', filters: 'none'],
        [pattern: '/**',             filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,' +
                                    '-exceptionTranslationFilter,-authenticationProcessingFilter,' +
                                    '-securityContextPersistenceFilter,-rememberMeAuthenticationFilter']
]