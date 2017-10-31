/**
 * Created by mumar on 2/2/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.script')
    .factory('ScriptService',['$resource','$q','$rootScope','Upload',ScriptService]);

  function ScriptService($resource,$q,$rootScope,Upload){
    var scripts = $resource($rootScope.appBaseUrl+'/script',{id:'@id'},{
      list:{
         method:'GET',
        url:window.appBaseUrl+"/tam/script/",
        params:{
          path:'@path'

        }
      },
      createFolder:{
        "method":'POST',
         url:window.appBaseUrl+"/tam/script/createFolder",
        params:{
          folder:'@folder',
          team:'@team'
        }

      },
      updateVariables:{
        "method":'PUT',
        url:window.appBaseUrl+"/tam/script/updateVariables",
        params:{
          name:'@name',
          variables:'@variables',
          team:'@team'
        }
      },
      delete:{
        "method":'DELETE',
        url:window.appBaseUrl+"/tam/script/delete",
        params:{
          folder:'@folder',
          team:'@team'
        }
      },
      getScriptsOrFolder:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/script/getScriptsOrFolder",
        params:{
          folder:'@folder',
          team:'@team'
        }
      } ,
      updateFolder:{
        "method":'PUT',
        url:window.appBaseUrl+"/tam/script/updateFolder",
        params:{
          folder:'@folder',
          folderPath:'@folderPath', newPath:'@newPath',
          team:'@team'
        }
      },
      updateFile:{
        "method":'PUT',
        url:window.appBaseUrl+"/tam/script/update",
        params:{
          scriptPath:'@scriptPath',
          script:'@script',
          newPath:'@newPath',
          team:'@team'
        }
      },
      save:{
        "method":'POST',
        url:window.appBaseUrl+"/tam/script/save",
        headers: { 'Content-Type': undefined },
        params:{
          script:'@script',
          file:'@file',
          team:'@team'
        }
      },
      getScriptVariables:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/script/getScriptVariables",
        params:{
          name:'@name',
          team:'@team'
        }
      },
      download:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/script/download",
        params:{
          path:'@path',
          team:'@team'
        } , isArray:true
      },
      getAllScripts:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/script/getAllScripts",
        params:{
          team:'@team'
        },

        isArray:true
      }

    });
    return{
      'list':function(id){
        var defered = $q.defer();
        scripts.list(id,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'createFolder':function(folder){
        var defered = $q.defer();
        scripts.createFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'delete':function(folder){
        var defered = $q.defer();
        scripts.delete(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateFolder':function(folder){
        var defered = $q.defer();
        scripts.updateFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateVariables':function(params){
        var defered = $q.defer();
        scripts.updateVariables(params,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateFile':function(folder){
        var defered = $q.defer();
        scripts.updateFile(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getScriptVariables':function(folder){
        var defered = $q.defer();
        scripts.getScriptVariables(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getScriptsOrFolder':function(folder){
        var defered = $q.defer();
        scripts.getScriptsOrFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'createFile':function(folder){
       var up =  Upload.upload({
          url:window.appBaseUrl+"/tam/script/save",
          data: {team:folder.team,file: folder.file, 'script':JSON.stringify(folder.script)}
        });

        return up;
      },
      'download':function(folder){
        var defered = $q.defer();
        scripts.download(folder,function(path){
          defered.resolve(path);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getAllScripts':function(params){
        var defered = $q.defer();
        scripts.getAllScripts(params,function(response){
          window.localStorage.setItem("scriptList",JSON.stringify(response));
          defered.resolve(response);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var scriptList = window.localStorage.scriptList;
        if(scriptList=="undefined" || scriptList=="null" || scriptList==null   ){
          defered.resolve(this.getAllScripts());
        }else{
          defered.resolve(JSON.parse(scriptList));

        }
        return defered.promise;
      },
      'recallScript':function(){
        var defered  = $q.defer();
        window.localStorage.removeItem("scriptList");
        defered.resolve(this.getListFromCache());
        return defered.promise;
      }
    }
  }


})();
