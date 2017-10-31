/**
 * Created by mumar on 1/7/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.service')
    .controller('ServiceController',ServiceController);


  function ServiceController(utilCustom,$document,$filter,serviceService,$mdDialog,$rootScope,$scope){
    var vm = this;


    vm.selectedService =  selectedService;
    vm.iniIt = iniIt;
    vm.create = create;
    vm.update = update;
    vm.findService = findService;
    vm.deleteService= deleteService;
    vm.addNew= addNew;
    vm.serviceList=[];

    function iniIt(){
      serviceService.list().then(function(respond){
        vm.serviceList = respond;
        vm.serviceList=setLanguageOnDate();
      },function(error){
        console.log(error);
      });
    }
    iniIt();
    function setLanguageOnDate(service){
      if(!service){
        return  vm.serviceList.map(function(service){
          service['dialNumber']=Number(service.dialNumber);
          service['dateCreated_lan']=moment(service.dateCreated).format('llll');
          service['lastUpdated_lan']=moment(service.lastUpdated).format('llll');
          return service;
        });
      }
      else{
        service['dialNumber']=Number(service.dialNumber);
        service['dateCreated_lan']=moment(service.dateCreated).format('llll');
        service['lastUpdated_lan']=moment(service.lastUpdated).format('llll');
        return service;
      }

    }
    function addNew(name){
      _.remove(vm.serviceList,function(r){
        return r==name;
      })
    }
    function findService(id){
      //var serviceFound=$filter('filter')(vm.serviceList,{id:id});
      //if(serviceFound)
      //return serviceFound[0].name;
    };

    $scope.$on('change_language',function(event,args){
      moment.locale(args.code);
      vm.serviceList=setLanguageOnDate();
      if(vm.service){

        vm.service=_.find(vm.serviceList,{id:vm.service.id});
      }

    });


    function create(event)
    {
      $mdDialog.show({
        controller         : 'ServiceCreateDialog',
        controllerAs       : 'vm',
        templateUrl        : 'app/TAM/services/dialog/create/create.html',
        parent             : angular.element($document.body),
        targetEvent        : event,
        clickOutsideToClose: true,
        locals             : {
          event              : event,
          serviceList:vm.serviceList
        }
      }).then(function(res){
        if(res!=undefined){
          setLanguageOnDate(res);
          vm.serviceList.push(res);
        }


        //   setServiceList();
      })
    };


    function selectedService(service){
      vm.service = angular.copy(service);
    };
    function update(service){
      var serviceCreated = $rootScope._user;
      var params = {id:service.id,name:service.name,description:service.description,dialNumber:service.dialNumber,updatedBy:{id:serviceCreated.id}};
      utilCustom.toasterLoading();
      serviceService.update(params).then(function(response){
        utilCustom.toaster($filter('translate')('service.service')+ ' ' + $filter('translate')('data.updated'));
        var serviceIndex= _.findIndex(vm.serviceList,{id:service.id});
        setLanguageOnDate(response);
        vm.serviceList[serviceIndex]=response;
        vm.service = undefined;
      },function(error){
        console.log(error);
        utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('service.service'));
        // setServiceList();
      });
    };
    function deleteService(service){
      utilCustom.toasterConfirm().then(function(response){
        if ( response == 'ok' ||  response) {
          var params = {id:service.id};
          utilCustom.toasterLoading();
          serviceService.delete(params).then(function(response){
              utilCustom.toaster($filter('translate')('service.service')+ ' ' + $filter('translate')('data.deleted'));

              _.remove(vm.serviceList,function(us){
                return us.id===service.id;
              });
              vm.service = undefined;
              //   setServiceList();
            },
            function(error){
              // console.log(error);
              utilCustom.toaster($filter('translate')('data.deleteError') +' ' + $filter('translate')('service.service'));

            });
        }else{
          vm.serviceList.push(service);

          utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
        }
      },function (error) {
        vm.serviceList.push(service);
      });


    }

    function setServiceList(){
      window.localStorage.setItem("servicesList",JSON.stringify(vm.serviceList))
    }




  }







})();
