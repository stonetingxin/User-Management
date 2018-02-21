warning: LF will be replaced by CRLF in grails-app/controllers/com/ef/umm/SecurityInterceptor.groovy.
The file will have its original line endings in your working directory.
[1mdiff --git a/grails-app/controllers/com/ef/umm/SecurityInterceptor.groovy b/grails-app/controllers/com/ef/umm/SecurityInterceptor.groovy[m
[1mindex 6f7b72f..07f2fa5 100644[m
[1m--- a/grails-app/controllers/com/ef/umm/SecurityInterceptor.groovy[m
[1m+++ b/grails-app/controllers/com/ef/umm/SecurityInterceptor.groovy[m
[36m@@ -1,6 +1,8 @@[m
 package com.ef.umm[m
 [m
 import grails.converters.JSON[m
[32m+[m[32mimport grails.transaction.Transactional[m
[32m+[m
 import static org.springframework.http.HttpStatus.*[m
 [m
 class SecurityInterceptor {[m
[36m@@ -14,6 +16,7 @@[m [mclass SecurityInterceptor {[m
                 .excludes(uri: "/umm/static/console/**")[m
     }[m
 [m
[32m+[m[32m    @Transactional[m
     boolean before() {[m
         def resultSet = [:][m
         def user[m
[36m@@ -21,31 +24,28 @@[m [mclass SecurityInterceptor {[m
         def controller[m
         def action[m
         def micro[m
[31m-        def req = request.forwardURI - "/umm"[m
[31m-        resultSet.put("status", FORBIDDEN)[m
[31m-        resultSet.put("message", "Access forbidden. User not authorized to request this resource.")[m
[32m+[m[32m        try{[m
[32m+[m[32m            def req = request.forwardURI - "/umm"[m
[32m+[m[32m            resultSet.put("status", FORBIDDEN)[m
[32m+[m[32m            resultSet.put("message", "Access forbidden. User not authorized to request this resource.")[m
 [m
[31m-        if(!request?.getHeader("Authorization")){[m
[31m-            log.info("Access denied. Token not provided in the header.")[m
[31m-            resultSet.put("message", "Access denied. Token not provided in the header.")[m
[31m-            response.status = 403[m
[31m-            render resultSet as JSON[m
[31m-            return false[m
[31m-        }[m
[32m+[m[32m            if(!request?.getHeader("Authorization")){[m
[32m+[m[32m                log.info("Access denied. Token not provided in the header.")[m
[32m+[m[32m                resultSet.put("message", "Access denied. Token not provided in the header.")[m
[32m+[m[32m                response.status = 403[m
[32m+[m[32m                render resultSet as JSON[m
[32m+[m[32m                return false[m
[32m+[m[32m            }[m
 [m
[31m-        def userName= authorizationService.extractUsername(request?.getHeader("Authorization"))[m
[31m-        (microName, controller, action) = authorizationService.extractURI(request.forwardURI)[m
[32m+[m[32m            def userName= authorizationService.extractUsername(request?.getHeader("Authorization"))[m
[32m+[m[32m            (microName, controller, action) = authorizationService.extractURI(request.forwardURI)[m
 [m
[31m-        log.info("Requested URI is: " + request.forwardURI)[m
[31m-        log.info("Name of the microservice is: " + microName)[m
[31m-        log.info("Name of the controller is: " + controller)[m
[31m-        log.info("Name of the action is: " + action)[m
[31m-        log.info("Logged in user is " + userName)[m
[32m+[m[32m            log.info("Requested URI is: " + request.forwardURI)[m
[32m+[m[32m            log.info("Name of the microservice is: " + microName)[m
[32m+[m[32m            log.info("Name of the controller is: " + controller)[m
[32m+[m[32m            log.info("Name of the action is: " + action)[m
[32m+[m[32m            log.info("Logged in user is " + userName)[m
 [m
[31m-        if(!User.findByUsername(userName)){[m
[31m-            new User(userName:userName, password: password).save(flush: true, failOnError: true)[m
[31m-            log.info("Saved the LDAP user \"${userName}\" in local DB.")[m
[31m-        }[m
 //        To test the logging of various levels in the application. Uncomment following[m
 //        lines and each level of logging will invoke corresponding logback configuration.[m
 //[m
[36m@@ -61,80 +61,86 @@[m [mclass SecurityInterceptor {[m
 //        log.debug("...........................debug.............................")[m
 //        log.trace("...........................trace.............................")[m
 [m
[31m-        micro = Microservice.findByName(microName)[m
[32m+[m[32m            micro = Microservice.findByName(microName)[m
 [m
[31m-        if(microName != "umm"){[m
[31m-            log.info("If authorized, request will be forwarded to: ${micro?.ipAddress}${req}")[m
[31m-        }[m
[31m-[m
[31m-        if(!(springSecurityService?.principal?.username|| userName)){[m
[31m-            log.info("Access denied. Token not provided in the header.")[m
[31m-            resultSet.put("message", "Access denied. Token not provided in the header.")[m
[31m-            response.status = 403[m
[31m-            render resultSet as JSON[m
[31m-            return false[m
[31m-        }[m
[31m-[m
[31m-        try{[m
[31m-            user = User.findByUsername(userName)[m
[31m-        }catch (Exception ex){[m
[31m-            log.error("Exception occured while retrieving username in the securityInterceptor.", ex)[m
[31m-        }[m
[31m-[m
[31m-        if(!micro){[m
[31m-            log.info("Microservice: '${microName}' does not exist. Contact system admin.")[m
[31m-            resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")[m
[31m-            response.status = 403[m
[31m-            render resultSet as JSON[m
[31m-            return false[m
[31m-        }[m
[32m+[m[32m            if(microName != "umm"){[m
[32m+[m[32m                log.info("If authorized, request will be forwarded to: ${micro?.ipAddress}${req}")[m
[32m+[m[32m            }[m
 [m
[31m-        if(!UMR.findByUsersAndMicroservices(user, micro)){[m
[31m-            log.info("Access forbidden. User not authorized to request this resource.")[m
[31m-            resultSet.put("message", "Access forbidden. User not authorized to request this resource.")[m
[31m-            response.status = 403[m
[31m-            render resultSet as JSON[m
[31m-            return false[m
[31m-        }[m
[32m+[m[32m            if(!(springSecurityService?.principal?.username|| userName)){[m
[32m+[m[32m                log.info("Access denied. Token not provided in the header.")[m
[32m+[m[32m                resultSet.put("message", "Access denied. Token not provided in the header.")[m
[32m+[m[32m                response.status = 403[m
[32m+[m[32m                render resultSet as JSON[m
[32m+[m[32m                return false[m
[32m+[m[32m            }[m
 [m
[31m-        def permSuper = Permission.findByExpression("*:*")[m
[31m-        def permFull = Permission.findByExpression("${controller}:*")[m
[31m-        def permAction = Permission.findByExpression("${controller}:${action}")[m
[32m+[m[32m            try{[m
[32m+[m[32m                user = User.findByUsername(userName)[m
[32m+[m[32m            }catch (Exception ex){[m
[32m+[m[32m                log.error("Exception occured while retrieving username in the securityInterceptor.", ex)[m
[32m+[m[32m            }[m
 [m
[31m-        if(permSuper && authorizationService.hasPermission(user, micro, permSuper)){[m
[31m-            if(microName != "umm"){[m
[31m-                log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[31m-                redirect(url: "${micro?.ipAddress}${req}")[m
[32m+[m[32m            if(!micro){[m
[32m+[m[32m                log.info("Microservice: '${microName}' does not exist. Contact system admin.")[m
[32m+[m[32m                resultSet.put("message", "Microservice: '${microName}' does not exist. Contact system admin.")[m
[32m+[m[32m                response.status = 403[m
[32m+[m[32m                render resultSet as JSON[m
                 return false[m
             }[m
[31m-            log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[31m-            return true[m
[31m-        }[m
 [m
[31m-        if(permFull && authorizationService.hasPermission(user, micro, permFull)){[m
[31m-            if(microName != "umm"){[m
[31m-                log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[31m-                redirect(url: "${micro?.ipAddress}${req}")[m
[32m+[m[32m            if(!UMR.findByUsersAndMicroservices(user, micro)){[m
[32m+[m[32m                log.info("Access forbidden. User not authorized to request this resource.")[m
[32m+[m[32m                resultSet.put("message", "Access forbidden. User not authorized to request this resource.")[m
[32m+[m[32m                response.status = 403[m
[32m+[m[32m                render resultSet as JSON[m
                 return false[m
             }[m
[31m-            log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[31m-            return true[m
[31m-        }[m
 [m
[31m-        if(permAction && authorizationService.hasPermission(user, micro, permAction)) {[m
[31m-            if(microName != "umm"){[m
[31m-                log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[31m-                redirect(url: "${micro?.ipAddress}${req}")[m
[31m-                return false[m
[32m+[m[32m            def permSuper = Permission.findByExpression("*:*")[m
[32m+[m[32m            def permFull = Permission.findByExpression("${controller}:*")[m
[32m+[m[32m            def permAction = Permission.findByExpression("${controller}:${action}")[m
[32m+[m
[32m+[m[32m            if(permSuper && authorizationService.hasPermission(user, micro, permSuper)){[m
[32m+[m[32m                if(microName != "umm"){[m
[32m+[m[32m                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[32m+[m[32m                    redirect(url: "${micro?.ipAddress}${req}")[m
[32m+[m[32m                    return false[m
[32m+[m[32m                }[m
[32m+[m[32m                log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[32m+[m[32m                return true[m
[32m+[m[32m            }[m
[32m+[m
[32m+[m[32m            if(permFull && authorizationService.hasPermission(user, micro, permFull)){[m
[32m+[m[32m                if(microName != "umm"){[m
[32m+[m[32m                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[32m+[m[32m                    redirect(url: "${micro?.ipAddress}${req}")[m
[32m+[m[32m                    return false[m
[32m+[m[32m                }[m
[32m+[m[32m                log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[32m+[m[32m                return true[m
[32m+[m[32m            }[m
[32m+[m
[32m+[m[32m            if(permAction && authorizationService.hasPermission(user, micro, permAction)) {[m
[32m+[m[32m                if(microName != "umm"){[m
[32m+[m[32m                    log.info("Successfully Authorized. Forwarding request to: ${micro?.ipAddress}${req}")[m
[32m+[m[32m                    redirect(url: "${micro?.ipAddress}${req}")[m
[32m+[m[32m                    return false[m
[32m+[m[32m                }[m
[32m+[m[32m                log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[32m+[m[32m                return true[m
             }[m
[31m-            log.info("Successfully Authorized. Forwarding request to: ${req}")[m
[31m-            return true[m
[32m+[m
[32m+[m[32m            log.info("Access forbidden. User is not authorized to request this resource.")[m
[32m+[m[32m            response.status = 403[m
[32m+[m[32m            render resultSet as JSON[m
[32m+[m[32m            return false[m
[32m+[m[32m        }catch(Exception ex){[m
[32m+[m[32m            log.error("Exception occurred in the interceptor.")[m
[32m+[m[32m            log.error("Following is the stack trace along with the error message: ", ex)[m
[32m+[m[32m            return false[m
         }[m
 [m
[31m-        log.info("Access forbidden. User is not authorized to request this resource.")[m
[31m-        response.status = 403[m
[31m-        render resultSet as JSON[m
[31m-        false[m
 [m
     }[m
 [m
