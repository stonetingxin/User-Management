/**
 * Created by mumar on 3/9/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.users')
        .factory('userService', ['$resource', '$rootScope', '$q', userService]);

    function userService($resource, $rootScope, $q) {
        var users = $resource('../user/', { id: '@id' }, {
            update: {
                method: 'PUT',
                url: window.appBaseUrl2 + '/user/update/',
                params: {
                    id: '@id',
                    fullName: '@fullName',
                    email: '@email',
                    isActive: '@isActive',
                    updatedBy: '@updatedBy'
                }

            },

            list: {
                method: 'GET',
                url: window.appBaseUrl2 + '/user/list/'
                    //isArray:true
            },
            show: {
                method: 'GET',
                url: window.appBaseUrl2 + '/user/show/',
                params: {
                    id: '@id'
                }
            },

            delete: {
                method: 'DELETE',
                url: window.appBaseUrl2 + '/user/delete/',
                params: {
                  id: '@id'
                }

            },
            deleteMulti: {
              method: 'DELETE',
              url: window.appBaseUrl2 + '/user/deleteMulti/',
              params: {
                ids: '@ids'
              }

            },
            create: {
                method: 'POST',
                url: window.appBaseUrl2 + '/user/create',
                params: {
                    fullName: '@fullName',
                    email: '@email',
                    isActive: '@isActive',
                    password: '@password',
                    createdBy: '@createdBy'
                }
            },

            activeAndBlockUser: {
                method: 'POST',
                url: window.appBaseUrl2 + '/user/activateOrBlockUser/',
                params: {
                    id: '@id',
                    checked: '@checked'
                }
            },

            changePassword: {
                method: 'POST',
                url: window.appBaseUrl2 + '/user/changePassword/',
                params: {
                    id: '@id',
                    currentPassword: '@oldPassword',
                    newPassword: '@newPassword',
                    confirmPassword: '@confirmPassword'
                }
            },
            resetPassword: {
                method: 'POST',
                url: window.appBaseUrl2 + '/user/resetPassword/',
                params: {
                    username: '@username',
                    currentpassword: '@currentpassword'
                }
            },
            getRoles: {
                method: 'GET',
                url: window.appBaseUrl2 + '/role/list/'
            },
            addRevokeMicroserviceRoles : {
                method: 'PUT',
                url: window.appBaseUrl2 + '/user/resetPassword/',
                params: {
                    username: '@username',
                    roles: '@roles',
                    addRevoke : '@addRevoke'
                }
            },
            isUserAuthentic: {
                method: 'GET',
                url: window.appBaseUrl + '/user/isUserAuthentic/',
                params: {
                    username: '@username',
                    password: '@password'
                }
            }



        });
        return {
            'update': function(user) {
                var defered = $q.defer();
                users.update(user, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'list': function(params) {
                var defered = $q.defer();
                users.list(params, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            get_api: users,
            'activeAndBlockUser': function(id) {
                var defered = $q.defer();
                users.activeAndBlockUser(id, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'show': function(id) {
                var defered = $q.defer();
                users.show(id, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'create': function(user) {
              var defered = $q.defer();
              users.create(user, function(response) { defered.resolve(response); },
                function(error) { defered.reject(error); });
              return defered.promise;
            },
            'changePassword': function(userData) {
                var defered = $q.defer();
                users.changePassword(userData, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'resetPassword': function(username) {
                var defered = $q.defer();
                users.resetPassword(username, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'addRevokeMicroserviceRoles': function(username, roles) {
                var defered = $q.defer();
                users.addRevokeMicroserviceRoles(username, roles, addRevoke,
                    function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'delete': function(id) {
                var defered = $q.defer();
                users.delete(id, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'deleteMulti': function(ids) {
              var defered = $q.defer();
              users.deleteMulti(ids, function(response) { defered.resolve(response); },
                function(error) { defered.reject(error); });
              return defered.promise;
            },
            'getRoles': function(id) {
                var defered = $q.defer();
                users.getRoles(id, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            },
            'isUserAuthentic': function(params) {
                var defered = $q.defer();
                users.isUserAuthentic(params, function(response) { defered.resolve(response); },
                    function(error) { defered.reject(error); });
                return defered.promise;
            }
        }
    }

})();
