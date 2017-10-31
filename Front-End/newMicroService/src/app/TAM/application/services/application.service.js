/**
 * Created by mumar on 2/2/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.application')
    .factory('ApplicationService',ApplicationService);

  function ApplicationService($resource,$q,$http){


    var application = $resource(window.appBaseUrl+'/application',{id:'@id'},{

      list:{
        "method":"GET",
        url:window.appBaseUrl+'/tam/application/',
        isArray:true
      },
      getApplication:{
        method:"GET",
        url:window.appBaseUrl+'/tam/application/get',
        params:{
          id:'@id'
        }
      },
      update:{
        method:'PUT',
        url:window.appBaseUrl+'/tam/application/update',
        params:{
          application:'@application',
          applicationName:'@applicationName',
          team:'@team'
        }
      },
      save:{
        method:'POST',
        url:window.appBaseUrl+'/tam/application/save',
        params:{
          application:'@application',
          applicationName:'@applicationName',
          directoryNumber:'@directoryNumber',
          team:'@team'
        }
      },
      delete:{
        method:'DELETE',
        url:window.appBaseUrl+'/tam/application/delete',
        params:{
          id:'@id'
        }
      }



    });
    return{
      'list':function(params){
        var defered = $q.defer();
        application.list(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getApplication':function(params){
        var defered = $q.defer();
        application.getApplication(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'update':function(params){
        var defered = $q.defer();
        application.update(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'save':function(params){
        var defered = $q.defer();
        application.save(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'delete':function(params){
        var defered = $q.defer();
        application.delete(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      }
    }
  }


})();
