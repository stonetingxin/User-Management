/**
 * Created by mumar on 3/3/2016.
 */
(function(){

  'use strict';

  angular
    .module('app.queue')
    .factory('queueService',['$resource','$q',queueService]);
  function queueService($resource,$q){
    var queues = $resource('../queue/',{id:'@id'},
      {
      getList:{
        method:'GET',
        url:window.appBaseUrl+"/tam/queue/index/",
        isArray:true
      },
      save:{
        method:'POST',
        url:window.appBaseUrl+'/tam/queue/save',params:{
          queue:'@queue',
          id:'@id'
        }
      },
        update:{
          method:'PUT',
          url:window.appBaseUrl+'/tam/queue/update',params:{
            queue:'@queue',
            id:'@id'
          }
        },
        delete:{
          "method":'DELETE',
          url:window.appBaseUrl+'/tam/queue/delete',params:{
          id:'@id'
        }
        }
    });
    return{
      'get_api':queues,
      'list':function(params){
        var defered = $q.defer();
        queues.getList(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'update':function(params){
        var defered = $q.defer();

        queues.update(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'delete':function(id){
        var defered = $q.defer();
        queues.delete(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      }

    }

  }




})();
