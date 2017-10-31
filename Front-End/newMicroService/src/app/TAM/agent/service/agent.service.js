/**
 * Created by mumar on 3/3/2016.
 */
(function(){

  'use strict';

  angular
    .module('app.agent')
    .factory('agentService',['$resource','$q','Upload',agentService]);
  function agentService($resource,$q,Upload){
    var agents = $resource('../agent/',{id:'@id'},
      {
        getList:{
          method:'GET',
          url:window.appBaseUrl+"/tam/agent/index/",
          isArray:false
        },
        save:{
          method:'POST',
          url:window.appBaseUrl+'/tam/agent/save',params:{
            name:'@name',
            placeHolder:'@placeHolder',
            description:'@description',
            announcements:'@announcements',
            regions:'@regions',
            event:'@event'
          }
        },
        update:{
          method:'PUT',
          url:window.appBaseUrl+'/tam/agent/update',params:{
            agent:'@agent',
            id:'@id'
          }
        },
        removeProfilePic:{
          "method":'POST',
          url:window.appBaseUrl+'/tam/agent/deleteProfilePic',params:{
            agentId:'@agentId'
          }
        },
        getProfilePic:{
          method:'POST',
          url:window.appBaseUrl+'/tam/agent/getProfilePic',params:{
            id:'@id'
          }
        },
        getAgent:{
        method:'GET',
        url:window.appBaseUrl+'/tam/agent/getAgent',params:{
          id:'@id'
        }
      },
        getAgentTeam:{
          method:'GET',
          url:window.appBaseUrl+'/tam/agent/getAgentTeam',params:{
            id:'@id'
          }
          ,isArray:true
        }
      });
    return{
      'get_api':agents,
      'list':function(params){
        var defered = $q.defer();
        agents.getList(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'update':function(params){
        var defered = $q.defer();
        agents.update(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getProfilePic':function(params){
        var defered = $q.defer();
        agents.getProfilePic(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'removeProfilePic':function(id){
        var defered = $q.defer();
        agents.removeProfilePic(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateProfilePic':function(params){
        var up =  Upload.upload({
          url:window.appBaseUrl+"/user/updateProfilePic",
          data: params
        });

        return up;
      },
      'getAgent':function(id){
        var defered = $q.defer();
        agents.getAgent(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getAgentTeam':function(id){
        var defered = $q.defer();
        agents.getAgentTeam(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      }
    }

  }




})();
