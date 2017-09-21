(function ()
{
    'use strict';

    angular
        .module('app.users',
            [
                // 3rd Party Dependencies
                'xeditable'
            ]
        )
        .config(config);

    /** @ngInject */
    function config($stateProvider, msNavigationServiceProvider)
    {

        $stateProvider.state('app.users', {
            url    : '/users',
            views  : {
                'content@app': {
                    templateUrl: 'app/adminPanel/user/users.html',
                    controller : 'UsersController as vm'
                }
            }

        });

        // Translation

        // Navigation
        msNavigationServiceProvider.saveItem('apps.users', {
            title : 'Users',
            icon  : 'icon-account-circle',
            state : 'app.users',
            weight: 10
        });

    }

})();
