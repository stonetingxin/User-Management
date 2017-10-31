/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.service')
    .factory('serviceService',['$resource','$q',ServiceService]);
  function ServiceService($resource,$q){
    // window.appBaseUrl = 'http://192.168.200.47:9092/AdminPanel';
    var services =$resource(window.appBaseUrl+'/service/',{id:'@id'},
      {
        update:{ method:'PUT',
          url:window.appBaseUrl+'/tam/service/update/',
          params:{
            id:'@id'
          }

        },

        getList:{
          method:'GET',
          url:window.appBaseUrl+"/tam/service",
          params:{
            team:'@team'
          },
          isArray:true
        },
        show:{
          method:'GET',
          url:window.appBaseUrl+'/tam/service/show/'
          ,params:{
            id:'@id',
            name:'@name',
            dialNumber:'@dialNumber',
            description:'@description',
            updatedBy:'@updatedBy'
          }
        },

        delete:{
          method:'DELETE',
          url:window.appBaseUrl+'/tam/service/delete/'

        },
        save:{
          method:'POST',
          url:window.appBaseUrl+'/tam/service/save',
          params:{
            name:'@name',
            dialNumber:'@dialNumber',
            description:'@description'
          }
        }



      })
    return{
      'update' : function(service){
        var defered  = $q.defer();
        services.update(service,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'list':  function(params){
        var defered  = $q.defer();
        services.getList(params,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
       get_api:services,
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var serviceList = window.localStorage.getItem("servicesList");
        if(serviceList=="undefined" || serviceList=="null" || serviceList==null   ){
          defered.resolve(this.list(params));
        }else{
          defered.resolve(JSON.parse(serviceList));

        }
        return defered.promise;
      },
      'show':function(id){
        var defered  = $q.defer();
        services.show(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      } ,

      'delete':function (id){
        var defered  = $q.defer();
        services.delete(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      }
    }
  }

})();
