/**
 * Created by mumar on 2/17/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.application',[])
    .config(config);

  function config($stateProvider,msNavigationServiceProvider){
    // State
    $stateProvider.state('app.application', {
      url      : '/application',
      views    : {
        'content@app': {
          templateUrl: 'app/TAM/application/application.html',
          controller : 'ApplicationController as vm'
        }
      },
      resolve: {

      }/*,
      bodyClass: 'calendar'*/
    });


    // Navigation
    msNavigationServiceProvider.saveItem('TAM.application', {
      title : 'application.applications',
      icon  : 'icon-apps',
      state : 'app.application',
      weight: 1,
        backendController: 'application'
    });

  }
    angular.module('fuse').requires.push('app.application');
})();
