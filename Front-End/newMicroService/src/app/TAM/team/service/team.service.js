/**
 * Created by mumar on 3/9/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.team')
    .factory('teamService',['$resource','$rootScope','$q',teamService]);
  function teamService($resource,$rootScope,$q){
    var teams = $resource('../team/',{id : '@id' },
      {
        update:{ method:'PUT',
          url:window.appBaseUrl+'/tam/team/update/',
          params:{
            id:'@id',
            team:'@team',
            applications:'@applications',
            services:'@services',
            promptFolder:'@promptFolder',
            scriptFolder:'@scriptFolder'
          }

        },
       getLocalTeam:{
         method:'GET',
         url:window.appBaseUrl+'/tam/team/getLocalTeam/',
         params:{
           id:'@id'
         }
       },
        list:{
          method:'GET',
          url:window.appBaseUrl+'/tam/team/index/',
          isArray:false
        },
        show:{
          method:'GET',
          url:window.appBaseUrl+'/tam/team/getTeam/'
          ,params:{
            id:'@id'
          }
        },

        delete:{
          method:'DELETE',
          url:window.appBaseUrl+'/tam/team/delete/'

        },
        save:{
          method:'POST',
          url:window.appBaseUrl+'/tam/team/save',
          params:{
            team:'@team',
            applications:'@applications',
            services:'@services',
            promptFolder:'@promptFolder',
              scriptFolder:'@scriptFolder'

          }
        }



      })
    return{
      'update' : function(team){
        var defered  = $q.defer();
        teams.update(team,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'save' : function(team){
        var defered  = $q.defer();
        teams.save(team,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'list':  function(params){
        var defered  = $q.defer();
        teams.list(params,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      get_api:teams,

      'show':function(id){
        var defered  = $q.defer();
        teams.show(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      } ,
      'getLocalTeam':function(params){
        var defered  = $q.defer();
        teams.getLocalTeam(params,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      } ,

      'delete':function (id){
        var defered  = $q.defer();
        teams.delete(id,function(response){ defered.resolve(response); },
          function(error)
          { defered.reject(error);});
        return defered.promise;
      },
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var teamList = window.localStorage.getItem("teamList");
        if(teamList!=undefined){
          defered.resolve(JSON.parse(teamList));
        }else{
          defered.resolve(this.list(params));

        }
        return defered.promise;
      }

    }
  }

})();
