/**
 * Created by mumar on 2/17/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.trigger',[])
    .config(config);

  function config($stateProvider,msNavigationServiceProvider){
    // State
    $stateProvider.state('app.trigger', {
      url      : '/trigger',
      views    : {
        'content@app': {
          templateUrl: 'app/TAM/trigger/trigger.html',
          controller : 'TriggerController as vm'
        }
      },
      resolve: {

      },
      bodyClass: 'calendar'
    });


    // Navigation


  }
})();
