/**
 * Created by mumar on 1/13/2016.
 */
(function(){
  'use strict';

  angular
    .module('app.queue')
    .controller('CreateQueueController',createQueueController)
    .directive('requireIfTrue',requireIfTrue);



  function createQueueController($mdDialog,groupList,queueList,utilCustom,team,queueService,$filter,teamService){
    var vm = this;

    vm.closeDialog= closeDialog;
    vm.createQueue=createQueue;
    vm.changeQueue=changeQueue;
    vm.groupList = groupList;
    vm.queueList = queueList;
    vm.team= team;
    vm.queueGroupName =[];
    angular.forEach(queueList,function(value,key){
      if(value.resourcePoolType==="RESOURCE_GROUP"){
        var grp = value.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair;
        if(grp!=undefined)
          vm.queueGroupName.push(grp['@name']);
      }
    });
    vm.queueGroupName = _.difference(_.map(vm.groupList,'name'),vm.queueGroupName);
    vm.queue={resourcePoolType:'SKILL_GROUP',resourceSelectionCriteriaList:"Longest Available"};
    vm.resourceSelectionCriteriaList=[];
    changeQueue('SKILL_GROUP');
    vm.translationData = {
      group: $filter('translate')('group.group'),
      selectCriteria: $filter('translate')('csq.selectCriteria')
    };
    vm.queuingModel = [{id:"SKILL_GROUP",name:$filter('translate')('csq.SKILL_GROUP')},{id:"RESOURCE_GROUP",name:$filter('translate')('csq.RESOURCE_GROUP')}];
    vm.other={selectionCriteria:'',groupName:''};

    function changeQueue(resourcePoolType){
      if(resourcePoolType==='SKILL_GROUP')
      vm.resourceSelectionCriteriaList=[
        {id:"Longest Available",name:$filter('translate')('csq.Longest_Available')},
      {id:"Most Handled Contacts",name:$filter('translate')("csq.Most_Handled_Contacts")},
      {id:"Shortest Average Handle Time",name:$filter('translate')("csq.Shortest_Average_Handle_Time")},
      {id:"Most Skilled",name:$filter('translate')("csq.Most_Skilled")},
      {id:"Most Skilled by Weight",name:$filter('translate')("csq.Most_Skilled_by_Weight")},
      {id:"Least Skilled by Weight",name:$filter('translate')("csq.Least_Skilled_by_Weight")},
      {id:"Most Skilled by Order",name:$filter('translate')("csq.Most_Skilled_by_Order")},
      {id:"Least Skilled by Order",name:$filter('translate')("csq.Least_Skilled_by_Order")}
    ];
      else{
        vm.resourceSelectionCriteriaList=[
          {id:"Longest Available",name:$filter('translate')("csq.Longest_Available")},
          {id:"Most Handled Contacts",name:$filter('translate')("csq.Most_Handled_Contacts")},
          {id:"Shortest Average Handle Time",name:$filter('translate')("csq.Shortest_Average_Handle_Time")},
          {id:"Linear", name:$filter('translate')("csq.Linear")},
          {id:"Circular",name:$filter('translate')("csq.Circular")}
        ];

      }
    }

   function addCsQInTeam(csq){
     var team =  window.localStorage.getItem('currentTeam');
     var teamId;
     if(team){
       team  = JSON.parse(team);
       teamId= team.id;
     }
     if(teamId){
        teamService.show({id:teamId}).then(function(data){
          vm.team = data;
          if(!vm.team.hasOwnProperty('csqs')){
            vm.team.csqs={csq:[]};
          }
          vm.team.csqs.csq.push({"@name":csq.name,refURL:csq.self});
       teamService.update({id:teamId,team:vm.team}).then(function(data) {console.log(data)},function (error) {console.log(error);})

        },
        function(error){
          console.log(error)
        });
     }

   }
    function createQueue(queue){

      var params = {
        name:queue.name,
        "queueType": "VOICE",
        "routingType": "VOICE",
        "queueAlgorithm": "FIFO",
        "autoWork": false,
        "wrapupTime": 0,
        "resourcePoolType": queue.resourcePoolType,
        "serviceLevel": 5,
        "serviceLevelPercentage": 50,
        "pollingInterval": 600,
        "snapshotAge": 120,
        poolSpecificInfo:{}
      };
      var poolSpecificInfo = {resourceGroup:{selectionCriteria:''}};
      if(queue.resourcePoolType==="RESOURCE_GROUP"){
        if(!queue.groupName){
           utilCustom.toaster($filter('translate')('csq.groupSelected'));
           return
        }
        poolSpecificInfo.resourceGroup.selectionCriteria=queue.selectionCriteria;
        var found = _.find(vm.groupList,{name:queue.groupName});
        if(found){
          poolSpecificInfo.resourceGroup['resourceGroupNameUriPair']={'@name':found.name,refURL:found.self};
        }

      }
      else{
        poolSpecificInfo = {skillGroup:{selectionCriteria:vm.queue.selectionCriteria}};

      }
      params.poolSpecificInfo = poolSpecificInfo;
      utilCustom.toasterLoading();

      queueService.get_api.save({queue:params,id:queue.name},function(response){
        if(team!='all'){
          addCsQInTeam(response);
        }
        utilCustom.toaster($filter('translate')('csq.csq')+ ' ' + $filter('translate')('data.created'));
        $mdDialog.hide(response);
      },function(error){
        utilCustom.toaster($filter('translate')('data.createError') +' ' + $filter('translate')('csq.csq'));

      });

    }
    function closeDialog()
    {
      $mdDialog.hide();
    }
  }
  function requireIfTrue($compile) {
    return {
      require: '?ngModel',
      link: function (scope, el, attrs, ngModel) {
        if (!ngModel) {
          return;
        }

        if(attrs.requireiftrue==="true"){
          console.log('should require');
          el.attr('required', true);
          el.removeAttr('requireiftrue');
          $compile(el[0])(scope);
        }
        else{
          console.log('should not require');
        }
      }
    };
  };
})();
