/**
 * Created by mumar on 1/14/2016.
 */

(function(){
  'use strict';

  angular
    .module('app.queue')
    .controller('EditQueueController',editQueueController);



  function editQueueController($mdDialog,agentList,groupList,queue,queueList,queueService,utilCustom,$filter){
    var vm = this;

    vm.closeDialog= closeDialog;

    vm.editQueue=editQueue;
      vm.queuingModel = [{id:"SKILL_GROUP",name:$filter('translate')('csq.SKILL_GROUP')},{id:"RESOURCE_GROUP",name:$filter('translate')('csq.RESOURCE_GROUP')}];
     vm.queueList=queueList;
    vm.other={selectionCriteria:'',groupName:''};
    vm.queue=queue;
    vm.translationData = {
      group: $filter('translate')('group.group'),
      selectCriteria: $filter('translate')('csq.selectCriteria')
    };
    !vm.queue.wrapupTime ? vm.queue.enable_wrapup_time=false: vm.queue.enable_wrapup_time=true;
    if(vm.queue.resourcePoolType==="RESOURCE_GROUP"){
      vm.resourceSelectionCriteriaList=[
        {id:"Longest Available",name:$filter('translate')("csq.Longest_Available")},
        {id:"Most Handled Contacts",name:$filter('translate')("csq.Most_Handled_Contacts")},
        {id:"Shortest Average Handle Time",name:$filter('translate')("csq.Shortest_Average_Handle_Time")},
        {id:"Linear", name:$filter('translate')("csq.Linear")},
        {id:"Circular",name:$filter('translate')("csq.Circular")}
      ];
      vm.groupList = groupList;
      vm.queueGroupName =[];
      angular.forEach(queueList,function(value,key){
        if(value.resourcePoolType==="RESOURCE_GROUP"){
          var grp = value.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair;
          if(grp!=undefined)
            vm.queueGroupName.push(grp['@name']);
        }
      });
      vm.queueGroupName = _.difference(_.map(vm.groupList,'name'),vm.queueGroupName);
      var resourceGroup = vm.queue.poolSpecificInfo.resourceGroup;
      vm.other.selectionCriteria = vm.queue.poolSpecificInfo.resourceGroup.selectionCriteria;
      if(resourceGroup.resourceGroupNameUriPair){
        var groupName  = resourceGroup.resourceGroupNameUriPair['@name'];
          if(groupName){
            vm.other.groupName =groupName;
            vm.queueGroupName.push(groupName);
          }
      }


    }
    else{
      vm.resourceSelectionCriteriaList=[  {id:"Longest Available",name:$filter('translate')('csq.Longest_Available')},
      {id:"Most Handled Contacts",name:$filter('translate')("csq.Most_Handled_Contacts")},
      {id:"Shortest Average Handle Time",name:$filter('translate')("csq.Shortest_Average_Handle_Time")},
      {id:"Most Skilled",name:$filter('translate')("csq.Most_Skilled")},
      {id:"Most Skilled by Weight",name:$filter('translate')("csq.Most_Skilled_by_Weight")},
      {id:"Least Skilled by Weight",name:$filter('translate')("csq.Least_Skilled_by_Weight")},
      {id:"Most Skilled by Order",name:$filter('translate')("csq.Most_Skilled_by_Order")},
      {id:"Least Skilled by Order",name:$filter('translate')("csq.Least_Skilled_by_Order")}];
      vm.other.selectionCriteria = vm.queue.poolSpecificInfo.skillGroup.selectionCriteria;
    }




    function editQueue(queueData,other){
      var queue = angular.copy(queueData);
        if(!vm.queue.enable_wrapup_time) queue.wrapupTime=0;
      if(queue.resourcePoolType==="RESOURCE_GROUP"){
        queue.poolSpecificInfo.resourceGroup.selectionCriteria = other.selectionCriteria;
        var found = _.find(vm.groupList,{name:other.groupName});
        if(found){
          queue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair={'@name':found.name,refURL:found.self};
          queue.poolSpecificInfo.resourceGroup.orderedResources=undefined;
          queue.poolSpecificInfo.resourceGroup.orderedResources=[];
          queue.groupId=undefined;
          agentList.map(function(agnt){
            if(agnt.resourceGroup){
              if(agnt.resourceGroup['@name']==other.groupName){
                queue.poolSpecificInfo.resourceGroup.orderedResources.push({'@name':agnt.firstName+''+agnt.lastName,refURL:agnt.self});
              }
            }

          })
        }

      }else{
        vm.queue.poolSpecificInfo.skillGroup.selectionCriteria=vm.other.selectionCriteria;
      }
      utilCustom.toasterLoading();
      queueService.update({queue:queue,id:queue.id}).then(function(response){
        utilCustom.toaster($filter('translate')('csq.csq')+ ' ' + $filter('translate')('data.updated'));
        vm.queue.poolSpecificInfo = response.poolSpecificInfo
        $mdDialog.hide(queue);
      },function(error){
        utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('csq.csq'));
      })

    };
    function closeDialog()
    {
      $mdDialog.hide();
    }
  }
})();
