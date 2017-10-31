/**
 * Created by mumar on 1/7/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.group')
    .controller('GroupController',GroupController);


  function GroupController(utilCustom,$document,$filter,groupService,$mdDialog,$rootScope){
    var vm = this;


    vm.selectedGroup =  selectedGroup;
    vm.iniIt = iniIt;
    vm.create = create;
    vm.update = update;

    vm.deleteGroup= deleteGroup;
    vm.addNew = addNew;
    vm.groupList=[];
    vm.iniIt = iniIt;


    function iniIt(){

      groupService.list().then(function(respond){
        vm.groupList = respond.resourceGroup;
      },function(error){
        console.log(error);
      });
    }

   iniIt();




    function create(event)
    {
      $mdDialog.show({
        controller         : 'GroupCreateDialog',
        controllerAs       : 'vm',
        templateUrl        : 'app/TAM/group/dialog/create/create.html',
        parent             : angular.element($document.body),
        targetEvent        : event,
        clickOutsideToClose: true,
        locals             : {
          event              : event,
          groupList:vm.groupList
        }
      }).then(function(res){
        if(res!=undefined){
          vm.groupList.push({id:res.id,name:res.name,self:res.self});
        }

      })
    };

    function addNew(name){
      _.remove(vm.groupList,function(r){
        return r==name;
      })
    }
    function selectedGroup(group){
      vm.group = angular.copy(group);
    };
    function update(group){
      var groupCreated = $rootScope._user;
      var params = {group:group,id:group.id,createdBy:{id:groupCreated.id}};
      utilCustom.toasterLoading();
      groupService.update(params).then(function(response){
          vm.group = undefined;
        vm.groupList[_.findIndex(vm.groupList,{id:group.id})]=group;
        utilCustom.toaster($filter('translate')('group.group')+ ' ' + $filter('translate')('data.updated'));
      },function(error){
        console.log(error);
        utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('group.group'));

      });
    };
    function deleteGroup(group){
      utilCustom.toasterConfirm().then(function(response){
        if ( response == 'ok' ||  response) {
          utilCustom.toasterLoading();
          var params = {id:group.id};
          groupService.delete(params).then(function(response){
            utilCustom.toaster($filter('translate')('group.group')+ ' ' + $filter('translate')('data.deleted'));

            _.remove(vm.groupList,function(us){
              return us.id===group.id;
            });
            // setGroupList();
          },function(error){
            console.log(error);
            utilCustom.toaster($filter('translate')('data.deleteError') +' ' + $filter('translate')('group.group'));
          });
        }else{
          vm.groupList.push(group);
          utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
        }
      },function (error) {
        vm.groupList.push(group);
      })

    };

    function setGroupList(){
      window.localStorage.setItem("groupList",JSON.stringify(vm.groupList))
    }


  }







})();
