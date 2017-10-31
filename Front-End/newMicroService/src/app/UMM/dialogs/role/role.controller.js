(function () {
    'use strict';

    angular
        .module('app.users')
        .controller('RoleController', RoleController);

    /** @ngInject */
    function RoleController($rootScope, userService, utilCustom, contacts, Microservices, Permissions, $mdDialog, $filter, Role, Roles, msUtils) {
        var message;
        var vm = this;

        // Data
        vm.title = $filter('translate')('CONTACTS.editRole');
        vm.role = Role;
        vm.microservices = angular.copy(Microservices);
        vm.microservice = vm.microservices[1];
        vm.permissions = Permissions;
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
                'name': '',
                'Description': null,
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

        function roleExists(data){
          var ind = _.findIndex(vm.roles, function(o){
            return angular.lowercase(o.name)=== angular.lowercase(data);
          });
          if(ind!==-1)
            vm.uniqueRole = false;
          else
            vm.uniqueRole = true;
        }

        function removable(name) {
            if (name === 'admin')
                return false;
            else
                return true;
        }


        if (vm.role) {
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
                role: role.id,
                user: chip.id,
                microservices: micros,
                addRevoke: "add"
            };
            utilCustom.toasterLoading();
            userService.userAssignment(params).then(function (response) {
                var ind = _.findIndex(vm.contacts, {id: response.user.id});

                if (response.user.profileExists)
                    response.user.avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(response.user.username) + '.jpg?timestamp=' + new Date().getTime();
                else
                    response.user.avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();

                if (ind != -1)
                    vm.contacts[ind] = response.user;

                var ind2 = _.findIndex(vm.roleUser, {id: response.user.id});
                if(ind2 == -1)
                    vm.roleUser.unshift(response.user);

                message = "contactUpdate";
                utilCustom.toaster($filter('translate')('CONTACTS.roleAssignmentSuccess'));
                vm.roleForm.$setDirty();
            }, function (error) {
                console.log(error);
            });
        }

        function revokeUser(role, chip) {
            var micros = extractRoleMicros(role);
            var params = {
                role: role.id,
                user: chip.id,
                microservices: micros,
                addRevoke: "revoke"
            };
            utilCustom.toasterLoading();
            userService.userAssignment(params).then(function (response) {
                var ind = _.findIndex(vm.contacts, {id: response.user.id});

                if (response.user.profileExists)
                    response.user.avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(response.user.username) + '.jpg?timestamp=' + new Date().getTime();
                else
                    response.user.avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();

                if (ind != -1)
                    vm.contacts[ind] = response.user;

                var ind2 = _.findIndex(vm.roleUser, {id: response.user.id});
                if(ind2 != -1)
                    vm.roleUser.splice(ind2, 1);
                message = "contactUpdate";
                utilCustom.toaster($filter('translate')('CONTACTS.roleRevokeSuccess'));
                vm.roleForm.$setDirty();
            }, function (error) {
                console.log(error);
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

        function addPermission(role, chip) {
            //var micros = extractMicros(chip);
            //vm.rolesAssigned.push(micros);
            //var perms = extractPerms(chip);
            var params = {
                id: role.id,
                permissions: [{id: chip.id}],
                addRevoke: "add"
            };
            utilCustom.toasterLoading();
            userService.addRemPermissions(params).then(function (response) {
                vm.role = response.role;
                message = "update";
                utilCustom.toaster($filter('translate')('CONTACTS.roleAssignmentSuccess'));
                vm.roleForm.$setDirty();
            }, function (error) {
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
            utilCustom.toasterLoading();
            userService.addRemPermissions(params).then(function (response) {
                vm.role = response.role;
                message = "update";
                utilCustom.toaster($filter('translate')('CONTACTS.roleRevokeSuccess'));
                vm.roleForm.$setDirty();
            }, function (error) {
                console.log(error);
            });
            return chip;
        }

        function queryArraySearch(query, objectList) {
            var results = query ? objectList.filter(createFilterFor(query)) : [];

            for (var i = 0; i < vm.role.permissions.length; i++) {

                for (var i2 = 0; i2 < results.length; i2++) {

                    if (results[i2].expression.toLowerCase() === vm.role.permissions[i].expression.toLowerCase()) {

                        results.splice(i2, 1);

                    }

                }

            }
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
            var ind = _.findIndex(objectList, function(o){
              return o.username === "admin";
            });
            if(ind !==-1)
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
                return angular.lowercase(item.expression).indexOf(lowercaseQuery) >= 0;
            };
        }

        function scrollToBottom(id) {
            var objDiv = document.getElementById(id);
            objDiv.lastElementChild.lastElementChild.lastElementChild.focus()
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
                message = "create";
                utilCustom.toaster($filter('translate')('CONTACTS.successCreatedRole'));
                //$mdDialog.hide({role: vm.role, message: 'create'});
                closeDialog();
            }, function (error) {
                console.log(error);
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
                message = "update";
                utilCustom.toaster($filter('translate')('CONTACTS.successUpdatedRole'));
                closeDialog();
                //$mdDialog.hide({role: vm.role, message: 'update'});
            }, function (error) {
                console.log(error);
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

        function toggleInArrayPerm(item, list) {
            var ind = _.findIndex(list, {id: item.id});
            if (ind != -1) {
                removePermission(vm.role, item);
            } else {
                addPermission(vm.role, item);
            }
        }

        function toggleInArrayUser(item, list) {
            var ind = _.findIndex(vm.roleUser, {id: item.id});
            if (ind != -1) {
                revokeUser(list, item);
            } else {
                userAssignment(list, item);
            }
        }

        /**
         * Close dialog
         */
        function closeDialog() {
            $mdDialog.hide({role: vm.role, contacts: vm.contacts, message: message});
        }
    }
})();
