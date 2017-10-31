/**
 * Created by mumar on 2/3/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.prompt')
        .controller('FolderDialogController', FolderDialogController);

    function FolderDialogController($mdDialog, path, PromptService, utilCustom, teamName, $filter) {
        var vm = this;
        vm.createFolder = createFolder;
        vm.closeDialog = closeDialog;


        function createFolder(folder) {
            if (path == "") {
                path = "/";
            }
            var params = { team: teamName, folder: { Prompt: { Folder: { FolderName: folder.name, path: path } } } };
            utilCustom.toasterLoading();
            PromptService.createFolder(params).then(function() {
                utilCustom.toaster($filter('translate')('file.folder') + ' ' + $filter('translate')('data.created'));
                $mdDialog.hide(folder);
            }, function(error) {
                utilCustom.toaster($filter('translate')('data.createError') + ' ' + $filter('translate')('file.folder'));
                //console.log(error);
            });




        }

        function closeDialog() {
            $mdDialog.hide();
        }


    }
})();