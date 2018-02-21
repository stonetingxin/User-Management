(function () {
  'use strict';

  angular
    .module('app.users')
    .controller('preReqController', preReqController);

  /** @ngInject */
  function preReqController(permissions, permission, preReqs, $mdDialog, $filter) {
    var vm = this;

    // Data
    vm.title = $filter('translate')('CONTACTS.preReqPermissions');
    vm.permissions = angular.copy(permissions);
    vm.preReqs = angular.copy(preReqs);
    vm.preReqsCopy = angular.copy(preReqs);
    vm.translationData = {
      permissions: $filter('translate')('CONTACTS.preReqPermissions')
    };
    // vm.permission = angular.copy(permission);

    // Methods
    vm.closeDialog = closeDialog;
    vm.queryArraySearch = queryArraySearch;
    vm.addPermissions = addPermissions;

    function addPermissions() {
      var perms = [];
      angular.forEach(vm.preReqs, function (o) {
        perms.push({id: o.id});
      });
      perms.push({id: permission.id});
      $mdDialog.hide({perms: perms, message: "add"});
    }


    function queryArraySearch(query, objectList) {
      var results = query ? objectList.filter(createFilterFor(query)) : [];

      for (var i = 0; i < vm.preReqs.length; i++) {

        for (var i2 = 0; i2 < results.length; i2++) {

          if (results[i2].name.toLowerCase() === vm.preReqs[i].name.toLowerCase()) {

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

    function createFilterFor(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item) {
        return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
      };
    }

    /**
     * Close dialog
     */
    function closeDialog() {
      $mdDialog.hide("bloob");
    }
  }
})();
