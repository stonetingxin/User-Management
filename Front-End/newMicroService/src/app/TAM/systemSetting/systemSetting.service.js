/**
 * Created by mumar on 9/29/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.systemSetting')
    .factory('systemSettingService',['$resource','$q',systemSettingService]);

  function systemSettingService($resource,$q){
    var applicationSetting = $resource('../applicationSetting',{id:'@id'},
      {
        getSetting:{
          method:'GET',
          url:window.appBaseUrl+"/applicationSetting/index/"
        }
      })
    return{
      getSetting : function(){
        var defered = $q.defer();
        applicationSetting.getSetting(function(data){
          defered.resolve(data);
        },function(error){
          defered.reject(error);
        });
        return defered.promise;
      }
    }


  }

})();
