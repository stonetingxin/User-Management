/**
 * Created by mumar on 1/7/2016.
 */

(function() {
    'use strict';

    angular
        .module('app.service', [])
        .config(config);

    /** @ngInject */
    function config($stateProvider, msNavigationServiceProvider) {
        // State
        $stateProvider.state('app.systemSetting.service', {
            url: '/directoryNumber',
            views: {
                'system': {
                    templateUrl: 'app/TAM/services/list.html',
                    controller: 'ServiceController as vm'
                },
                data: {
                    name: 'service'
                }
            },

            bodyClass: 'todo'
        });



        // Navigation
        /* msNavigationServiceProvider.saveItem('service', {
           title : 'Services',
           icon  : 'icon-account-multiple',
           state:'app.service'
         });*/

    }
    angular.module('fuse').requires.push('app.service');
})();