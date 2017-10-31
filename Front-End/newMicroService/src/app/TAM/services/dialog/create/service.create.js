/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.service')
    .controller('ServiceCreateDialog',serviceCreateDialog);

  function serviceCreateDialog($mdDialog,serviceService,utilCustom,$rootScope,serviceList,$filter){
     var vm = this;
    vm.closeDialog = closeDialog;
    vm.saveService = saveService;
	   vm.serviceList = serviceList;



    function saveService(service){
      var userCreated = $rootScope._user;
      var params = {id:service.id,name:service.name,description:service.description,dialNumber:service.dialNumber
        ,createdBy:userCreated.username};
      serviceService.get_api.save(params,function(response){
        utilCustom.toaster($filter('translate')('service.service')+ ' ' + $filter('translate')('data.created'));
        $mdDialog.hide(response);
      },function(error){
        console.log(error);
        utilCustom.toaster($filter('translate')('data.createError') +' ' + $filter('translate')('service.service'));
      });
    }
    function closeDialog(){
      $mdDialog.hide();
    }
  }

})();
