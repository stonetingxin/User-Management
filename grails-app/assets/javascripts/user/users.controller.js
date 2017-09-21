(function ()
{
    'use strict';

    angular
        .module('app.users')
        .controller('UsersController', UsersController);

    /** @ngInject */
    function UsersController(userService, utilCustom, $scope, $filter, $mdSidenav, msUtils, $mdDialog, $document)
    {

        var vm = this;

        // Data
		vm.widget11 = {
            title    : "Roles",
            table    : {
            "columns": [
                {
                    "title": "Name"
                },
                {
                    "title": "Permissions"
                },
                {
                    "title": "Description"
                }
            ],
            "rows": []
        },
            dtOptions: {
              dom       : '<"top"f>rt<"bottom"<"left"<"length"l>><"right"<"info"i><"pagination"p>>>',
              pagingType: 'simple',
              autoWidth : false,
              responsive: true
            }
        };
        vm.roles = null;
        vm.contacts = null;
        vm.user =  {
        "id": "5725a6802d10e277a0f35724",
        "name": "John Doe",
        "avatar": "assets/images/avatars/profile.jpg",
        "starred": [
            "5725a680ae1ae9a3c960d487",
            "5725a6801146cce777df2a08",
            "5725a680bbcec3cc32a8488a",
            "5725a680bc670af746c435e2",
            "5725a68009e20d0a9e9acf2a"
        ],
        "frequentContacts": [
            "5725a6809fdd915739187ed5",
            "5725a68031fdbb1db2c1af47",
            "5725a680606588342058356d",
            "5725a680e7eb988a58ddf303",
            "5725a6806acf030f9341e925",
            "5725a68034cb3968e1f79eac",
            "5725a6801146cce777df2a08",
            "5725a680653c265f5c79b5a9"
        ],
        "groups": [
            {
                "id": "5725a6802d10e277a0f35739",
                "name": "Friends",
                "contactIds": [
                    "5725a680bbcec3cc32a8488a",
                    "5725a680e87cb319bd9bd673",
                    "5725a6802d10e277a0f35775"
                ]
            },
            {
                "id": "5725a6802d10e277a0f35749",
                "name": "Clients",
                "contactIds": [
                    "5725a680cd7efa56a45aea5d",
                    "5725a68018c663044be49cbf",
                    "5725a6809413bf8a0a5272b1",
                    "5725a6803d87f1b77e17b62b"
                ]
            },
            {
                "id": "5725a6802d10e277a0f35329",
                "name": "Recent Workers",
                "contactIds": [
                    "5725a680bbcec3cc32a8488a",
                    "5725a680653c265f5c79b5a9",
                    "5725a6808a178bfd034d6ecf",
                    "5725a6801146cce777df2a08"
                ]
            }
        ]
    };
        vm.filterIds = null;
        vm.listType = 'all';
        vm.listOrder = 'name';
        vm.listOrderAsc = false;
        vm.selectedContacts = [];
        vm.newGroupName = '';
        vm.roleNames = [];

        // Methods
        vm.filterChange = filterChange;
        vm.openContactDialog = openContactDialog;
        vm.deleteContactConfirm = deleteContactConfirm;
        vm.deleteContact = deleteContact;
        vm.deleteSelectedContacts = deleteSelectedContacts;
        vm.toggleSelectContact = toggleSelectContact;
        vm.deselectContacts = deselectContacts;
        vm.selectAllContacts = selectAllContacts;
        vm.deleteContact = deleteContact;
        vm.addNewGroup = addNewGroup;
        vm.deleteGroup = deleteGroup;
        vm.toggleSidenav = toggleSidenav;
        vm.toggleInArray = msUtils.toggleInArray;
        vm.exists = msUtils.exists;
		    vm.showAdvanced = showAdvanced;

        vm.init = init;

        init();

        function init(){
          userService.list().then(
            function(data) {
              vm.contacts = data.users;
              parseUserList();
              //extractUserRoles();
              populateRoles();
            },function(error) {
              console.log(error)
            }
          );
        }

        function populateRoles(){
          userService.getRoles().then(function(data){
            vm.roles = data.roles;
            extractRoles();
          }, function(error){
            console.log(error);
          });
        }

        function extractRoles(){
          angular.forEach(vm.roles, function(value, key){
            var tempArr=[];
            tempArr.push(value.name);
            tempArr.push(value.permissions);
            tempArr.push(value.description);
            vm.widget11.table.rows.push(tempArr);
          });
          vm.widget11.table.rows
        }
        function extractPermissions(){

        }

        function parseUserList() {
          angular.forEach(vm.contacts, function(value, key) {
            if (key === vm.contacts.length - 1) {
              utilCustom.hideToaster();
            }

            if (value.team) {
              var team = value.team.refURL.split('/');
              vm.contacts[key].teamId = Number(team[team.length - 1]);
            }

            if (value.profileExists)
              vm.contacts[key].avatar = window.appBaseUrl3 + '/images/agents/' + angular.lowercase(value.username) + '.jpg?timestamp=' + new Date().getTime();
            else
              vm.contacts[key].avatar = '/efadminpanel/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();


          });
        }
        //////////

        /**
         * Change Contacts List Filter
         * @param type
         */
        function filterChange(type)
        {

            vm.listType = type;

            if ( type === 'all' )
            {
                vm.filterIds = null;
            }
            else if ( type === 'frequent' )
            {
                vm.filterIds = vm.user.frequentContacts;
            }
            else if ( type === 'starred' )
            {
                vm.filterIds = vm.user.starred;
            }
            else if ( angular.isObject(type) )
            {
                vm.filterIds = type.contactIds;
            }

            vm.selectedContacts = [];

        }

        /**
         * Open new contact dialog
         *
         * @param ev
         * @param contact
         */
        function openContactDialog(ev, contact)
        {
            $mdDialog.show({
                controller         : 'ContactDialogController',
                controllerAs       : 'vm',
                templateUrl        : 'app/adminPanel/user/dialogs/contact/contact-dialog.html',
                parent             : angular.element($document.find('#content-container')),
                targetEvent        : ev,
              skipHide : true,
                clickOutsideToClose: false,
                locals             : {
                    Contact : contact,
                    User    : vm.user,
                    Contacts: vm.contacts
                }
            })
              .then(function(userData){
              if (userData) {
                var ind = _.findIndex(vm.contacts, { username: userData.user.username });

                if(userData.message === 'create'){
                  vm.contacts.unshift(userData.user);
                  if (vm.contacts[0].profileExists)
                    vm.contacts[0].avatar = window.appBaseUrl3 + '/images/agents/' + angular.lowercase(vm.contacts[0].username) + '.jpg?timestamp=' + new Date().getTime();
                  else
                    vm.contacts[0].avatar = '/efadminpanel/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();
                } else if(userData.message === 'update' || userData.message === 'upload'){
                  if (ind != -1) {
                    vm.contacts[ind] = userData.user;
                    if (vm.contacts[ind].profileExists)
                      vm.contacts[ind].avatar = window.appBaseUrl3 + '/images/agents/' + angular.lowercase(vm.contacts[ind].username) + '.jpg?timestamp=' + new Date().getTime();
                    else
                      vm.contacts[ind].avatar = '/efadminpanel/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();

                  }
                }
              }
                vm.selectedContacts = [];
            })
            ;
        }

        /**
         * Delete Contact Confirm Dialog
         */
        function deleteContactConfirm(contact, ev)
        {
            var confirm = $mdDialog.confirm()
          var confirm = $mdDialog.confirm()
            .title($filter('translate')('CONTACTS.ContactDeleteConfirm'))
            .ariaLabel('delete contact')
            .targetEvent(ev)
            .ok($filter('translate')('generic.ok'))
            .cancel($filter('translate')('generic.cancel'));

            $mdDialog.show(confirm).then(function ()
            {
                deleteContact(contact);
                vm.selectedContacts = [];

            }, function (error)
            {
              console.log(error);
            });
        }

        /**
         * Delete Contact
         */
        function deleteContact(contact)
        {
          var params = {id:contact.id};
          userService.delete(params).then(function(response){
            var ind = _.findIndex(vm.contacts, { username: contact.username });
            vm.contacts.splice(ind, 1);
          }, function(error){
            console.log(error);
          });

        }

        /**
         * Delete Selected Contacts
         */
        function deleteSelectedContacts(ev)
        {
            var confirm = $mdDialog.confirm()
                .title($filter('translate')('CONTACTS.selectedDelete'))
                .ariaLabel('delete contacts')
                .targetEvent(ev)
                .ok($filter('translate')('generic.ok'))
                .cancel($filter('translate')('generic.cancel'));

            $mdDialog.show(confirm).then(function ()
            {
              var params = [];

                vm.selectedContacts.forEach(function (contact)
                {
                  params.push({id: contact.id});

                });
              var user = {ids: params}
                userService.deleteMulti(user).then(function(response){
                  response.message.forEach(function(id){
                    var ind = _.findIndex(vm.contacts, { id: id });
                    vm.contacts.splice(ind, 1);
                  });
                }, function(error){
                  console.log(error);
                });
                vm.selectedContacts = [];

            });

        }

        /**
         * Toggle selected status of the contact
         *
         * @param contact
         * @param event
         */
        function toggleSelectContact(contact, event)
        {
            if ( event )
            {
                event.stopPropagation();
            }

            if ( vm.selectedContacts.indexOf(contact) > -1 )
            {
                vm.selectedContacts.splice(vm.selectedContacts.indexOf(contact), 1);
            }
            else
            {
                vm.selectedContacts.push(contact);
            }
        }

        /**
         * Deselect contacts
         */
        function deselectContacts()
        {
            vm.selectedContacts = [];
        }

        /**
         * Sselect all contacts
         */
        function selectAllContacts()
        {
            vm.selectedContacts = $scope.filteredContacts;
        }

        /**
         *
         */
        function addNewGroup()
        {
            if ( vm.newGroupName === '' )
            {
                return;
            }

            var newGroup = {
                'id'        : msUtils.guidGenerator(),
                'name'      : vm.newGroupName,
                'contactIds': []
            };

            vm.user.groups.push(newGroup);
            vm.newGroupName = '';
        }

        /**
         * Delete Group
         */
        function deleteGroup(ev)
        {
            var group = vm.listType;

            var confirm = $mdDialog.confirm()
                .title('Are you sure want to delete the group?')
                .htmlContent('<b>' + group.name + '</b>' + ' will be deleted.')
                .ariaLabel('delete group')
                .targetEvent(ev)
                .ok('OK')
                .cancel('CANCEL');

            $mdDialog.show(confirm).then(function ()
            {

                vm.user.groups.splice(vm.user.groups.indexOf(group), 1);

                filterChange('all');
            });

        }

        /**
         * Toggle sidenav
         *
         * @param sidenavId
         */
        function toggleSidenav(sidenavId)
        {
            $mdSidenav(sidenavId).toggle();
        }

		function showAdvanced() {
			$mdDialog.show({
			controller: 'RoleController',
			controllerAs: 'vm',
			templateUrl: 'app/adminPanel/user/dialogs/role/role.html',
			parent: angular.element(document.body),
			clickOutsideToClose:false,
			//fullscreen: vm.customFullscreen // Only for -xs, -sm breakpoints.
		})
		.then(function(answer) {
		vm.status = 'You said the information was "' + answer + '".';
		}, function() {
		vm.status = 'You cancelled the dialog.';
		});
		};

    }

})();
