/**
 * Created by mumar on 2/2/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.trigger')
    .factory('triggerService',TriggerService);

  function TriggerService($resource,$q,$http){


    var trigger = $resource(window.appBaseUrl+'/trigger',{id:'@id'},{

      list:{
        "method":"GET",
        url:window.appBaseUrl+'/tam/trigger/',
        isArray:false
      },
      getTrigger:{
        method:"GET",
        url:window.appBaseUrl+'/tam/trigger/get',
        params:{
          id:'@id'
        }
      },
      update:{
        method:'PUT',
        url:window.appBaseUrl+'/tam/trigger/update',
        params:{
          trigger:'@trigger',
          triggerName:'@triggerName'
        }
      },
      save:{
        method:'POST',
        url:window.appBaseUrl+'/tam/trigger/save',
        params:{
          trigger:'@trigger',
          directoryNumber:'@directoryNumber'
        }
      },
      delete:{
        method:'DELETE',
        url:window.appBaseUrl+'/tam/trigger/delete',
        params:{
          id:'@id'
        }
      }



    });
    return{
      'list':function(params){
        var defered = $q.defer();
        trigger.list(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getTrigger':function(params){
        var defered = $q.defer();
        trigger.getTrigger(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'update':function(params){
        var defered = $q.defer();
        trigger.update(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'save':function(params){
        var defered = $q.defer();
        trigger.save(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'delete':function(params){
        var defered = $q.defer();
        trigger.delete(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      }
    }
  }


})();
