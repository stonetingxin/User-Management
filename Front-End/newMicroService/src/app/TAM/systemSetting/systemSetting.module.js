/**
 * Created by mumar on 3/31/2016.
 */
(function ()
{
  'use strict';

  angular
    .module('app.systemSetting', [])
    .config(config);

  /** @ngInject */
  function config($stateProvider,msNavigationServiceProvider){
    // State
    $stateProvider.state('app.systemSetting', {
      url      : '/systemSetting',
      views    : {
        'content@app': {
          templateUrl: 'app/TAM/systemSetting/systemSetting.html',
          controller:'systemSettingController as vm'
        }
      },
      resolve: {

      }

    });


    // Navigation
    msNavigationServiceProvider.saveItem('TAM.systemSetting', {
      title : 'systemSetting.systemSettings',
      icon  : 'icon-cog',
      state : 'app.systemSetting',
      weight: 1,
        backendController: "systemSetting"
    });

  }
    angular.module('fuse').requires.push('app.systemSetting');
})();
