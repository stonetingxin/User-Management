/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.group')
    .controller('GroupCreateDialog',groupCreateDialog);

  function groupCreateDialog($mdDialog,groupService,utilCustom,$rootScope,groupList,$filter){
     var vm = this;
    vm.closeDialog = closeDialog;
    vm.saveGroup = saveGroup;
    vm.groups = groupList;



    function saveGroup(group){
      var userCreated = $rootScope._user;
      var params = {group:group,id:group.name,createdBy:{id:userCreated.id}};
      groupService.get_api.save(params,function(response){
        utilCustom.toaster($filter('translate')('group.group')+' ' + $filter('translate')('data.created'));
        $mdDialog.hide(response);
      },function(error){
       // console.log(error);
        utilCustom.toaster($filter('translate')('data.createError') +' ' + $filter('translate')('group.group'));
      });
    }
    function closeDialog(){
      $mdDialog.hide();
    }
  }

})();
