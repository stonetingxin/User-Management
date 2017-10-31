/**
 * Created by mumar on 2/3/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.script')
        .controller('ScriptFileDialogController', ScriptFileDialogController);

    function ScriptFileDialogController(PromptService, $mdDialog, ScriptService, path, teamName, utilCustom, $filter) {
        var vm = this;
        // vm.createFile = createFile;
        vm.upload = upload;
        vm.closeDialog = closeDialog;


        // CALLBACKS

        function upload(script) {
            if (path == "") {
                path = "/";
            }
            utilCustom.toasterLoading();
            angular.forEach(script.file, function(file, idx) {

                var params = { team: teamName, script: { Files: { xsi: { noNamespaceSchemaLocation: file.name } }, Script: { File: { path: path, FileName: file.name } } }, file: file };
                var checkSpace = /\s/g.test(file.name);


                if (!isWaveFile(file.name) || checkSpace) {
                    if (checkSpace) {
                        script.file[idx].status = $filter('translate')('file.fileNotSpace');
                        script.file[idx].errorStatus = 0;
                    } else {
                        vm.fileFormate = true;
                        script.file[idx].status = $filter('translate')('file.onlyAefFormat');
                        script.file[idx].errorStatus = 0;
                    }

                } else {
                    ScriptService.createFile(params).then(function(response) {
                        var filePath = path + script.file.name;
                        script.file[idx].status = $filter('translate')('file.uploaded');
                        script.file[idx].errorStatus = 1;
                        if (script.file.length - 1 == idx) {
                            utilCustom.hideToaster();
                        }
                    }, function(error) {
                        var msg = $filter('translate')('file.file') + $filter('translate')('data.uploadError');
                        //utilCustom.toaster(msg);
                        script.file[idx].status = msg;
                        script.file[idx].errorStatus = 2;
                        console.log(error);
                        if (script.file.length - 1 == idx) {
                            utilCustom.hideToaster();
                        }
                    });
                }

            });

            // var params = {script:{Files:{xsi:{ noNamespaceSchemaLocation:script.file.name}},Script:{ File: { path:path,FileName:script.file.name }}},file:script.file };
            //
            //
            // if(!isWaveFile(script.file.name)){
            //   utilCustom.toaster($filter('translate')('file.onlyAefFormat'));
            //   vm.fileFormate = true;
            // }else{
            //   vm.fileFormate = false;
            //   utilCustom.toasterLoading();
            //   ScriptService.createFile(params).then(function(response){
            //     var filePath = path+script.file.name;
            //     $mdDialog.hide(script);
            //     PromptService.setLocalPrompt(filePath,'add','','scriptList');
            //   },function(error){
            //     utilCustom.toaster($filter('translate')('data.uploadError') +' ' + $filter('translate')('file.file'));
            //     console.log(error);
            //   });
            // }


        }

        function getExtension(filename) {
            var parts = filename.split('.');
            return parts[parts.length - 1];
        }

        function isWaveFile(filename) {
            var ext = getExtension(filename);
            switch (ext.toLowerCase()) {
                case 'aef':
                    return true;
            }
            return false;
        }

        function closeDialog() {
            $mdDialog.hide('res');
        }


    }
})();