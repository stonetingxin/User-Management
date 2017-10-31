/**
 * Created by mumar on 1/7/2016.
 */

(function() {
    'use strict';

    angular
        .module('app.agent', ['ngFileUpload'])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msNavigationServiceProvider, $translatePartialLoaderProvider) {
        // State
        $stateProvider.state('app.agent', {
            url: '/agent',
            views: {
                'content@app': {
                    templateUrl: 'app/TAM/agent/list.html',
                    controller: 'AgentController as vm'
                }
            },
            resolve: {

            }
            /*,
                  bodyClass: 'todo'*/
        });
        $stateProvider.state('app.agent.details', {
            url: '/:id',
            views: {
                'detailsContent@app': {
                    templateUrl: 'app/TAM/agent/list.html',
                    controller: 'AgentController as vm'
                }
            },
            resolve: {

            }
            /*,
                   bodyClass: 'todo'*/
        });


        // Navigation
        msNavigationServiceProvider.saveItem('TAM.agent', {
          title : 'agent.agents',
          icon  : 'icon-headset',
          state:'app.agent',
          backendController:"agent"
        });

        // Translation
        $translatePartialLoaderProvider.addPart('/translations/Base/i18n');
    }
    angular.module('fuse').requires.push('app.agent');
})();
