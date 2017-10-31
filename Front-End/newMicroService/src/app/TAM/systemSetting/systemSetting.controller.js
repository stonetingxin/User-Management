/**
 * Created by mumar on 4/26/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.systemSetting')
    .controller('systemSettingController',systemSettingController);

  function systemSettingController(MicroserviceFactory,$state,$scope){
    var vm = this;

    function iniIt(){

        $state.go('app.systemSetting.group');
       vm.onlyAdminPanel=true; vm.onlyEcm=false;



    }

    $scope.$on('$stateChangeSuccess', function ()
    {
     vm.currentState = $state.current.name.split('.');
      vm.currentState = vm.currentState[ vm.currentState.length-1];
    });
    if(MicroserviceFactory.APAdmin()){
      vm.authorizationRequires=false;
      iniIt();

    }else{
      vm.authorizationRequires=true;
    }

  }


})();
