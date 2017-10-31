/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.skill')
    .factory('skillService',['$resource','$rootScope','$q',skillService]);
  function skillService($resource,$rootScope,$q){
    var skills = $resource('../skill/',{id : '@id' },
      {
        update:{ method:'PUT',
          url:window.appBaseUrl+'/tam/skill/update/',
          params:{
            id:'@id',
            skill:'@skill'
          }

        },

        list:{
          method:'GET',
          url:window.appBaseUrl+'/tam/skill/index/',
          isArray:false
        },
        show:{
          method:'GET',
          url:window.appBaseUrl+'/tam/skill/show/'
          ,params:{
            id:'@id'
          }
        },

        delete:{
          method:'DELETE',
          url:window.appBaseUrl+'/tam/skill/delete/'

        },
        save:{
          method:'POST',
          url:window.appBaseUrl+'/tam/skill/save',
          params:{
            skill:'@skill'
          }
        }



      })
    return{
      'update' : function(skill){
        var defered  = $q.defer();
        skills.update(skill,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'list':  function(params){
        var defered  = $q.defer();
        skills.list(params,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      get_api:skills,

      'show':function(id){
        var defered  = $q.defer();
        skills.show(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      } ,

      'delete':function (id){
        var defered  = $q.defer();
        skills.delete(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var skillList = window.localStorage.getItem.skillList;
        if(skillList==undefined){
          defered.resolve(this.list(params));
        }else{
          defered.resolve(JSON.parse(skillList));

        }
        return defered.promise;
      }

    }
  }

})();
