(function() {
    'use strict';

    angular
        .module('app.prompt')
        .controller('PromptController', PromptController);

    /** @ngInject */
    function PromptController(MicroserviceFactory, $scope, utilCustom, msUtils, $mdToast, $mdSidenav, $filter, $state, PromptService, $mdDialog, $document, ngAudio) {
        var vm = this;

        // Data
        vm.accounts = {
            'creapond': 'johndoe@creapond.com',
            'withinpixels': 'johndoe@withinpixels.com'
        };
        vm.selectedAccount = 'creapond';
        vm.currentView = 'list';
        vm.showDetails = true;
        // vm.init = init;



        // Methods
        vm.select = select;
        vm.toggleDetails = toggleDetails;
        vm.toggleSidenav = toggleSidenav;
        vm.toggleView = toggleView;
        vm.deleteRe = deleteRe;
        vm.download = download;
        vm.playFile = playFile;
        vm.folderTriverseArray = [];
        vm.context; // Audio context
        vm.buf;
        vm.source;
        vm.hidden = false;
        vm.isOpen = false;
        vm.disableBack = 0;
        vm.hover = true;
        vm.showFolderDialog = showFolderDialog;
        vm.showFileDialog = showFileDialog;
        vm.openFolder = openFolder;
        vm.updateName = updateName;
        vm.dateConvert = dateConvert;
        vm.toggleInArray = msUtils.toggleInArray;
        vm.exists = msUtils.exists;
        vm.filterLanguage = filterLanguage;
        vm.languageName = languageName;
        vm.backFolder = backFolder;

        //////////
        function filterLanguage(language) {
            if (!vm.languageSearchText || vm.languageSearchText === '') {
                return true;
            }

            return angular.lowercase(language.name).indexOf(angular.lowercase(vm.languageSearchText)) >= 0;
        }

        function languageName(id) {
            return _.find(vm.languageList, { id: id });
        }

        function languageQuerySearch(query) {
            return query ? vm.languageList.filter(createFilterFor(query)) : [];
        }

        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(item) {
                return angular.lowercase(item.name).indexOf(lowercaseQuery) >= 0;
            };
        }
        vm.paths = [];

        function iniIt(req, insertPath, teamName) {
            var params;
            req == "first" ? params = { path: '', team: 'all' } : params = { path: JSON.parse(req).name, team: 'all' };
            vm.params = params;
            vm.selected = { languages: [] };
            vm.params.team = setTeamName(teamName);
            utilCustom.toasterLoading();
            PromptService.list(params).then(function(response) {
                utilCustom.hideToaster();
                if (insertPath != 'noInsert')
                    vm.folderTriverseArray.push({ prompts: response.Prompt, path: response.path });
              else{
                  vm.folderTriverseArray[ vm.folderTriverseArray.length-1] = { prompts: response.Prompt, path: response.path };
                }
              //if(insertPath !='noInsert')
              //  vm.disableBack = vm.disableBack + 1;
                vm.prompts = response.Prompt;
                vm.path = response.path;
                vm.selectedPath = response.path.split("/");
                vm.selectedPath[0] = "file.prompts";
              if((vm.selectedPath.length>4) && !MicroserviceFactory.APAdmin()){ // TO deal with supervisor's directory (removing his assigned folder's name) path display on the top (Right to the back button)
                vm.selectedPath.splice(2,1);
              }
                vm.folders = response.Prompt.Folder;
                vm.files = response.Prompt.File;
                if (params.path == "") {
                    params.path = "/"
                }
                var pa = params.path;
                if (insertPath == 'no') {
                    vm.folderTriverseArray[vm.folderTriverseArray.length - 1] = { prompts: response.Prompt, path: response.path };
                    if (params.path.substring(0, 1) != '/') {
                        pa = '/' + params.path;
                    }

                }


                if (vm.files != undefined) {
                    vm.selected = vm.files[0];
                    if (vm.selected)
                        vm.selected['type'] = 'file';
                    //vm.sound = ngAudio.load("../app/adminPanel/prompt/welcome.mp3");

                } else if (vm.folders) {
                    vm.selected = vm.folders[0];
                    if (vm.selected) {
                        vm.selected.FileName = vm.selected.FolderName;
                        var datee = vm.selected.Details.dateModified;
                        vm.selected['type'] = 'Folder';
                        vm.selected.languages = [];
                    }

                } else {
                    vm.selected.path = params.path;
                }


            }, function(error) {
                utilCustom.hideToaster();
                console.log(error);
            })


        }

        function backFolder() {
            var size = vm.folderTriverseArray.length - 1;
            vm.folderTriverseArray.pop();
            vm.disableBack = vm.disableBack - 1;
            vm.prompts = vm.folderTriverseArray[vm.folderTriverseArray.length - 1];
            vm.selectedPath = vm.prompts.path.split("/");
            vm.selectedPath[0] = "file.prompts";
            vm.folders = (vm.prompts.prompts.Folder) ? vm.prompts.prompts.Folder : [];
            vm.files = (vm.prompts.prompts.File) ? vm.prompts.prompts.File : [];
            vm.selected.path = vm.prompts.path;
            vm.params.path = vm.selected.path;

        }

        iniIt('first', 'yes');

        function setTeamName(teamName) {
            var name = "all";
            if (!teamName) {
                var team = window.localStorage.getItem('currentTeam');
                if (team == undefined || team == "undefined") {
                    if (!MicroserviceFactory.APAdmin()) {
                        vm.folders = [];
                        vm.files = [];
                    }
                } else {
                    team = JSON.parse(team);
                    name = team.name;
                }
            } else {
                name = teamName;
            }
            return name
        }

        $scope.$on('CHANGE_TEAM', function(event, args) {
            iniIt('first', 'no', args.name);
        });


        /**
         * Select an item
         *
         * @param item
         */
        function select(itemF, type) {
            var item = angular.copy(itemF);
            if (type === 'file') {
                //     vm.sound.pause();
                item.type = 'file';
                //    vm.sound = ngAudio.load("../app/adminPanel/prompt/welcome.mp3");

            } else {
                item.type = "Folder";
                item.FileName = item.FolderName;
            }
            vm.selected = item;
            vm.selected.languages = [];
        }

        function dateConvert(date) {
            var da = $filter('limitTo')(date, 22, 0);
            return moment(da).fromNow();
        };

        function openFolder(folderName, insertPath) {
            var fldNm = JSON.stringify({ name: folderName });
            var size = vm.paths.length;
            // if (vm.paths[size - 1].path != folderName) {
            if (size == 3 && setTeamName() != 'all') fldNm = JSON.stringify({ name: "/" + folderName });
            iniIt(fldNm, insertPath);
            //  }

        }

        function showFolderDialog(e) {
            if (!vm.selected) {
                vm.selected = { path: vm.path };
            }
            if (vm.selected.path.split('/').length == 3 && vm.params.team != 'all') {
                vm.selected.path = vm.selected.path + setTeamName() + "/";

            }

            $mdDialog.show({
                controller: 'FolderDialogController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/prompt/dialog/folder/create.html',
                parent: angular.element($document.body),
                targetEvent: e,
                clickOutsideToClose: true,
                locals: {
                    event: e,
                    path: vm.selected.path,
                    teamName: setTeamName()
                }
            }).then(function(response) {

                if (response) {
                    utilCustom.toaster($filter('translate')('file.folder') + ' ' + $filter('translate')('data.created'));

                    var pat = JSON.stringify({ "name": vm.params.path });
                    iniIt(pat, 'noInsert');

                }

            })
        }

        function showFileDialog(e) {
            if (!vm.selected) {
                vm.selected = { path: vm.path };
            }
            if (vm.selected.path.split('/').length == 3 && vm.params.team != 'all') {
                vm.selected.path = vm.selected.path + setTeamName() + "/";

            }
            $mdDialog.show({
                controller: 'FileDialogController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/prompt/dialog/file/create.html',
                parent: angular.element($document.body),
                targetEvent: e,
                clickOutsideToClose: false,
                locals: {
                    event: e,
                    path: vm.selected.path,
                    teamName: setTeamName()
                }
            }).then(function(response) {
                if (response) {

                  var pat = JSON.stringify({ "name": vm.params.path });
                    iniIt(pat, 'noInsert');
                }

            })
        }

        function download(id, type) {
            var params = {};
            if (type === 'file') {
                params = { path: id.path + id.FileName, team: setTeamName() };
                utilCustom.toasterLoading();
                PromptService.download(params).then(function(response) {
                    //console.log(response);

                    downloadFile(response, id.FileName);
                    playFile(response, id.FileName);
                    utilCustom.hideToaster();
                }, function(error) {
                    console.log(error);
                    utilCustom.toaster('Error while downloading the prompt');
                })
            }
        }

        function downloadFile(data, fileName) {
            var arr = data;
            var byteArray = new Uint8Array(arr);
            var a = window.document.createElement('a');

            a.href = window.URL.createObjectURL(new Blob([byteArray], { type: 'application/octet-stream' }));
            a.download = fileName;

            // Append anchor to body.
            document.body.appendChild(a);
            a.click();


            // Remove anchor from body
            document.body.removeChild(a)
        }

        function playFile(id, type) {
            var params = {};
            if (type === 'file') {
                params = { path: id.path + id.FileName, team: setTeamName() };
                utilCustom.toasterLoading();
                PromptService.download(params).then(function(response) {
                    var byteArray = new Uint8Array(response);
                    playByteArray(byteArray);
                    utilCustom.hideToaster();
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.playError') + ' ' + $filter('translate')('file.prompt'));
                    //   console.log(error);

                })
            }


        }

        function playByteArray(byteArray) {
            // Audio buffer

            if (!window.AudioContext) {
                if (!window.webkitAudioContext) {
                    alert("Your browser does not support any AudioContext and cannot play back this audio.");
                    // return;
                }
                window.AudioContext = window.webkitAudioContext;
            }

            vm.context = new AudioContext();
            var arrayBuffer = new ArrayBuffer(byteArray.length);
            var bufferView = new Uint8Array(arrayBuffer);
            for (var i = 0; i < byteArray.length; i++) {
                bufferView[i] = byteArray[i];
            }

            vm.context.decodeAudioData(arrayBuffer, function(buffer) {
                vm.buf = buffer;
                play();
            });
        }

        // Play the loaded file
        function play() {
            // Create a source node from the buffer
            vm.source = vm.context.createBufferSource();
            vm.source.buffer = vm.buf;
            // Connect to the final output node (the speakers)
            vm.source.connect(vm.context.destination);
            // Play immediately
            vm.source.start(0);
        }


        function updateName(data, folderData) {
            var folder = angular.copy(folderData);
            var nameFolder = folder.FileName;
            var params;
            if (folder.type == 'file') {
                var re = /^[a-zA-Z0-9-_]+.wav$/;
                if (!re.test(data)) {
                    utilCustom.toaster($filter('translate')('file.updateFileErrorMessageWav'));
                    return $filter('translate')('file.updateError');
                }
                utilCustom.toasterLoading();
                params = { team: setTeamName(), prompt: { Prompt: { File: { FileName: folder.FileName, reNameTo: data, path: folder.path } } }, promptPath: folder.path + folder.FileName, newPath: folder.path + data };
                PromptService.updateFile(params).then(function(response) {
                    PromptService.setLocalPrompt(folder.path + folder.FileName, 'update', folder.path + data, 'promptList');
                    utilCustom.toaster($filter('translate')('generic.name') + ' ' + $filter('translate')('data.updated'));
                    var indx = _.findIndex(vm.files, { FileName: nameFolder });
                    if (indx != -1) {
                        vm.files[indx] = response.Prompt.File[0];
                    }


                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('generic.name'));
                    //console.log(error);

                })
            } else {
                re = /^[a-zA-Z0-9-_]+$/;

                if (!re.test(data)) {
                    utilCustom.toaster($filter('translate')('file.updateFolderErrorMessage'));
                    return $filter('translate')('file.updateError');

                }
                params = { team: setTeamName(), folder: { Prompt: { Folder: { FolderName: folder.FileName, reNameTo: data, path: folder.path } } }, folderPath: folder.path + folder.FileName + '/', newPath: folder.path + data + '/' };
                PromptService.updateFolder(params).then(function(response) {
                    // console.log(response);
                    utilCustom.toaster($filter('translate')('generic.name') + ' ' + $filter('translate')('data.updated'));
                    var indx = _.findIndex(vm.folders, { FolderName: nameFolder });
                    if (indx != -1) {
                        vm.folders[indx].FolderName = data;
                    }
                    PromptService.recallPrompt();
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('generic.name'));
                    // console.log(error);

                })
            }


        }
        /**
         * Toggle details
         *
         * @param item
         */
        function toggleDetails(item) {
            vm.selected = item;
            toggleSidenav('details-sidenav');
        }

        function deleteRe(id, type) {



            utilCustom.toasterConfirm().then(function(response) {
                if (response == 'ok' || response) {
                    var params = {};
                    type === 'file' ? params = { path: id.path + id.FileName, team: setTeamName() } : params = { team: setTeamName(), path: id.path + id.FolderName + "/" };
                    utilCustom.toasterLoading();
                    PromptService.delete(params).then(function(response) {
                        vm.selected = undefined;
                        if (type === 'file') {
                            _.remove(vm.files, function(fl) {
                                return id.FileName === fl.FileName;

                            });
                            utilCustom.toaster($filter('translate')('file.file') + ' ' + $filter('translate')('data.deleted'));;
                        } else {
                            utilCustom.toaster($filter('translate')('file.folder') + ' ' + $filter('translate')('data.deleted'));

                            _.remove(vm.folders, function(fl) {
                                return id.FolderName === fl.FolderName;
                            })
                        }


                    }, function(error) {
                        console.log(error);
                        utilCustom.toaster($filter('translate')('data.deleteError'));
                    })
                } else {
                    utilCustom.toaster($filter('translate')('generic.noOptionSelected'));

                }
            });




        }

        /**
         * Toggle sidenav
         *
         * @param sidenavId
         */
        function toggleSidenav(sidenavId) {
            $mdSidenav(sidenavId).toggle();
        }

        /**
         * Toggle view
         */
        function toggleView() {
            vm.currentView = vm.currentView === 'list' ? 'grid' : 'list';
        }
    }
})();
