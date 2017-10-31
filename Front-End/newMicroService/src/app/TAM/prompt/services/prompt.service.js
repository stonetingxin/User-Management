/**
 * Created by mumar on 2/2/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.prompt')
    .factory('PromptService',['$resource','$q','$rootScope','Upload',PromptService]);

  function PromptService($resource,$q,$rootScope,Upload){
    var prompts = $resource($rootScope.appBaseUrl+'/prompt',{id:'@id'},{
      list:{
         method:'GET',
        url:window.appBaseUrl+"/tam/prompt/",
        params:{
          path:'@path'

        }
      },
      createFolder:{
        "method":'POST',
         url:window.appBaseUrl+"/tam/prompt/createFolder",
        params:{
          folder:'@folder',
          team:'@team'
        }
      },
      delete:{
        "method":'DELETE',
        url:window.appBaseUrl+"/tam/prompt/delete",
        params:{
          folder:'@folder',
          team:'@team'
        }
      },
      getPromptsOrFolder:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/prompt/getPromptsOrFolder",
        params:{
          folder:'@folder',
          team:'@team'
        }
      } ,
      updateFolder:{
        "method":'PUT',
        url:window.appBaseUrl+"/tam/prompt/updateFolder",
        params:{
          folder:'@folder',
          folderPath:'@folderPath', newPath:'@newPath',
          team:'@team'
        }
      },
      updateFile:{
        "method":'PUT',
        url:window.appBaseUrl+"/tam/prompt/update",
        params:{
          promptPath:'@promptPath',
          prompt:'@prompt',
          newPath:'@newPath',
          team:'@team'
        }
      },
      save:{
        "method":'POST',
        url: window.appBaseUrl +"/tam/prompt/save",
        headers: { 'Content-Type': undefined },
        params:{
          prompt:'@prompt',
          file:'@file',
          team:'@team'
        }
      },
      download:{
        "method":'GET',
        url:window.appBaseUrl+"/tam/prompt/download",
        params:{
          path:'@path',
          team:'@team'
        } , isArray:true
      },
      getAllPrompts:{
        "method":'POST',
        url:window.appBaseUrl+"/tam/prompt/getAllPrompts",
        params:{
          team:'@team'
        },
       isArray:true
      }

    });
    return{
      'list':function(id){
        var defered = $q.defer();
        prompts.list(id,function(data){

          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'createFolder':function(folder){
        var defered = $q.defer();
        prompts.createFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'delete':function(folder){
        var defered = $q.defer();
        prompts.delete(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateFolder':function(folder){
        var defered = $q.defer();
        prompts.updateFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'updateFile':function(folder){
        var defered = $q.defer();
        prompts.updateFile(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getPromptsOrFolder':function(folder){
        var defered = $q.defer();
        prompts.getPromptsOrFolder(folder,function(data){
          defered.resolve(data);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'createFile':function(folder){
       var up =  Upload.upload({
          url:window.appBaseUrl+"/tam/prompt/save",
          data: {team:folder.team,file: folder.file, 'prompt':JSON.stringify(folder.prompt)}
        });

        return up;
      },
      'download':function(folder){
        var defered = $q.defer();
        prompts.download(folder,function(path){
          defered.resolve(path);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getAllPrompts':function(params){
        var defered = $q.defer();
        prompts.getAllPrompts(params,function(response){
          window.localStorage.setItem("promptList",JSON.stringify(response));
          defered.resolve(response);
        },function(er){
          defered.reject(er);
        });
        return defered.promise;
      },
      'getListFromCache':function(params){
        var defered  = $q.defer();
        var promptList = window.localStorage.promptList;
        if(promptList=="undefined" || promptList=="null" || promptList==null   ){
          defered.resolve(this.getAllPrompts());
        }else{
          defered.resolve(JSON.parse(promptList));

        }
        return defered.promise;
      },
      'setLocalPrompt':function(filePath,type,reName,module){
        var fP ='';
        if(module=='promptList'){
           fP = filePath.split('/');
          fP = fP.slice(2,fP.length);
          fP  = fP.join('/');
        }else{
          fP = filePath.split('/');
          fP = fP.slice(1,fP.length);
          fP  = fP.join('/');
        }
        var allPrompts = window.localStorage.getItem(module);
        if(allPrompts){
          allPrompts = JSON.parse(allPrompts);
          if(type=='add')
          allPrompts.push(fP);
          else if(type=='delete'){
            _.remove(allPrompts,function(fl){
              return fl===fP
            });
          }
          else if(type=='update'){
            _.remove(allPrompts,function(fl){
              return fl===fP
            });
            if(module=='promptList'){
              fP = reName.split('/');
              fP = fP.slice(2,fP.length);
              fP  = fP.join('/');
            }else{
              fP = reName.split('/');
              fP = fP.slice(1,fP.length);
              fP  = fP.join('/');
            }
            allPrompts.push(fP);
          }

          window.localStorage.setItem(module,JSON.stringify(allPrompts));
        }
      },
      'recallPrompt':function(){
        var defered  = $q.defer();
        window.localStorage.removeItem("promptList");
        defered.resolve(this.getListFromCache());
        return defered.promise;
      }
    }
  }


})();
