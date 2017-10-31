
/**
 * Created by mumar on 1/13/2016.
 */
(function()
{
  'use strict';
  angular
    .module('app.queue')
    .factory('DialogServiceQueue',dialogServiceQueue);


  function dialogServiceQueue($mdDialog, $document,api,$q){
    var service = {
      data:{},
      openCreateQueueDialog:openCreateQueueDialog,
      openEditQueueDialog:openEditQueueDialog,
      getQueueData:getQueueData
    };
    function openCreateQueueDialog(ev)
    {
      $mdDialog.show({
        templateUrl        : 'app/TAM/queue/dialog/createQueue.html',
        controller         : "CreateQueueController",
        controllerAs       : "vm",
        parent             : $document.find('#scrumboard'),
        targetEvent        : ev,
        clickOutsideToClose: true,
        escapeToClose      : true
      }).then(function(response){

      })
    }
    function openEditQueueDialog(ev,queueId){
      $mdDialog.show({
        templateUrl        : 'app/TAM/queue/dialog/editQueue.html',
        controller         : "EditQueueController",
        controllerAs       : "vm",
        parent             : $document.find('#scrumboard'),
        targetEvent        : ev,
        clickOutsideToClose: true,
        escapeToClose      : true,
        locals :{
          queueId:queueId
        }
      });
    }
    function getQueueData()
    {
      // Create a new deferred object
      var deferred = $q.defer();

      api.queue.list.get({},

        // SUCCESS
        function (response)
        {
          // Attach the data
         // console.log(response.queueList);
          service.data = response.queueList;

          // Resolve the promise
          deferred.resolve(response);
        },

        // ERROR
        function (response)
        {
          // Reject the promise
          deferred.reject(response);
        }
      );

      return deferred.promise;
    }
    return service;
  }
})();


