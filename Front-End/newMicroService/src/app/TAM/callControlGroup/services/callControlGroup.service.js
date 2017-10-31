/**
 * Created by mumar on 3/3/2016.
 */
(function(){

  'use strict';

  angular
    .module('app.callControlGroup')
    .factory('callControlGroupService',['$resource','$q',callControlGroupService]);
  function callControlGroupService($resource,$q){
    var callControlGroups = $resource('../callControlGroup/',{id:'@id'},
      {
      getList:{
        method:'GET',
        url:window.appBaseUrl+"/tam/callControlGroup/index/",
        isArray:false
      }

    })
    return{
      'get_api':callControlGroups,
      'list':function(params){
        var defered = $q.defer();
        callControlGroups.getList(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      }
    }

  }




})();
