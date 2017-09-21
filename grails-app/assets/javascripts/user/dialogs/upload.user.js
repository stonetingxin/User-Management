/**
 * Created by mumar on 4/6/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.users')
    .controller('UserUploadPicController',UserUploadPicController);

  function UserUploadPicController(Contact,User,Contacts, utilCustom,agentService,$mdDialog,$filter){

    var vm =this;
    vm.agentData = Contact;
    vm.userID= Contact.id;
    vm.closeDialog =closeDialog;
    vm.saveAgentPic = saveAgentPic;
    vm.removeProfilePic = removeProfilePic;
    vm.agent = {file:Contact.avatar};

    function  closeDialog(){
      $mdDialog.hide();
    }

    function  saveAgentPic(Uploadagent){
      if(!isWaveFile(Uploadagent.file.name)){
        utilCustom.toaster($filter('translate')('agent.picFormat'));
        vm.fileFormate = true;
      }else{
        vm.fileFormate = false;
        var params = {file:Uploadagent.file,agentId:angular.lowercase(Contact.username)};
        utilCustom.toasterLoading();
        agentService.updateProfilePic(params).then(function(response){
          utilCustom.toaster($filter('translate')('agent.picUploaded'));
          vm.agentData.profileExists = true;
          $mdDialog.hide({userID:Contact.username,message:'upload'});
        },function(error){
          utilCustom.toaster($filter('translate')('agent.picUploadError'));
        // console.log(error);
        });
      }
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
    function getExtension(filename) {
      var parts = filename.split('.');
      return parts[parts.length - 1];
    }
    function isWaveFile(filename) {
      var ext = getExtension(filename);
      switch (ext.toLowerCase()) {
        case 'jpg':
          return true;
      }
      return false;
    }
  }
})();
