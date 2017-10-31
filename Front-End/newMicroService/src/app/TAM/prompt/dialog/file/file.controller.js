/**
 * Created by mumar on 2/3/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.prompt')
        .controller('FileDialogController', FileDialogController);

    function FileDialogController($filter, $mdDialog, PromptService, path, teamName, utilCustom) {
        var vm = this;
        // vm.createFile = createFile;
        vm.upload = upload;
        vm.uploadMultiple = uploadMultiple;
        vm.closeDialog = closeDialog;


        // CALLBACKS

        function upload(prompt) {
            if (path == "") {
                path = "/";
            }
            var promptArray = [];
            if (prompt.file.length == 0) {
                utilCustom.toaster($filter('translate')('file.selectFile'));
                return
            }
            utilCustom.toasterLoading();
            angular.forEach(prompt.file, function(file, idx) {
                if (prompt.file.length - 1 == idx) {
                    utilCustom.hideToaster();
                }
                var params = { team: teamName, prompt: { Files: { xsi: { noNamespaceSchemaLocation: file.name } }, Prompt: { File: { path: path, FileName: file.name } } }, file: file };

                var checkSpace = /\s/g.test(file.name);
                if (!isWaveFile(file.name) || checkSpace) {
                    vm.fileFormate = true;
                    if (checkSpace) {
                        prompt.file[idx].status = $filter('translate')('file.fileNotSpace');
                        prompt.file[idx].errorStatus = 0;
                    } else {
                        utilCustom.toaster($filter('translate')('file.onlyWavFormat'));
                        prompt.file[idx].status = $filter('translate')('file.onlyWavFormat');
                        prompt.file[idx].errorStatus = 0;
                    }
                } else {
                    vm.fileFormate = false;
                    PromptService.createFile(params).then(function(response) {
                        prompt.file[idx].status = $filter('translate')('file.uploaded');
                        prompt.file[idx].errorStatus = 1;
                        var filePath = path + file.name;
                        promptArray.push(params.prompt.Prompt);
                        if (prompt.file.length - 1 == idx) {
                            utilCustom.hideToaster();
                        }
                    }, function(error) {
                        console.log(error);
                        //utilCustom.toaster($filter('translate')('data.uploadError') +' ' + $filter('translate')('file.file'));
                        prompt.file[idx].status = $filter('translate')('data.uploadError') + ' ' + $filter('translate')('file.file');
                        prompt.file[idx].errorStatus = 2;
                        if (prompt.file.length - 1 == idx) {
                            utilCustom.hideToaster();
                        }
                    });
                }

            });






        }

        function uploadMultiple(filesData) {
            var promptArray = { fileData: [] }
            angular.forEach(filesData.file, function(prompt) {
                var params = { prompt: { Files: { xsi: { noNamespaceSchemaLocation: prompt.name } }, Prompt: { File: { path: path, FileName: prompt.name } } }, file: prompt };
                if (!isWaveFile(prompt.name)) {
                    vm.fileFormate = true;
                } else {

                    promptArray.fileData.push(params)
                }

            });
            if (vm.fileFormate) {
                utilCustom.toaster($filter('translate')('file.onlyWavFormat'));
            } else {
                console.log(promptArray);
                PromptService.createFile(promptArray).then(function(response) {
                    var filePath = path + prompt.file.name;
                    $mdDialog.hide(prompt);
                    PromptService.setLocalPrompt(filePath, 'add', '', 'promptList');
                }, function(error) {
                    console.log(error);
                    utilCustom.toaster($filter('translate')('data.uploadError') + ' ' + $filter('translate')('file.file'));
                });
            }


        }

        function getExtension(filename) {
            var parts = filename.split('.');
            return parts[parts.length - 1];
        }

        function isWaveFile(filename) {
            var ext = getExtension(filename);
            switch (ext.toLowerCase()) {
                case 'wav':
                    return true;
            }
            return false;
        }

        function closeDialog() {
            $mdDialog.hide('res');
        }


    }
})();