/**
 * Created by mumar on 2/17/2016.
 */
(function(){
  'use strict';
  angular
    .module('app.application')
    .controller('CreateApplicationController',CreateApplicationController);


  function CreateApplicationController(scripts,msUtils,$mdDialog){
       var vm =this;
    vm.filterLanguage = filterLanguage;
    vm.languageName = languageName;
    vm.toggleInArray = msUtils.toggleInArray;
    vm.exists = msUtils.exists;
    vm.application ={languages:[],enabled:true};
    vm.languageQuerySearch = languageQuerySearch;
    vm.createApplication = createApplication;
    vm.close = close;
    vm.scripts = scripts;

    vm.types = ['Cisco Script Application','Busy','Ring-No-Answer'];
    function filterLanguage(language)
    {
      if ( !vm.languageSearchText || vm.languageSearchText === '' )
      {
        return true;
      }

      return angular.lowercase(language.name).indexOf(angular.lowercase(vm.languageSearchText)) >= 0;
    }
    function languageName(id){
      return _.find(vm.languageList, {id:id});
    }
    function languageQuerySearch(query)
    {
      return query ? vm.languageList.filter(createFilterFor(query)) : [];
    }
    function createFilterFor(query)
    {
      var lowercaseQuery = angular.lowercase(query);
      return function filterFn(item)
      {
        return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
      };
    }

    function createApplication(application){
      $mdDialog.hide(application);
    }
    function close(){
      $mdDialog.hide();
    }

  }
})();
