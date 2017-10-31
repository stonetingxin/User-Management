/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.team')
    .controller('TeamCreateDialog',teamCreateDialog);

  function teamCreateDialog($mdDialog,teamService,utilCustom,$rootScope){
     var vm = this;
    vm.closeDialog = closeDialog;
    vm.saveTeam = saveTeam;




    function saveTeam(team){
      var userCreated = $rootScope._user;
      var params = {team:team,createdBy:{id:userCreated.id}};
      teamService.get_api.save(params,function(response){
        utilCustom.toaster('Team {'+ team.teamname+'} has been created');
        $mdDialog.hide(response);
      },function(error){
        console.log(error);
        utilCustom.toaster('Error while creating team');
      });
    }
    function closeDialog(){
      $mdDialog.hide();
    }
  }

})();
