(function () {
  'use strict';

  angular
    .module('app.users')
    .controller('RoleController', RoleController);

  /** @ngInject */
  function RoleController($rootScope, userService, utilCustom, contacts, Microservices, Permissions, $mdDialog, $filter, Role, Roles, $document) {
    var message = [];
    var vm = this;

    // Data
    vm.title = $filter('translate')('CONTACTS.editRole');
    vm.role = angular.copy(Role);
    vm.microservices = angular.copy(Microservices);
    vm.microservice = vm.microservices[1];
    vm.permissions = angular.copy(Permissions);
    vm.contacts = contacts;
    vm.roleUser = [];
    vm.roles = angular.copy(Roles);

    //vm.user = User;
    vm.newRole = false;
    vm.allFields = false;
    vm.translationData = {
      roles: $filter('translate')('CONTACTS.roles'),
      permissions: $filter('translate')('CONTACTS.permissions'),
      users: $filter('translate')('user.users'),
      secondarySupervisor: $filter('translate')('team.secondarySupervisor'),
      csq: $filter('translate')('csq.csqs'),
      agent: $filter('translate')('agent.agents'),
      applications: $filter('translate')('application.applications'),
      services: $filter('translate')('service.services')
    };

    if (!vm.role) {
      vm.role = {
        'name': null,
        'Description': null
      };

      vm.title = $filter('translate')('CONTACTS.newRole');
      vm.newRole = true;
    }

    // Methods
    vm.toggleInArrayPerm = toggleInArrayPerm;
    vm.toggleInArrayUser = toggleInArrayUser;
    vm.addNewRole = addNewRole;
    vm.saveRole = saveRole;
    vm.closeDialog = closeDialog;
    vm.exists = exists;
    vm.queryArraySearch = queryArraySearch;
    vm.queryUserSearch = queryUserSearch;
    vm.addPermission = addPermission;
    vm.removePermission = removePermission;
    vm.scrollToBottom = scrollToBottom;
    vm.userAssignment = userAssignment;
    vm.revokeUser = revokeUser;
    vm.filterUsers = filterUsers;
    vm.filterPermissions = filterPermissions;
    vm.removable = removable;
    vm.roleExists = roleExists;
    vm.uniqueRole = true;

    function roleExists(data) {
      var ind = _.findIndex(vm.roles, function (o) {
        return angular.lowercase(o.name) === angular.lowercase(data);
      });
      if (ind !== -1)
        vm.uniqueRole = false;
      else
        vm.uniqueRole = true;
    }

    function removable(name) {
      if (name === 'Administrator')
        return false;
      else
        return true;
    }


    if (vm.role.name !== null) {
      userService.roleUser({id: vm.role.id}).then(function (response) {
        //console.log(response);
        //utilCustom.hideToaster();
        mapUsers(response.users)
        //utilCustom.toaster($filter('translate')('CONTACTS.roleAssignmentSuccess'));
      }, function (error) {
        console.log(error);
      });
    }

    function userAssignment(role, chip) {
      var micros = extractRoleMicros(role);
      var params = {
        id: chip.id,
        roles: [{id: role.id}]
      };

      userService.assignRoles(params).then(function (response) {
        var ind = _.findIndex(vm.contacts, {id: response.user.id});

        if (response.user.profileExists)
          response.user.avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(response.user.username) + '.jpg?timestamp=' + new Date().getTime();
        else
          response.user.avatar = 'assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();

        if (ind !== -1)
          vm.contacts[ind] = response.user;

        var ind2 = _.findIndex(vm.roleUser, {id: response.user.id});
        if (ind2 === -1)
          vm.roleUser.unshift(response.user);

        message.push("userAss");
        vm.roleForm.$setDirty();
      }, function (error) {
        if (error.status !== 403)
          utilCustom.toaster($filter('translate')('CONTACTS.roleAssignmentFailure'));
        console.log(error);
        return null;
      });
    }

    function revokeUser(role, chip) {
      var micros = extractRoleMicros(role);
      var params = {
        id: chip.id,
        roles: [{id: role.id}]
      };

      userService.revokeRoles(params).then(function (response) {
        var ind = _.findIndex(vm.contacts, {id: response.user.id});

        if (response.user.profileExists)
          response.user.avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(response.user.username) + '.jpg?timestamp=' + new Date().getTime();
        else
          response.user.avatar = 'assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();

        if (ind !== -1)
          vm.contacts[ind] = response.user;

        var ind2 = _.findIndex(vm.roleUser, {id: response.user.id});
        if (ind2 !== -1)
          vm.roleUser.splice(ind2, 1);
        message.push("userRev");
        vm.roleForm.$setDirty();
      }, function (error) {
        if (error.status !== 403)
          utilCustom.toaster($filter('translate')('CONTACTS.roleRevokeFailure'));
        console.log(error);
        return null;
      });

    }

    function extractRoleMicros(role) {
      var micros = [];
      for (var i = 0; i < role.permissions.length; i++) {
        micros.push({id: role.permissions[i].microservice});
      }
      micros = _.uniqBy(micros, 'id');
      return micros;
    }

    function mapUsers(users) {
      for (var i = 0; i < users.length; i++) {
        for (var j = 0; j < vm.contacts.length; j++) {
          if (users[i].id === vm.contacts[j].id) {
            vm.roleUser.push(vm.contacts[j]);
          }
        }
      }
    }

    function getPermFromExp(exp) {
      return _.find(vm.permissions, function (p) {
        return p.expression === exp;
      });
    }

    function permNotAssigned(role, exp) {
      var ind, ind2;
      ind = _.findIndex(role.permissions, function (o) {
        return o.expression === exp;
      });
      if (ind === -1) {
        var full = exp.split(":")[0] + ":*";
        ind2 = _.findIndex(role.permissions, function (o) {
          return o.expression === full;
        });
      }
      return ind2 === -1;
    }

    function addSubReqs(role, chipPerm, requisites) {
      var subPreReqs = getPermFromExp(chipPerm).preReqs;
      if (subPreReqs && subPreReqs.length !== 0) {
        angular.forEach(subPreReqs, function (subPerm) {
          if (permNotAssigned(role, subPerm) && !_.includes(requisites, subPerm)) {
            requisites.push(subPerm);
            addSubReqs(role, subPerm, requisites);
          } else
            return;
        });
      } else
        return;
    }

    function addPermission(ev, role, chip) {
      var requisites = [];
      var perms = [];
      var perm;
      if (chip.preReqs.length !== 0) {
        angular.forEach(chip.preReqs, function (chipPerm) {
          if (permNotAssigned(role, chipPerm)) {
            requisites.push(chipPerm);
            addSubReqs(role, chipPerm, requisites);
          }
        });

        if (requisites.length !== 0) {
          perms = mapPerms(_.uniq(requisites));
        } else {
          perm = [{id: chip.id}];
          return addPermissionAux(role, chip, perm);
        }

        $mdDialog.show({
          controller: 'preReqController',
          controllerAs: 'vm',
          templateUrl: 'app/UMM/dialogs/role/preReq-dialog.html',
          parent: angular.element($document.find('#content-container')),
          targetEvent: ev,
          skipHide: true,
          clickOutsideToClose: false,
          multiple: true,
          locals: {
            permission: chip,
            permissions: vm.permissions,
            preReqs: perms,
            rolePerms: vm.role.permissions
          }
        })
          .then(function (response) {
            if (response.message === "add") {
              return addPermissionAux(role, chip, response.perms);
            } else {
              return null;
            }
          });

      } else {
        perm = [{id: chip.id}];
        return addPermissionAux(role, chip, perm);
      }

    }

    function mapPerms(permsToMap) {
      var perms = [];
      angular.forEach(permsToMap, function (preReq) {
        var lowerPre = angular.lowercase(preReq);
        var ind = _.findIndex(vm.permissions, function (o) {
          return angular.lowercase(o.expression) === lowerPre;
        });
        if (ind !== -1) {
          perms.push(vm.permissions[ind]);
        }
      });
      return perms;
    }

    function addPermissionAux(role, chip, perm) {
      var params = {
        id: role.id,
        permissions: perm,
        addRevoke: "add"
      };
      userService.addRemPermissions(params).then(function (response) {
        vm.role = response.role;
        message.push("permAdd");
        vm.roleForm.$setDirty();
      }, function (error) {
        if (error.status !== 403)
          utilCustom.toaster($filter('translate')('CONTACTS.permAddFailure'));
        console.log(error);
      });
      return chip;
    }

    function removePermission(role, chip) {
      //var perms = extractPerms(chip);
      var params = {
        id: role.id,
        permissions: [{id: chip.id}],
        addRevoke: "revoke"
      };

      userService.addRemPermissions(params).then(function (response) {
        vm.role = response.role;
        message.push("permRem");
        vm.roleForm.$setDirty();
      }, function (error) {
        if (error.status !== 403)
          utilCustom.toaster($filter('translate')('CONTACTS.permRemoveFailure'));
        console.log(error);
      });
      return chip;
    }

    function queryArraySearch(query, objectList) {
      var results = query ? objectList.filter(createFilterFor(query)) : [];

      for (var i = 0; i < vm.role.permissions.length; i++) {

        for (var i2 = 0; i2 < results.length; i2++) {

          if (results[i2].name.toLowerCase() === vm.role.permissions[i].name.toLowerCase()) {

            results.splice(i2, 1);

          }

        }

      }

      var ind = _.findIndex(results, function (o) {
        return o.expression === "default:*";
      });
      if (ind !== -1)
        results.splice(ind, 1);

      return results;

    }

    function queryUserSearch(query, objectList) {
      var results = query ? objectList.filter(createFilterForUser(query)) : [];

      for (var i = 0; i < vm.roleUser.length; i++) {

        for (var i2 = 0; i2 < results.length; i2++) {

          if (results[i2].id === vm.roleUser[i].id) {

            results.splice(i2, 1);

          }

        }

      }
      var ind = _.findIndex(results, function (o) {
        return o.username === "admin";
      });
      if (ind !== -1)
        results.splice(ind, 1);
      return results;

    }

    function createFilterForUser(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item) {
        return angular.lowercase(item.fullName).indexOf(lowercaseQuery) >= 0;
      };
    }

    function createFilterFor(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item) {
        return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
      };
    }

    function scrollToBottom(id) {
      var objDiv = document.getElementById(id);
      objDiv.lastElementChild.lastElementChild.lastElementChild.focus();
      objDiv.scrollTop = objDiv.scrollHeight;
    }

    //////////

    /**
     * Add new contact
     */
    function addNewRole(role) {
      var userCreated = $rootScope._user;
      var params = {
        authority: role.name,
        description: role.description,
        createdBy: {id: userCreated.id}
      };
      // Dummy save action
      utilCustom.toasterLoading();
      userService.createRole(params).then(function (response) {

        vm.role = response.role;
        message.push("create");
        utilCustom.toaster($filter('translate')('CONTACTS.successCreatedRole'));
        //$mdDialog.hide({role: vm.role, message: 'create'});
        closeDialog();
      }, function (error) {
        if (error.status !== 403)
          console.log(error);
        utilCustom.toaster($filter('translate')('CONTACTS.failedCreatedRole'));
      });

    }

    /**
     * Save contact
     */
    function saveRole(role) {
      var userUpdated = $rootScope._user;
      var params = {
        id: role.id,
        authority: role.name,
        description: role.description,
        createdBy: {id: userUpdated.id}
      };
      utilCustom.toasterLoading();
      userService.updateRole(params).then(function (response) {
        vm.role = response.role;
        message.push("update");
        utilCustom.toaster($filter('translate')('CONTACTS.successUpdatedRole'));
        closeDialog();
        //$mdDialog.hide({role: vm.role, message: 'update'});
      }, function (error) {
        console.log(error);
        if (error.status !== 403)
          utilCustom.toaster($filter('translate')('CONTACTS.failedUpdatedRole'));
      });


    }


    function filterPermissions(label) {
      if (!vm.labelSearchPermText || vm.labelSearchPermText === '') {
        return true;
      }

      return angular.lowercase(label.name).indexOf(angular.lowercase(vm.labelSearchPermText)) >= 0;
    }

    function filterUsers(label) {
      if (label.username === 'admin')
        return false;

      if (!vm.labelSearchUserText || vm.labelSearchUserText === '') {
        return true;
      }

      return angular.lowercase(label.fullName).indexOf(angular.lowercase(vm.labelSearchUserText)) >= 0;
    }

    function exists(item, list) {
      return _.find(list, {id: item.id});
    }

    function toggleInArrayPerm(ev, item, list) {
      var ind = _.findIndex(list, {id: item.id});
      if (ind !== -1) {
        removePermission(vm.role, item);
      } else {
        addPermission(ev, vm.role, item);
      }
    }

    function toggleInArrayUser(item, list) {
      var ind = _.findIndex(vm.roleUser, {id: item.id});
      if (ind !== -1) {
        revokeUser(list, item);
      } else {
        userAssignment(list, item);
      }
    }

    /**
     * Close dialog
     */
    function closeDialog() {
      $mdDialog.hide({role: vm.role, contacts: vm.contacts, message: message, roleUser: vm.roleUser});
    }
  }
})();
