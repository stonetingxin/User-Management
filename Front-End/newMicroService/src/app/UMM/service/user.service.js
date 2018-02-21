/**
 * Created by mumar on 3/9/2016.
 */
(function () {
  'use strict';
  angular
    .module('app.users')
    .factory('userService', ['$resource', '$rootScope', '$q', 'Upload',userService]);

  function userService($resource, $rootScope, $q, Upload) {
    var users = $resource('../user/', {id: '@id'}, {
      update: {
        method: 'PUT',
        url: window.appBaseUrl + '/user/update/',
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
        url: window.appBaseUrl + '/user/list/'
        //isArray:true
      },
      getMicroservices: {
        method: 'GET',
        url: window.appBaseUrl + '/microservice/list/'
        //isArray:true
      },
      show: {
        method: 'GET',
        url: window.appBaseUrl + '/user/show/',
        params: {
          id: '@id'
        }
      },

      delete: {
        method: 'DELETE',
        url: window.appBaseUrl + '/user/delete/',
        params: {
          id: '@id'
        }

      },
      deleteRole: {
        method: 'DELETE',
        url: window.appBaseUrl + '/role/delete/',
        params: {
          id: '@id'
        }

      },
      deleteMulti: {
        method: 'DELETE',
        url: window.appBaseUrl + '/user/deleteMulti/',
        isArray: true,
        params: {
          ids: '@ids'
        }

      },
      // assignRoles: {
      //   method: 'PUT',
      //   url: window.appBaseUrl + '/user/addRevokeMicroserviceRoles/',
      //   //isArray: true,
      //   params: {
      //     assignRoles: '@assignRoles'
      //   }
      //
      // },
      assignRoles: {
        method: 'PUT',
        url: window.appBaseUrl + '/user/assignRoles/',
        //isArray: true,
        params: {
          assignRoles: '@assignRoles'
        }

      },
      revokeRoles: {
        method: 'PUT',
        url: window.appBaseUrl + '/user/revokeRoles/',
        //isArray: true,
        params: {
          revokeRoles: '@revokeRoles'
        }

      },
      addRemPermissions: {
        method: 'PUT',
        url: window.appBaseUrl + '/role/addRevokePermissions/',
        //isArray: true,
        params: {
          assignPerms: '@assignPerms'
        }

      },
      userAssignment: {
        method: 'PUT',
        url: window.appBaseUrl + '/role/userAssign/',
        //isArray: true,
        params: {
          assignUsers: '@assignUsers'
        }

      },
      roleUser: {
        method: 'GET',
        url: window.appBaseUrl + '/role/roleUser/',
        //isArray: true,
        params: {
          id: '@id'
        }

      },
      create: {
        method: 'POST',
        url: window.appBaseUrl + '/user/create',
        params: {
          fullName: '@fullName',
          email: '@email',
          isActive: '@isActive',
          password: '@password',
          createdBy: '@createdBy'
        }
      },
      createRole: {
        method: 'POST',
        url: window.appBaseUrl + '/role/create',
        params: {
          authority: '@authority',
          description: '@description',
          createdBy: '@createdBy'
        }
      },
      updateRole: {
        method: 'PUT',
        url: window.appBaseUrl + '/role/update',
        params: {
          id: '@id',
          authority: '@authority',
          description: '@description',
          createdBy: '@createdBy'
        }
      },

      activeAndBlockUser: {
        method: 'POST',
        url: window.appBaseUrl + '/user/activateOrBlockUser/',
        params: {
          id: '@id',
          checked: '@checked'
        }
      },

      changePassword: {
        method: 'POST',
        url: window.appBaseUrl + '/user/changePassword/',
        params: {
          id: '@id',
          currentPassword: '@oldPassword',
          newPassword: '@newPassword',
          confirmPassword: '@confirmPassword'
        }
      },
      resetPassword: {
        method: 'POST',
        url: window.appBaseUrl + '/user/resetPassword/',
        params: {
          username: '@username',
          currentpassword: '@currentpassword'
        }
      },
      getRoles: {
        method: 'GET',
        url: window.appBaseUrl + '/role/list/'
      },
      addRevokeMicroserviceRoles: {
        method: 'PUT',
        url: window.appBaseUrl + '/user/resetPassword/',
        params: {
          username: '@username',
          roles: '@roles',
          addRevoke: '@addRevoke'
        }
      },
      removeProfilePic:{
        "method":'POST',
        url:window.appBaseUrl+'/user/deleteProfilePic',params:{
          agentId:'@agentId'
        }
      },
      getProfilePic:{
        method:'POST',
        url:window.appBaseUrl+'/user/getProfilePic',params:{
          id:'@id'
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
      'update': function (user) {
        var defered = $q.defer();
        users.update(user, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'list': function (params) {
        var defered = $q.defer();
        users.list(params, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'getMicroservices': function (params) {
        var defered = $q.defer();
        users.getMicroservices(params, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      get_api: users,
      'activeAndBlockUser': function (id) {
        var defered = $q.defer();
        users.activeAndBlockUser(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'show': function (id) {
        var defered = $q.defer();
        users.show(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'create': function (user) {
        var defered = $q.defer();
        users.create(user, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'createRole': function (role) {
        var defered = $q.defer();
        users.createRole(role, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'updateRole': function (role) {
        var defered = $q.defer();
        users.updateRole(role, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'changePassword': function (userData) {
        var defered = $q.defer();
        users.changePassword(userData, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'resetPassword': function (username) {
        var defered = $q.defer();
        users.resetPassword(username, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'addRevokeMicroserviceRoles': function (username, roles) {
        var defered = $q.defer();
        users.addRevokeMicroserviceRoles(username, roles, addRevoke,
          function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'delete': function (id) {
        var defered = $q.defer();
        users.delete(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'deleteRole': function (id) {
        var defered = $q.defer();
        users.deleteRole(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'deleteMulti': function (ids) {
        var defered = $q.defer();
        users.deleteMulti(ids, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      // 'assignRoles': function (ids) {
      //   var defered = $q.defer();
      //   users.assignRoles(ids, function (response) {
      //       defered.resolve(response);
      //     },
      //     function (error) {
      //       defered.reject(error);
      //     });
      //   return defered.promise;
      // },
      'assignRoles': function (ids) {
        var defered = $q.defer();
        users.assignRoles(ids, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },

      'revokeRoles': function (ids) {
        var defered = $q.defer();
        users.revokeRoles(ids, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'roleUser': function (id) {
        var defered = $q.defer();
        users.roleUser(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'addRemPermissions': function (ids) {
        var defered = $q.defer();
        users.addRemPermissions(ids, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'userAssignment': function (ids) {
        var defered = $q.defer();
        users.userAssignment(ids, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'getRoles': function (id) {
        var defered = $q.defer();
        users.getRoles(id, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      },
      'getProfilePic':function(params){
        var defered = $q.defer();
        users.getProfilePic(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'removeProfilePic':function(id){
        var defered = $q.defer();
        users.removeProfilePic(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateProfilePic':function(params){
        var up =  Upload.upload({
          url:window.appBaseUrl+"/user/updateProfilePic",
          data: params
        });

        return up;
      },
      'isUserAuthentic': function (params) {
        var defered = $q.defer();
        users.isUserAuthentic(params, function (response) {
            defered.resolve(response);
          },
          function (error) {
            defered.reject(error);
          });
        return defered.promise;
      }
    }
  }

})();
