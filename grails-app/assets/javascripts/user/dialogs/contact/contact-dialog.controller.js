(function ()
{
    'use strict';

    angular
        .module('app.users')
        .controller('ContactDialogController', ContactDialogController);

    /** @ngInject */
    function ContactDialogController( $rootScope, userService, agentService, $mdDialog, $filter, Contact, Contacts, User, msUtils, $document)
    {
        var vm = this;

        // Data
        vm.title = 'Edit Contact';
        vm.contact = angular.copy(Contact);
        vm.contacts = Contacts;
        //vm.user = User;
        vm.newContact = false;
        vm.allFields = false;


        if ( !vm.contact )
        {
            vm.contact = {
                'name'    : '',
                'lastName': '',
                'avatar'  : '/efadminpanel/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime(),
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

            vm.title = 'New Contact';
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
        vm.exists = msUtils.exists;

        vm.dialogShowDetail = dialogShowDetail;

        function  saveProfilePic(userPic){

            var params = {file:userPic.file,agentId:angular.lowercase(Contact.username)};
            utilCustom.toasterLoading();
            agentService.updateProfilePic(params).then(function(response){
              utilCustom.toaster($filter('translate')('agent.picUploaded'));
              vm.contact.profileExists = true;
              //$mdDialog.hide({userID:Contact.username,message:'upload'});
            },function(error){
              utilCustom.toaster($filter('translate')('agent.picUploadError'));
               console.log(error);
            });
        }

        function removeProfilePic(){
          utilCustom.toasterLoading();
          agentService.removeProfilePic({agentId:vm.userID}).then(function(response){
            utilCustom.toaster($filter('translate')('agent.picDeleted'));
            vm.agentData.profileExists = false;
            $mdDialog.hide({userID:userID,message:'delete'});
          },function(error){
            utilCustom.toaster($filter('translate')('agent.picDeleteError'));
          })
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
              agentService.updateProfilePic(params2).then(function(response){
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
                agentService.updateProfilePic(params2).then(function(response){
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
                  vm.contact.avatar = window.appBaseUrl3 + '/images/agents/' + angular.lowercase(userData.userID) + '.jpg?timestamp=' + new Date().getTime();
                } else if (userData.message === 'delete') {
                  vm.contact.avatar = '/efadminpanel/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();
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

        /**
         * Close dialog
         */
        function closeDialog()
        {
            $mdDialog.hide();
        }

    }
})();
