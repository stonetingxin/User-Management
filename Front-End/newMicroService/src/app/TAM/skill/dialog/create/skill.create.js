/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.skill')
    .controller('SkillCreateDialog',skillCreateDialog);

  function skillCreateDialog($mdDialog,skillService,utilCustom,$rootScope,skillList,$filter){
     var vm = this;
    vm.closeDialog = closeDialog;
    vm.saveSkill = saveSkill;
	vm.skillList = skillList;



    function saveSkill(skill){
      var userCreated = $rootScope._user;
      var params = {skill:skill,id:skill.skillName,createdBy:{id:userCreated.id}};
      skillService.get_api.save(params,function(response){
        utilCustom.toaster($filter('translate')('skill.skill')+ ' ' + $filter('translate')('data.created'));
        $mdDialog.hide(response);
      },function(error){
        console.log(error);
        utilCustom.toaster($filter('translate')('data.createError') +' ' + $filter('translate')('skill.skill'));
      });
    }
    function closeDialog(){
      $mdDialog.hide();
    }
  }

})();
