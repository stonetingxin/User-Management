/**
 * Created by mumar on 1/7/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.team')
    .controller('TeamController',TeamController);


  function TeamController(MicroserviceFactory, msUtils,utilCustom,$state,$filter,teamService,$mdDialog,$rootScope,serviceService,agentService,queueService,$scope,ApplicationService){
    var vm = this;


    vm.selectedTeam =  selectedTeam;
    vm.iniIt = iniIt;
    vm.create = create;
    vm.update = update;
    vm.findTeam = findTeam;
    vm.deleteTeam= deleteTeam;
    vm.queryArraySearch = queryArraySearch;
    //vm.querySearchForSupervisor = querySearchForSupervisor;
    vm.addSupervisor = addSupervisor;
    vm.querySearchForQueue = querySearchForQueue;
    vm.teamList=[];vm.agentList=[];vm.applicationList=[];vm.serviceList=[];
    vm.createFormOr = undefined;
    vm.filterSelected=true;
    vm.filterApplication = filterApplication;
    vm.filterSupervisor = filterSupervisor;
    vm.filterResource = filterResource;
    vm.filterCsq = filterCsq;
    vm.filterService = filterService;
    vm.querySearch = querySearch;
    vm.exists = exists;
    vm.existsMain = msUtils.exists;
    vm.findApplicationName=findApplicationName;
    vm.findServiceName=findServiceName;
    vm.toggleInArray = msUtils.toggleInArray;
    vm.translationData = {
      supervisor: $filter('translate')('team.supervisor'),
      secondarySupervisor: $filter('translate')('team.secondarySupervisor'),
      csq: $filter('translate')('csq.csqs'),
      agent: $filter('translate')('agent.agents'),
      applications:$filter('translate')('application.applications'),
      services:$filter('translate')('service.services')
    };
    if(MicroserviceFactory.APAdmin()){
      vm.authorizationRequires=false;
      getAgent();getApplication({id:'all'});getService();
      queueService.list('').then(function(response){
        vm.queueList = response;
        vm.queues = [];
        angular.forEach(vm.queueList,function(value,key){
          if(value.queueType==("VOICE"))
            vm.queues.push({
              lower_name:angular.lowercase(value.name),
              name:value.name,
              refURL:value.self,
              id:value.id,
              csq:{'@name':value.name,refURL:value.self}
            })
        });

      },function(error){
        console.log(error);
      });
    }else{
      vm.authorizationRequires=true;
    }

    /**
     * Search for contacts.
     */
    function querySearch (query,type) {
      var results=[];
      if(type==2){
        results = query ?
          vm.supervisors.filter(createFilterFor(query)) : [];
          for (var i = 0; i < vm.team.secondarySupervisors.length; i++) {
                for (var i2 = 0; i2 < results.length; i2++) {

                  if (results[i2].name.toLowerCase() == vm.team.secondarySupervisors[i].name.toLowerCase()) {

                      results.splice(i2, 1);

                  }

              }


          }
        return results;
      }else{
        results = query ? vm.agents.filter(createFilterFor(query)) : [];
        for (var i = 0; i < vm.team.resources.length; i++) {

            for (var i2 = 0; i2 < results.length; i2++) {

                if (results[i2].name.toLowerCase() == vm.team.resources[i].name.toLowerCase() ) {

                    results.splice(i2, 1);

                }

            }

        }
        return results;
      }

    }

    function getAgent(){
    //  if(!$rootScope.agentList){
        agentService.list('').then(function(response){
          vm.agentList = response.resource;
          separateAgentFromMainList();
        },function(error){
          console.log(error);
        });
      // }else{
      //   vm.agentList = $rootScope.agentList;
      //   separateAgentFromMainList();
      // }

    }

    function exists(item,list,attribute) {
      if(attribute=='agent'){
         return !!_.find(list,{userId:item.userId});
      }
      if(attribute=='csq'){
         return !!_.find(list,{id:item.id});
      }
      if(attribute=='app'){
         return !!_.find(list,{idS:item.idS});
      }

    }
    function separateAgentFromMainList(){

      vm.agentWithNoTeams = [];  vm.supervisorList=[];
      angular.forEach(vm.agentList,function(value,key){
        if(value.type==2){
          vm.supervisorList.push(value);
          if(value.team['@name']==="Default"){
            // console.log(value);
            vm.agentWithNoTeams.push(value)
          }
        }else{
          if(value.team['@name']==="Default"){
            // console.log(value);
            vm.agentWithNoTeams.push(value)
          }
        }

      });
      vm.onlyAgents = vm.agentList.filter(function(ag){
        if(ag.type==1){
          return ag;
        }
      });
      vm.onlyAgents = resourceImages(vm.onlyAgents);
      vm.agents= resourceImages(vm.agentWithNoTeams);
      vm.supervisors = fetchSupervisor(vm.supervisorList);
    }
    function querySearchForQueue (query) {
      var results = query ?
        vm.queues.filter(createFilterForQueue(query)) : [];

        for (var i = 0; i < vm.team.csqs.length; i++) {

            for (var i2 = 0; i2 < results.length; i2++) {

                if (results[i2].name.toLowerCase() == vm.team.csqs[i].name.toLowerCase()) {

                    results.splice(i2, 1);

                }

            }

        }
      return results;
    }
    function getApplication(param){
      ApplicationService.list(param).then(function(response){
         vm.applicationList=response.map(function (ap) {
           ap['idS'] = ap.id.toString()
           return ap;
         });
       },function(error){
          console.log(error);
        });

    }
    function getService(){
      serviceService.list().then(function(response){
         vm.serviceList=response.map(function(ser){
           ser['idS']= ser.id.toString();
           return ser;
         })
       },function(error){
          console.log(error);
        });

    }
    function fetchSupervisor(resources){
      return resources.map(function (resource, index) {
        var selfArray= resource.self.split('/');
        var name =  resource['firstName']
        if(resource['lastName']){
          name = name+" "+ resource['lastName'];
        }
        var contact = {
          userId:selfArray[selfArray.length-1],
          name: name,
          image: checkProfilePic({refURL:resource.self},selfArray[selfArray.length-1]),
          self:resource.self,
          resource:{'@name':name,refURL:resource.self}
        };
        contact._lowername = contact.name.toLowerCase();
        return contact;
      });
    }
    /**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(contact) {
        return (contact._lowername.indexOf(lowercaseQuery) != -1);
      };
    }

    function createFilterForQueue(query) {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(csq) {
        return (csq.lower_name.indexOf(lowercaseQuery) != -1);
      };
    }


    function iniIt(){
      vm.team = {resources:[],csqs:[],secondarySupervisors:[],primarySupervisor:undefined,applications:[],services:[]};

      teamService.list().then(function(respond){
        vm.teamList = respond.team;

      },function(error){
        console.log(error);
      });


    }
    if(MicroserviceFactory.APAdmin()){
      vm.authorizationRequires=false;
      iniIt();
    }else{
      vm.authorizationRequires=true;
    }

    function findTeam(id){
      var teamFound=$filter('filter')(vm.teamList,{id:id});
      if(teamFound)
        return teamFound[0].name;
    };


    $rootScope.$on('ERROR_',function(re){
      console.log(re);
      console.log('mum');
    });

    function create(event)
    {
      vm.createFormOr=undefined;
      vm.team = {resources:[],csqs:[],secondarySupervisors:[],primarySupervisor:undefined,applications:[],services:[],scriptFolder:'',promptFolder:''};
    };


    function selectedTeam(team){
     if(vm.teamForm){
        vm.teamForm.$setUntouched();
     }
      utilCustom.toasterLoading();
      vm.teamSelected = angular.copy(team);
      vm.createFormOr='edit';
      vm.team = {resources:[],csqs:[],secondarySupervisors:[],primarySupervisor:undefined,applications:[],services:[],scriptFolder:'',promptFolder:''};
      teamService.show({id:team.teamId}).then(function(response){
          utilCustom.hideToaster();
        if(response.resources){
          vm.team.resources = addImagesToResources(response.resources);

        }
        if(response.csqs){
          vm.team.csqs = changeObjectForQueue(response.csqs);
        }

        if(response.secondarySupervisors){
           vm.team.secondarySupervisors = response.secondarySupervisors.secondrySupervisor.map(function(sSupervisor){
               var selfArray=sSupervisor.refURL.split('/');
              return _.find(vm.supervisors,{userId:selfArray[selfArray.length-1]});
           })
          //= addImagesToSuperVisor(response.secondarySupervisors);


        }
        if(response.primarySupervisor){
          var selfArray= response.primarySupervisor.refURL.split('/');
          vm.team.primarySupervisor = {
            userId:selfArray[selfArray.length-1],
            name: response.primarySupervisor['@name'],
            image: checkProfilePic(response.primarySupervisor,selfArray[selfArray.length-1]),
            resource:response.primarySupervisor
          }
        };
        vm.team.teamname = response.teamname;
        vm.team.teamId=response.teamId;
        if(response.applications)
        vm.team.applications = response.applications;
        if(response.services)
        vm.team.services = response.services;
        vm.team.promptFolder = response.promptFolder;
        vm.team.scriptFolder=response.scriptFolder;
      },function(error){
          utilCustom.hideToaster();
        utilCustom.toaster($filter('translate')('data.loadingError') + $filter('translate')('team.team'));
      });

    }

    function addSupervisor(primary){
        vm.team.primarySupervisor ={};
    vm.team.primarySupervisor  = _.find(vm.supervisors,{userId:primary});

    }

    function resourceImages(resources){
      return resources.map(function (resource, index) {
        var name = resource['firstName'];
        if(resource['lastName']){
          name = name + " "+ resource['lastName'];
        }
        var contact = {
          userId:resource.userID,
          name: name,
          image: checkProfilePic({refURL:resource.self},resource.userID),
          self:resource.self,
          resource:{'@name':name,refURL:resource.self}
        };
        contact._lowername = contact.name.toLowerCase();
        return contact;
      });
    }


    function addImagesToResources(resources){
      return resources.resource.map(function (resource, index) {
        var userId = resource.refURL.split('/');
        userId = userId[userId.length-1];
        var contact = {
          userId:  userId,
          name: resource['@name'],
          image: checkProfilePic(resource,userId),
          resource:resource
        };
        contact._lowername = contact.name.toLowerCase();
        return contact;
      });
    }

    function checkProfilePic(resource,userId){
      var agent = _.find(vm.agentList,{userID:userId});
      var avatar = '/assets1/images/avatars/profile.jpg';
      if(agent){
        if(agent.profileExists)
          avatar=window.appBaseUrl+'/base/assets1/images/agents/'+angular.lowercase(userId)+'.jpg?timestamp='+new Date().getTime();
      }


      return avatar
    }
    function addImagesToSuperVisor(secondarySupervisors){
      return secondarySupervisors.secondrySupervisor.map(function (secondarySupervisor, index) {
        var selfArray = secondarySupervisor.refURL.split('/');
        var contact = {
          userId:selfArray[selfArray.length-1],
          name: secondarySupervisor['@name'],
          self:secondarySupervisor.refURL,
          image: checkProfilePic(secondarySupervisor,selfArray[selfArray.length-1]),
          resource:secondarySupervisor
        };
        contact._lowername = contact.name.toLowerCase();
        return contact;
      });
    }
    function changeObjectForQueue(csqs){
      return csqs.csq.map(function (csq, index) {
        var splitArray = csq.refURL.split('/');
        var contact = {
          id:Number(splitArray[splitArray.length-1]),
          name: csq['@name'],
          csq:csq
        };
        contact._lowername = contact.name.toLowerCase();
        return contact;
      });
    }
    function update(team){

         if(vm.teamForm.$invalid){
           if(vm.createFormOr=='edit')
          utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('team.team'));
           else
           utilCustom.toaster($filter('translate')('data.createError') +' ' + $filter('translate')('team.team'));
           return
         }

      vm.primarySupervisorRequire = false;
      vm.primarySupervisorOnlyOne=false;
      //var teamCreated = $rootScope._user;
      var updateTeam = angular.copy(team);
      if(!updateTeam.primarySupervisor){
        vm.primarySupervisorRequire = true;
        utilCustom.toaster($filter('translate')('team.supervisor_cannot_null'));
        return
      }else{
       var primarySupervisor = _.find(vm.supervisors,{userId:updateTeam.primarySupervisor.userId})
          if(!primarySupervisor.resource){
            primarySupervisor.push({resource:{'@name':primarySupervisor.name,'refURL':primarySupervisor.self}});
          }

      }
      var secondarySuperVisors= _.map(updateTeam.secondarySupervisors,'resource');
      var csqs=  _.map(updateTeam.csqs,'csq');
      var resources=  _.map(updateTeam.resources,'resource');
      var agentExist = false;
      var agents = '';
      angular.forEach(secondarySuperVisors,function(value,key){
        angular.forEach(primarySupervisor,function(val,ke){
          var v1 = value['refURL'].split('/');v1 = v1[v1.length-1];
          var v2 = val;
          if(v1===v2){
            agentExist = true;
            agents = agents +';'+ v1;
          }
        })
      });
      if(agentExist){
        utilCustom.toaster($filter('translate')('team.agent_in_team') );
        return
      }

      var params = {
        applications:updateTeam.applications,
        services:updateTeam.services,
        promptFolder:updateTeam.promptFolder,
        scriptFolder:updateTeam.scriptFolder,
        team:{teamname:updateTeam.teamname,
          primarySupervisor:primarySupervisor.resource,
          teamId:updateTeam.teamId,
          self:updateTeam.self
        },
        id:vm.createFormOr=='edit'?updateTeam.teamId:updateTeam.teamname
      };
      if(secondarySuperVisors.length>0){
        params.team['secondarySupervisors']={secondrySupervisor:secondarySuperVisors};
      }
      if(csqs.length>0){
        params.team['csqs']=  {csq:csqs};
      }
      if(resources.length>0){
        params.team['resources']={resource:resources}
      }
      utilCustom.toasterLoading();
      if(vm.createFormOr==='edit'){
        teamService.update(params).then(function(response){
          utilCustom.toaster($filter('translate')('team.team')+ ' ' + $filter('translate')('data.updated'));
          agentService.list().then(function(res){
            vm.agentList=res.resource;
            separateAgentFromMainList();
            $rootScope.agentList=res.resource
          } );

          var found = _.findIndex(vm.teamList,{teamId:response.teamId});
          if(found!=-1){
            vm.teamSelected = vm.teamList[found];
            checkTeamChangeForSupervisors(vm.teamSelected,response);

            vm.teamList[found]=response;
          }
        //  setTeamList();
        },function(error){
         if("404".equals(error.status)){
             utilCustom.toaster($filter('translate')('generic.notFound') +' ' + $filter('translate')('team.team'));
         }else{
             utilCustom.toaster($filter('translate')('data.updateError') +' ' + $filter('translate')('team.team'));
         }


        });
      }
      else{
        teamService.save(params).then(function(response){
          vm.createFormOr='edit';
          vm.team.teamId = response.teamId;
          vm.teamList.push(response);
        //  setTeamList();
          utilCustom.toaster($filter('translate')('team.team') +' ' +$filter('translate')('data.created'));

        },function(error){
            utilCustom.toaster($filter('translate')('data.createError') +' ' +$filter('translate')('team.team'));
        });
      }

    }
    function checkTeamChangeForSupervisors(oldTeam,newTeam){
      var supervisorsOld = [],supervisorsNew=[];
      if(oldTeam.secondarySupervisors)
      supervisorsOld.push(oldTeam.secondarySupervisors.secondrySupervisor);
      supervisorsOld.push(oldTeam.primarySupervisor);
      if(newTeam.secondarySupervisors)
      supervisorsNew.push(newTeam.secondarySupervisors.secondrySupervisor);
      supervisorsNew.push(newTeam.primarySupervisor);
      // console.log(supervisorsOld);
      // console.log(supervisorsNew);
    }
    function deleteTeam(team){

      utilCustom.toasterConfirm().then(function(response){
        if ( response == 'ok' ||  response) {
          utilCustom.toasterLoading();

          var params = {id:team.teamId};
          teamService.delete(params).then(function(responseT){
            utilCustom.toaster($filter('translate')('team.team') + $filter('translate')('data.deleted'));
            _.remove(vm.teamList,function(us){
              return us.teamId===team.teamId;
            })
          create();
          },function(error){
            console.log(error);
            utilCustom.toaster($filter('translate')('data.deleteError') + $filter('translate')('team.team'));
          });
        }else{
          utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
        }
      });



    }

    function setTeamList(){
      window.localStorage.setItem("teamList",JSON.stringify(vm.teamList))
    }

    function filterApplication(label)
    {
      if ( !vm.labelSearchAppText || vm.labelSearchAppText === '' )
      {
        return true;
      }

      return angular.lowercase(label.applicationName).indexOf(angular.lowercase(vm.labelSearchAppText)) >= 0;
    }
    function filterSupervisor(label)
    {
      if ( !vm.labelSearchSuperText || vm.labelSearchSuperText === '' )
      {
        return true;
      }

      return angular.lowercase(label._lowername).indexOf(angular.lowercase(vm.labelSearchSuperText)) >= 0;
    }
    function filterResource(label)
    {
      if ( !vm.labelSearchAgentText || vm.labelSearchAgentText === '' )
      {
        return true;
      }

      return angular.lowercase(label._lowername).indexOf(angular.lowercase(vm.labelSearchAgentText)) >= 0;
    }
    function filterCsq(label)
    {
      if ( !vm.labelSearchCsqText || vm.labelSearchCsqText === '' )
      {
        return true;
      }

      return angular.lowercase(label.lower_name).indexOf(angular.lowercase(vm.labelSearchCsqText)) >= 0;
    }
    function filterService(label)
    {
      if ( !vm.labelSearchText || vm.labelSearchText === '' )
      {
        return true;
      }

      return angular.lowercase(label.name).indexOf(angular.lowercase(vm.labelSearchText)) >= 0;
    }
    function queryArraySearch(query,objectList,type) {
      var results = query ? objectList.filter(createFilterFor(query, type)) : [];
      if (type == 'app') {
        for (var i = 0; i < vm.team.applications.length; i++) {

          for (var i2 = 0; i2 < results.length; i2++) {

            if (results[i2].idS.toLowerCase() == vm.team.applications[i].toLowerCase()) {

              results.splice(i2, 1);

            }

          }

        }
        return results;
      }
      else {

        for (var i = 0; i < vm.team.services.length; i++) {

          for (var i2 = 0; i2 < results.length; i2++) {

            if (results[i2].idS.toLowerCase() == vm.team.services[i].toLowerCase()) {

              results.splice(i2, 1);

            }

          }

        }
        return results;
      }
    }

    function createFilterFor(query,key)
    {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item)
      {
        if(key==undefined)
        return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
        else if(key=='app')
        return angular.lowercase(item.applicationName).indexOf(lowercaseQuery) >= 0;
        else
          return angular.lowercase(item).indexOf(lowercaseQuery) >= 0;
      };
    }
    function findApplicationName(id){

        id =Number(id);

    var name = _.result(_.find(vm.applicationList, {id:id.toString()}), 'applicationName');
      return name;
    }
    function findServiceName(id){

        id =Number(id);

    var fnd =_.find(vm.serviceList, {id:id});
      return  _.result(fnd,'name')+"("+ _.result(fnd,'dialNumber')+")";
    }
  }





})();
