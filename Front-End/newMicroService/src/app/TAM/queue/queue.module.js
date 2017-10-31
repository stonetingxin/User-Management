/**
 * Created by mumar on 1/11/2016.
 */

(function ()
{
  'use strict';

  angular
    .module('app.queue', [])
    .config(config)
  .run(run);
  /** @ngInject */
  function config($stateProvider, msNavigationServiceProvider)
  {
    // State
    $stateProvider.state('app.queue', {
      url      : '/queue',
      views    : {
        'content@app': {
          templateUrl: 'app/TAM/queue/list.html',
          controller : 'QueueController as vm'
        }
      },
      resolve  : {

      }/*,
       bodyClass: 'todo'*/
    });

    $stateProvider.state('app.queue.detail', {
      url      : '/:id',
      views    : {
        'detailsContent@app': {
          templateUrl: 'app/TAM/queue/list.html',
          controller : 'QueueController as vm'
        }
      },
      resolve  : {
        QueueData: function (apiResolver)
        {
          return apiResolver.resolve('queue.list@get');
        },
        AgentData: function (apiResolver)
        {
          return apiResolver.resolve('agent.list@get');
        }
      }/*,
       bodyClass: 'todo'*/
    });

    // Navigation
    msNavigationServiceProvider.saveItem('TAM.queue', {
      title : 'csq.csqs',
      icon  : 'icon-trello',
      state:'app.queue',
      backendController: "queue"
    });
  }
  function run(editableThemes)
  {
    /**
     * Inline Edit Configuration
     * @type {string}
     */
    editableThemes.default.submitTpl = '<md-button class="md-icon-button" type="submit" aria-label="save"><md-icon md-font-icon="icon-checkbox-marked-circle" class="md-accent-fg md-hue-1"></md-icon></md-button>';
    editableThemes.default.cancelTpl = '<md-button class="md-icon-button" ng-click="$form.$cancel()" aria-label="cancel"><md-icon md-font-icon="icon-close-circle" class="icon-cancel"></md-icon></md-button>';
  }
    angular.module('fuse').requires.push('app.queue');

})();
