/**
 * Created by mumar on 1/7/2016.
 */

(function ()
{
  'use strict';

  angular
    .module('app.group', [])
    .config(config);

  /** @ngInject */
  function config($stateProvider, msNavigationServiceProvider)
  {
    // State
    $stateProvider.state('app.systemSetting.group', {
      url      : '/group',
      views    : {
        'system': {
          templateUrl: 'app/TAM/group/list.html',
          controller : 'GroupController as vm'
        },
        data:{
          name:'group'
        }
      }

    });



    // Navigation
   /* msNavigationServiceProvider.saveItem('group', {
      title : 'Groups',
      icon  : 'icon-account-multiple',
      state:'app.group'
    });*/

  }
    angular.module('fuse').requires.push('app.group');
})();
