/**
 * Created by mumar on 1/7/2016.
 */

(function ()
{
  'use strict';

  angular
    .module('app.team', [])
    .config(config);

  /** @ngInject */
  function config($stateProvider, msNavigationServiceProvider)
  {
    // State
    $stateProvider.state('app.team', {
      url      : '/team',
      views    : {
        'content@app': {
          templateUrl: 'app/TAM/team/list.html',
          controller : 'TeamController as vm'
        }
      },

      bodyClass: 'todo'
    });


   // if(window.localStorage.getItem('userRole')==='admin'){
      // Navigation
      msNavigationServiceProvider.saveItem('TAM.team', {
        title : 'team.teams',
        icon  : 'icon-account-multiple',
        state:'app.team',
          backendController: "team"
      });
   // }


  }
    angular.module('fuse').requires.push('app.team');
})();
