(function ()
{
    'use strict';

    angular
        .module('app.prompt', ['ngAudio','ngFileUpload'])
        .config(config);

    /** @ngInject */
    function config($stateProvider, $translatePartialLoaderProvider, msNavigationServiceProvider)
    {
        // State
        $stateProvider
          .state('app.prompts', {
            url      : '/prompt/:id',
            views    : {
                'content@app': {
                    templateUrl: 'app/TAM/prompt/prompt.html',
                    controller : 'PromptController as vm'
                }
            },

            bodyClass: 'file-manager'
        })
    .state('app.prompts.prompt', {
      url    : '/:id',
      views  : {

        'content@app': {
          templateUrl: 'app/TAM/prompt/script.html',
          controller : 'PromptController as vm'
        }
      }
    });

        // Translation
    //    $translatePartialLoaderProvider.addPart('app/main/apps/file-manager');

        // Navigation
        msNavigationServiceProvider.saveItem('TAM.prompts', {
            title : 'file.prompts',
            icon  : 'icon-music-box',
            state : 'app.prompts({id:""})',
            weight: 1,
            backendController: "prompt"
        });
    }
    angular.module('fuse').requires.push('app.prompt');
})();
