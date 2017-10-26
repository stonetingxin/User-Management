(function ()
{
    'use strict';

    angular
        .module('app.users',
            [
                // 3rd Party Dependencies
                'xeditable',
              'ngFileUpload'
            ]
        )
        .config(config)
        .directive('customChip2', function(){
            return {
                restrict: 'A',
                link: function(scope, elem, attrs) {
                    var mdChip = elem.parent().parent();
                    mdChip.addClass('remPadding');
                }
            }
        });

    /** @ngInject */
    function config($stateProvider, msNavigationServiceProvider)
    {

        $stateProvider.state('app.users', {
            url    : '/users',
            views  : {
                'content@app': {
                    templateUrl: 'app/user/users.html',
                    controller : 'UsersController as vm'
                }
            }

        });

        // Translation
	
        // Navigation
        msNavigationServiceProvider.saveItem('UMM.user', {
           title : 'CONTACTS.users',
           icon  : 'icon-account-circle',
           state : 'app.users',
           weight: 10,
           backendController: 'user'
        });

    }
angular.module('fuse').requires.push('app.users');
})();
