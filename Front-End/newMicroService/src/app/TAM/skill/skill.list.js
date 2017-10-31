/**
 * Created by mumar on 1/7/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.skill')
    .controller('SkillController',SkillController);


  function SkillController(utilCustom,$document,$filter,skillService,$mdDialog,$rootScope){
    var vm = this;


    vm.selectedSkill =  selectedSkill;
    vm.iniIt = iniIt;
    vm.create = create;
    vm.update = update;
    vm.findSkill = findSkill;
    vm.deleteSkill= deleteSkill;
    vm.addNew= addNew;
    vm.skillList=[];

    function iniIt(){
      skillService.list().then(function(respond){
        vm.skillList = respond.skill;
      },function(error){
        console.log(error);
      });
    }
    iniIt();
    function addNew(name){
      _.remove(vm.skillList,function(r){
        return r==name;
      })
    }
    function findSkill(id){
      //var skillFound=$filter('filter')(vm.skillList,{id:id});
      //if(skillFound)
      //return skillFound[0].name;
    };




    function create(event)
    {
      $mdDialog.show({
        controller         : 'SkillCreateDialog',
        controllerAs       : 'vm',
        templateUrl        : 'app/TAM/skill/dialog/create/create.html',
        parent             : angular.element($document.body),
        targetEvent        : event,
        clickOutsideToClose: true,
        locals             : {
          event              : event,
          skillList:vm.skillList
        }
      }).then(function(res){
        if(res!=undefined)
        vm.skillList.push(res);
      })
    };


    function selectedSkill(skill){
      vm.skill = angular.copy(skill);
    };
    function update(skill){
      utilCustom.toasterLoading();
      var skillCreated = $rootScope._user;
      var params = {id:skill.skillId,skill:skill,updatedBy:{id:skillCreated.id}};
      skillService.update(params).then(function(response){
        vm.skillList[_.findIndex(vm.skillList,{skillId:skill.skillId})]=skill;
        utilCustom.toaster($filter('translate')('skill.skill')+ ' ' + $filter('translate')('data.updated'));
         vm.skill = undefined;
      },function(error){
        console.log(error);
        utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('skill.skill'));
      });
    };
    function deleteSkill(skill){
      utilCustom.toasterConfirm().then(function(response){
        if ( response == 'ok' ||  response) {
          utilCustom.toasterLoading();
          var params = {id:skill.skillId};
          skillService.delete(params).then(function(response){
            utilCustom.toaster($filter('translate')('skill.skill')+ ' ' + $filter('translate')('data.deleted'));

            _.remove(vm.skillList,function(us){
              return us.skillName===skill.skillName;
            });
           vm.skill = undefined;

          },function(error){
            console.log(error);
            utilCustom.toaster($filter('translate')('data.deleteError') +' ' + $filter('translate')('skill.skill'));
          });
        }else{
          vm.skillList.push(skill);
          utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
        }
      },function (error) {
        vm.skillList.push(skill);
      
      });

    };

    function setSkillList(){
      window.localStorage.setItem("skillList",JSON.stringify(vm.skillList))
    }


  }







})();
