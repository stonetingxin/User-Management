(function ()
{
    'use strict';

    angular
        .module('app.script', ['ngAudio','ngFileUpload'])
        .config(config);

    /** @ngInject */
    function config($stateProvider, $translatePartialLoaderProvider, msNavigationServiceProvider)
    {
        // State
        $stateProvider
          .state('app.scripts', {
            url      : '/script/:id',
            views    : {
                'content@app': {
                    templateUrl: 'app/TAM/script/script.html',
                    controller : 'ScriptController as vm'
                }
            },

            bodyClass: 'file-manager'
        })
    .state('app.scripts.script', {
      url    : '/:id',
      views  : {

        'content@app': {
          templateUrl: 'app/TAM/script/script.html',
          controller : 'ScriptController as vm'
        }
      }
    });

        // Translation
    //    $translatePartialLoaderProvider.addPart('app/main/apps/file-manager');

  //    if(window.userRole==='admin'){
        // Navigation
        msNavigationServiceProvider.saveItem('TAM.scripts', {
          title : 'file.scripts',
          icon  : 'icon-folder',
          state : 'app.scripts({id:""})',
          weight: 1,
            backendController: "script"
        });
    //  }

    }
    angular.module('fuse').requires.push('app.script');

})();
