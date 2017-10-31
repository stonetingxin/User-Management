/**
 * Created by mumar on 1/7/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.trigger')
    .controller('TriggerController',triggerController)

  function triggerController(utilCustom,msUtils,$log,$document,$filter,triggerService,$mdDialog,groupService,skillService,teamService){
    var vm = this;
    // vm.triggerList =  TriggerData.triggerList;

    vm.selectedTrigger =  selectedTrigger;
    vm.addNewSkill = addNewSkill;
    vm.dialogShowDetail=dialogShowDetail;
    vm.skillQuerySearch=skillQuerySearch;
    vm.filterSkill=filterSkill;
    vm.toggleInArraySkill = toggleInArraySkill;
    vm.existsSkill = existsSkill;
    vm.findSkill = findSkill;
    vm.selectedSkill = selectedSkill;
    vm.saveTrigger=saveTrigger;
    function iniIt(){
      triggerService.list().then(function(response){
        vm.triggerList= response.resource;
        parseTriggerList(vm.triggerList);
      },function(error){

      });
      var promise = new Promise(function(resolve,error){

        resolve(syncOtherList())
      });
      promise.then(function(resolve){
        console.log('synced');
      })

    }
    iniIt();


    function syncOtherList(){

    }
    function toggleInArraySkill(item,array){

      var arraySkillId =_.map(array,'skillId');
      var sk = item.skillId.toString();
      if (arraySkillId.indexOf(sk) == -1 )
      {

        if(!_.find(array,{skillId:item.skillId.toString()}))
          array.push({skillId:item.skillId.toString(),skillName:item.skillName,competencelevel:5});
      }
      else
      {
        array.splice(arraySkillId.indexOf(sk), 1);
      }
    }
    function existsSkill(item, list)
    {
      var listId = _.map(list,'skillId');

      return listId.indexOf(item.toString()) > -1;
    }
    function  findSkill(skill){
      if(skill){
        var triggerFound = _.find(vm.triggerList,{userID:skill.trigger});

      }
      if(triggerFound){

        var fou = _.find(triggerFound.skills,{skillId:skill.id.toString()});
        return fou
      }

    }
    function selectedSkill(skillChip){
      console.log(skillChip);
    }
    function parseTriggerList(){
      angular.forEach(vm.triggerList,function(value,key){
        var skills = [];
        angular.forEach(value.skillMap.skillCompetency,function(va,k){
          var skillId = va.skillNameUriPair.refURL.split('/');
          var skill = {competencelevel:va.competencelevel,skillId:skillId[skillId.length-1],skillName:va.skillNameUriPair['@name'],refURL:va.refURL};
          skills.push(skill);
        });
        if(value.resourceGroup){
          var group = value.resourceGroup.refURL.split('/');
          vm.triggerList[key].groupId=Number(group[group.length-1]);
        }
        if(value.team){
          var team = value.team.refURL.split('/');
          vm.triggerList[key].teamId=Number(team[team.length-1]);
        }

        vm.triggerList[key].skills= skills;
      });
    };
    vm.selectUserIndex = function (index) {
      if (vm.selectedUserIndex !== index) {
        vm.selectedUserIndex = index;
      }
      else {
        vm.selectedUserIndex = undefined;
      }
    };
    vm.getCheckedUsers =function(){
      return $filter('filter')(vm.triggerList,{checked:true});
    };
    vm.skillName = function(id){
      return _.result(_.find(vm.skillList, {id:Number(id)}), 'name');
    };
    vm.groupName = function(id){
      return _.result(_.find(vm.groupList, {id:Number(id)}), 'name');
    };
    vm.teamName = function(id){
      return _.result(_.find(vm.teamList, {teamId:Number(id)}), 'teamname');
    };
    vm.allChecked=function(){

      return vm.getCheckedUsers().length == vm.triggerList.length ;
    };
    vm.checkAll = function(value){

      angular.forEach(vm.triggerList,function(bu){
        bu.checked=value;
      })
    };
    vm.getCheckedIds = function(){
      return _.pluck(vm.getCheckedUsers(),'id');
    };
    function dialogShowDetail(trigger,e){
      $mdDialog.show({
        controller         : 'TriggerEditController',
        controllerAs       : 'vm',
        templateUrl        : 'app/adminPanel/trigger/edit.html',
        parent             : angular.element($document.body),
        targetEvent        : e,
        clickOutsideToClose: true,
        locals             : {
          trigger      : trigger,
          skillList      : vm.skillList,
          groupList      : vm.groupList
        }
      }).then(function(triggerData){
        var ind=_.findIndex(vm.triggerList,{userId:triggerData.userId});
        vm.triggerList[ind]=triggerData;
      })
    };

    function selectedTrigger(trigger){
      vm.trigger= trigger;
    };
    function saveTrigger(trigger){
      var params = angular.copy(trigger);
      var tUrl='';
      if(params.teamId){
        if(params.team){
          tUrl = params.team.refURL.split('/');
          tUrl[tUrl.length-1] = params.teamId;
          params.team.refURL = tUrl.join('/');
          params.team['@name']=vm.teamName(params.teamId);
        }else{
          tUrl =  trigger.self.replace('resource','team').split('/');
          tUrl[tUrl.length-1] = params.teamId;
          params.team ={'@name':vm.teamName(params.teamId),refURL : tUrl.join('/')};

        }

      }
      if(params.groupId){
        if(params.resourceGroup){
          tUrl = params.resourceGroup.refURL.split('/');
          tUrl[tUrl.length-1] = params.groupId;
          params.resourceGroup.refURL = tUrl.join('/');
          params.resourceGroup['@name']=vm.groupName(params.groupId);
        }else{
          tUrl =  trigger.self.replace('resource','resourceGroup').split('/');
          tUrl[tUrl.length-1] = params.groupId;
          params.resourceGroup={'@name': vm.groupName(params.groupId),refURL : tUrl.join('/')};
        }

      }
      if(params.skills){
        if(params.skillMap){
          var skillCompe = [];
          params.skillMap.skillCompetency=[];
          angular.forEach(params.skills,function(va,ke){
            tUrl =  trigger.self.replace('resource','skill').split('/');
            tUrl[tUrl.length-1] = va.skillId;
            var skillCompetency = {competencelevel:va.competencelevel,skillNameUriPair:{'@name':va.skillName,refURL:tUrl.join('/')}};
            params.skillMap.skillCompetency.push(skillCompetency);
          })
        }
      }

      var pam = {
        firstName:params.firstName,
        lastName:params.lastName,
        userID:params.userID,
        extension:params.extension,
        alias:params.alias,
        primarySupervisorOf:[],
        secondarySupervisorOf:[],
        self:params.self,
        type:params.type,
        skillMap:params.skillMap,
        resourceGroup:params.resourceGroup,
        team:params.team,
        autoAvailable:params.autoAvailable
      };
      triggerService.update({trigger:pam,id:trigger.userID}).then(function(response){
        utilCustom.toaster('Trigger has been updated');
      },function(error){
        utilCustom.toaster('Error while updating trigger');
      })
    }
    function addNewSkill(){
      var newSkill = vm.trigger.skillId.pop();
      var foundSkill = _.find(skillList, {name:newSkill});
      if(foundSkill===undefined){
        var count = skillList.length+1;
        skillList.push({id:count,name:newSkill});
        vm.trigger.skillId.push({id:count});

      }
    }
    function skillQuerySearch(query) {
      return query ? vm.skillList.filter(createFilterFor(query)) : [];
    }
    function createFilterFor(query){
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item)
      {
        return angular.lowercase(item.skillName).indexOf(lowercaseQuery) >= 0;
      };
    }
    function filterSkill(label)
    {
      if ( !vm.skillSearchText || vm.skillSearchText === '' )
      {
        return true;
      }

      return angular.lowercase(label.skillName).indexOf(angular.lowercase(vm.skillSearchText)) >= 0;
    }
    function triggerEditForm(trigger,skillList,groupList,e){
      $mdDialog.show({
        controller         : 'TriggerEditController',
        controllerAs       : 'vm',
        templateUrl        : 'app/adminPanel/trigger/edit.html',
        parent             : angular.element($document.body),
        targetEvent        : e,
        clickOutsideToClose: true,
        locals             : {
          trigger      : trigger,
          skillList      : skillList,
          groupList      : groupList
        }
      }).then(function(triggerData){
        var ind=_.findIndex(vm.triggerList,{userId:triggerData.userId});
        vm.triggerList[ind]=triggerData;
      })
    }

  }







})();
