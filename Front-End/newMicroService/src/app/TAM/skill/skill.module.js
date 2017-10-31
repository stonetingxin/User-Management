/**
 * Created by mumar on 1/7/2016.
 */

(function ()
{
  'use strict';

  angular
    .module('app.skill', [])
    .config(config);

  /** @ngInject */
  function config($stateProvider, msNavigationServiceProvider)
  {
    // State
    $stateProvider.state('app.systemSetting.skill', {
      url      : '/skill',
      views    : {
        'system': {
          templateUrl: 'app/TAM/skill/list.html',
          controller : 'SkillController as vm'
        }, data:{
          name:'skill'
        }
      },

      bodyClass: 'todo'
    });



    // Navigation
    /*msNavigationServiceProvider.saveItem('skill', {
      title : 'Skills',
      icon  : 'icon-account-multiple',
      state:'app.skill'
    });*/

  }
    angular.module('fuse').requires.push('app.skill');
})();
