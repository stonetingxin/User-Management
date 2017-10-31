(function ()
{
    'use strict';

    angular
        .module('app.users')
        .controller('ContactDialogController', ContactDialogController);

    /** @ngInject */
    function ContactDialogController( $rootScope, utilCustom, userService, $mdDialog, $filter, Roles, Contact, Contacts, User, msUtils, $document,$scope)
    {
        var vm = this;
        var message;
        // Data
        vm.title = $filter('translate')('CONTACTS.editUser');
        vm.contact = angular.copy(Contact);
        vm.contacts = Contacts;
        vm.roles = angular.copy(Roles);
        //vm.user = User;
        vm.newContact = false;
        vm.allFields = false;
        vm.showPassword= "password";
        vm.translationData = {
          roles: $filter('translate')('CONTACTS.roles'),
          secondarySupervisor: $filter('translate')('team.secondarySupervisor'),
          csq: $filter('translate')('csq.csqs'),
          agent: $filter('translate')('agent.agents'),
          applications:$filter('translate')('application.applications'),
          services:$filter('translate')('service.services')
        };

        if ( !vm.contact )
        {
            vm.contact = {
                'name'    : '',
                'lastName': '',
                'avatar'  : '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime(),
                'nickname': '',
                'company' : '',
                'jobTitle': '',
                'email'   : '',
                'phone'   : '',
                'address' : '',
                'birthday': null,
                'notes'   : '',
                'isActive' : false
            };

            vm.title = $filter('translate')('CONTACTS.newUser');
            vm.newContact = true;
            vm.contact.tags = [];
        }else
          vm.user = {file:vm.contact.avatar};

        // Methods
        vm.addNewContact = addNewContact;
        vm.saveContact = saveContact;
        vm.deleteContactConfirm = deleteContactConfirm;
        vm.closeDialog = closeDialog;
        vm.toggleInArray = msUtils.toggleInArray;
        vm.exists = exists;
        vm.queryArraySearch = queryArraySearch;
        vm.dialogShowDetail = dialogShowDetail;
        vm.roleAssignment = roleAssignment;
        vm.revokeRole = revokeRole;
        vm.togglePassword = togglePassword;
        vm.toggleInArrayRole = toggleInArrayRole;
        vm.filterRoles = filterRoles;
        vm.removable= removable;
        vm.usernameExists = usernameExists;
        vm.uniqueUsername = true;

        // $scope.$watch('vm.roles', function()
        // {
        //   vm.contactForm.$setDirty();
        // }, true);

        function usernameExists(data){
          var ind = _.findIndex(vm.contacts, function(o){
            return angular.lowercase(o.username)=== angular.lowercase(data);
          });
          if(ind!==-1)
            vm.uniqueUsername = false;
          else
            vm.uniqueUsername = true;
        }

        function removable(username){
            if(username === 'admin')
                return false;
            else
                return true;
        }

        function togglePassword(){
          if(vm.showPassword === "password")
            vm.showPassword = "text";
          else
            vm.showPassword = "password"
        }

        function roleAssignment(contact, chip){
          //var micros = extractMicros(chip);
          //vm.rolesAssigned.push(micros);
          var micros = extractMicros(chip);
          var params = {
            id: contact.id,
            microservices: micros,
            addRevoke:"add"
          };
          utilCustom.toasterLoading();
          userService.assignRoles(params).then(function(response){
            //console.log(response);
            //utilCustom.hideToaster();
            vm.contact = response.user;
            message = "roleAssigned";
            utilCustom.toaster($filter('translate')('CONTACTS.roleAssignmentSuccess'));
            vm.contactForm.$setDirty();
          },function(error){
            console.log(error);
          });
          return chip;
        }

        function revokeRole(contact, chip){
          var micros = extractMicros(chip);
          var params = {
            id: contact.id,
            microservices: micros,
            addRevoke:"revoke"
          };
          utilCustom.toasterLoading();
          userService.assignRoles(params).then(function(response){
            vm.contact = response.user;
            message = "roleAssigned";
            utilCustom.toaster($filter('translate')('CONTACTS.roleRevokeSuccess'));
            vm.contactForm.$setDirty();
          },function(error){
            console.log(error);
          });
          return chip;
        }

        function extractMicros(chip){
          var result = []
          for(var i=0; i<chip.permissions.length; i++){
            result.push({id: chip.permissions[i].microservice, roles: [{id: chip.id}]});
          }
          result = _.uniqBy(result, 'id');
          return result;
        }

        function queryArraySearch(query,objectList) {
          var results = query? objectList.filter(createFilterFor(query)): [];

          for (var i = 0; i < vm.contact.roles.length; i++) {

            for (var i2 = 0; i2 < results.length; i2++) {

              if (results[i2].name.toLowerCase() === vm.contact.roles[i].name.toLowerCase()) {

                results.splice(i2, 1);

              }

            }

          }
          return results;

        }

        function createFilterFor(query)
        {
          var lowercaseQuery = angular.lowercase(query);
          return function filterFn(item)
          {
            return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
          };
        }
        //////////

        /**
         * Add new contact
         */
        function addNewContact(user, file)
        {
          var userCreated = $rootScope._user;
          var params = {
            id:user.id,
            username: user.username,
            fullName: user.fullName,
            email: user.email,
            isActive: user.isActive,
            password: user.password,
            profileExists: file?true: false,
            createdBy:{id:userCreated.id}};
          // Dummy save action
          userService.create(params).then(function(response){

            vm.contact = response.user;

            if(file && file.name){
              var params2 = {file:file,agentId:angular.lowercase(user.username)};
              userService.updateProfilePic(params2).then(function(response){
                vm.contact.profileExists = true;
                $mdDialog.hide({user: vm.contact, message: 'create'});
              },function(error){
                console.log(error);
              });
            }else
              $mdDialog.hide({user: vm.contact, message: 'create'});

          }, function(error){
            console.log(error);
          });

        }

        /**
         * Save contact
         */
        function saveContact(user, file)
        {
            var userUpdated = $rootScope._user;
            var params = {
              id:user.id,
              fullName: user.fullName,
              email: user.email,
              isActive: user.isActive,
              profileExists: file?true: false,
              updatedby:{id:userUpdated.id}};
            // Dummy save action
            userService.update(params).then(function(response){
              vm.contact = response;
              if(file && file.name){
                var params2 = {file:file,agentId:angular.lowercase(Contact.username)};
                userService.updateProfilePic(params2).then(function(response){
                  vm.contact.profileExists = true;
                  $mdDialog.hide({user: vm.contact, message: 'update'});
                },function(error){
                  console.log(error);
                });
              }else
                $mdDialog.hide({user: vm.contact, message: 'update'});
            }, function(error){
              console.log(error);
            });


        }

        function dialogShowDetail(e, contact) {
          $mdDialog.show({
            controller: 'UserUploadPicController',
            controllerAs: 'vm',
            templateUrl: 'app/adminPanel/user/upload.html',
            parent: angular.element($document.body),
            targetEvent: e,
            clickOutsideToClose: true,
            skipHide: true,
            locals: {
              Contact : contact,
              User    : vm.user,
              Contacts: vm.contacts
            }
          }).then(function(userData) {
            if (userData) {
              //var ind = _.findIndex(vm.contacts, { username: userData.userID });
              //var indAge = _.findIndex(vm.agentList, { userID: userData.userID });
              //if (ind != -1) {
                if (userData.message === 'upload') {
                  vm.contact.avatar = window.appBaseUrl + 'base/assets1/images/agents/' + angular.lowercase(userData.username) + '.jpg?timestamp=' + new Date().getTime();
                } else if (userData.message === 'delete') {
                  vm.contact.avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();
                }
              //}
            }
            $mdDialog.hide( {contact: vm.contact, message: userData.message});
          })
        }
        /**
         * Delete Contact Confirm Dialog
         */
        function deleteContactConfirm(ev)
        {
            var success = null;
            var confirm = $mdDialog.confirm()
                .title($filter('translate')('CONTACTS.ContactDeleteConfirm'))
                .ariaLabel('delete contact')
                .targetEvent(ev)
                .ok($filter('translate')('generic.ok'))
                .cancel($filter('translate')('generic.cancel'));

            $mdDialog.show(confirm).then(function ()
            {
              var params = {id:vm.contact.id};
              userService.delete(params).then(function(response){
                success = response;
                $mdDialog.hide({id: vm.contact.id, message: 'delete'});
              }, function(error){
                console.log(error);
              });
            });

        }

      function filterRoles(label) {
        if ( !vm.labelSearchRoleText || vm.labelSearchRoleText === '' )
        {
          return true;
        }

        return angular.lowercase(label.name).indexOf(angular.lowercase(vm.labelSearchRoleText)) >= 0;
      }

      function exists(item,list) {
        return _.find(list,{id:item.id});
      }

      function toggleInArrayRole(item, list){
        var ind = _.findIndex(list, { id: item.id });
        if(ind != -1){
          revokeRole(vm.contact,item);
        }else {
          roleAssignment(vm.contact,item);
        }
      }

        /**
         * Close dialog
         */
        function closeDialog()
        {
            $mdDialog.hide({user: vm.contact, message: message});
        }

    }
})();
