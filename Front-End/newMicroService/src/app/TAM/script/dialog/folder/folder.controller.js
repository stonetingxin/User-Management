/**
 * Created by mumar on 2/3/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.script')
        .controller('ScriptFolderDialogController', ScriptFolderDialogController);

    function ScriptFolderDialogController($mdDialog, path, ScriptService, teamName, utilCustom, $filter) {
        var vm = this;
        vm.createFolder = createFolder;
        vm.closeDialog = closeDialog;


        function createFolder(folder) {
            if (path == "") {
                path = "/";
            }
            utilCustom.toasterLoading();
            var params = { team: teamName, folder: { Script: { Folder: { FolderName: folder.name, path: path } } } };
            ScriptService.createFolder(params).then(function() {
                utilCustom.toaster($filter('translate')('file.folder') + ' ' + $filter('translate')('data.created'));
                $mdDialog.hide(folder);
            }, function(error) {
                utilCustom.toaster($filter('translate')('data.createError') + ' ' + $filter('translate')('file.folder'));
                console.log(error);
            });




        }

        function closeDialog() {
            $mdDialog.hide();
        }


    }
})();