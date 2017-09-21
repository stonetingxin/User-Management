(function ()
{
    'use strict';

    angular
        .module('app.users')
        .controller('RoleController', RoleController);

    /** @ngInject */
    function RoleController($mdDialog)
    {
        var vm = this;
		vm.toppings = [
    { name: 'Pepperoni', wanted: true ,ms:'MS1'},
    { name: 'Sausage', wanted: false ,ms:'MS1'},
    { name: 'Black Olives', wanted: true, ms:'MS1'},
    { name: 'Green Peppers', wanted: false,ms:'MS1' },
	{ name: 'Pepperoni2', wanted: true ,ms:'MS2'},
    { name: 'Sausage2', wanted: false ,ms:'MS2'},
    { name: 'Black Olives2', wanted: true,ms:'MS2'},
    { name: 'Green Peppers2', wanted: false,ms:'MS2' },
	{ name: 'Pepperoni3', wanted: true ,ms:'MS3'},
    { name: 'Sausage3', wanted: false ,ms:'MS3'},
    { name: 'Black Olives3', wanted: true, ms:'MS3'},
    { name: 'Green Peppers3', wanted: false,ms:'MS3' }
  ];
		vm.microservices = [{name: 'MS1'},{ name: 'MS2'},{ name: 'MS3'}];

		vm.fruitNames = ['Apple', 'Banana', 'Orange'];






    }
})();
