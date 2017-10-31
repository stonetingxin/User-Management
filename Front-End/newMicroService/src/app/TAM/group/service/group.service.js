/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.group')
    .factory('groupService',['$resource','$rootScope','$q',groupService]);
  function groupService($resource,$rootScope,$q){
    var groups = $resource('../group/',{id : '@id' },
      {
        update:{ method:'PUT',
          url:window.appBaseUrl+'/tam/group/update/',
          params:{
            id:'@id',
            group:'@group'
          }

        },

        list:{
          method:'GET',
          url:window.appBaseUrl+'/tam/group/index/',
          isArray:false
        },
        show:{
          method:'GET',
          url:window.appBaseUrl+'/tam/group/show/'
          ,params:{
            id:'@id'
          }
        },

        delete:{
          method:'DELETE',
          url:window.appBaseUrl+'/tam/group/delete/'

        },
        save:{
          method:'POST',
          url:window.appBaseUrl+'/tam/group/save',params:{
            group:'@group'
          }
        }



      })
    return{
      'update' : function(group){
        var defered  = $q.defer();
        groups.update(group,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'list':  function(params){
        var defered  = $q.defer();
        groups.list(params,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      get_api:groups,

      'show':function(id){
        var defered  = $q.defer();
        groups.show(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      } ,

      'delete':function (id){
        var defered  = $q.defer();
        groups.delete(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var groupList = window.localStorage.getItem("groupList");
        if(groupList=="undefined" || groupList=="null" || groupList==null   ){
          defered.resolve(this.list(params));
        }else{
          defered.resolve(JSON.parse(groupList));

        }
        return defered.promise;
      }
    }
  }

})();
