(function() {
    'use strict';

    angular
        .module('app.script')
        .controller('ScriptController', ScriptController);

    /** @ngInject */
    function ScriptController(MicroserviceFactory, $scope, utilCustom, msUtils, $mdToast, $mdSidenav, $filter, $state, ScriptService, $mdDialog, $document, PromptService, teamService) {
        var vm = this;

        // Data
        vm.accounts = {
            'creapond': 'johndoe@creapond.com',
            'withinpixels': 'johndoe@withinpixels.com'
        };
        vm.selectedAccount = 'creapond';
        vm.currentView = 'list';
        vm.showDetails = true;


        // Methods
        vm.select = select;
        vm.toggleDetails = toggleDetails;
        vm.toggleSidenav = toggleSidenav;
        vm.toggleView = toggleView;
        vm.deleteRe = deleteRe;
        vm.download = download;
        vm.hidden = false;
        vm.isOpen = false;
        vm.hover = true;
        vm.variablesTypes = [{ name: 'Integer', type: 'java.lang.Integer' }, { name: 'Prompt', type: 'com.cisco.prompt.Playable' }, { name: 'String', type: 'java.lang.String' }];
        vm.showFolderDialog = showFolderDialog;
        vm.showFileDialog = showFileDialog;
        vm.openFolder = openFolder;
        vm.updateName = updateName;
        vm.dateConvert = dateConvert;
        vm.updateVariables = updateVariables;
        vm.toggleInArray = msUtils.toggleInArray;
        vm.exists = msUtils.exists;
        vm.filterLanguage = filterLanguage;
        vm.languageName = languageName;
        vm.addNewVariables = addNewVariables;
        vm.removeVariables = removeVariables;
        vm.authorizationRequires = false;
        vm.folderTriverseArray = [];
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
            ScriptService.list(params).then(function(response) {

                vm.folders = response.Script.Folder;
                vm.files = response.Script.File;
                if (insertPath != 'noInsert')
                    vm.folderTriverseArray.push({ scripts: response.Script, path: response.path });
                vm.selectedPath = response.path.split("/");
                vm.path = response.path;
                vm.selectedPath[0] = "file.scripts";
                if (params.path == "") {
                    params.path = "/"
                }
                var pa = params.path;
                if (insertPath == 'no') {
                    if (params.path.substring(0, 1) != '/') {
                        pa = '/' + params.path;
                    }
                }
                vm.path = pa.split('/');




                if (vm.files != undefined) {
                    vm.selected = vm.files[0];
                    vm.selected.type = 'file';
                    ScriptService.getScriptVariables({ name: vm.selected.path + vm.selected.FileName }).then(function(response) {
                        vm.selected.variables = response.variables;
                        if (!vm.selected.variables)
                            vm.selected.variables = [{ name: '', type: 'com.cisco.prompt.Playable' }];
                        else
                            vm.selected.variables = JSON.parse(vm.selected.variables);
                    }, function(error) {
                        console.log(error);
                    });

                } else if (vm.folders) {
                    vm.selected = vm.folders[0];
                    vm.selected.FileName = vm.selected.FolderName;
                    var datee = vm.selected.Details.dateModified;
                    vm.selected.type = 'folder';
                    vm.selected.languages = [];
                } else {
                    vm.selected.path = params.path;
                }


            }, function(error) {
                console.log(error);
            })


        }
        // if(window.userRole=='admin'){
        vm.authorizationRequires = false;
        iniIt('first', 'yes');
        // }else{
        //   vm.authorizationRequires=true;
        // }





        /**
         * Select an item
         *
         * @param item
         */

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

        function backFolder() {
            var size = vm.folderTriverseArray.length - 1;
            vm.folderTriverseArray.pop();
            vm.scripts = vm.folderTriverseArray[vm.folderTriverseArray.length - 1];
            vm.selectedPath = vm.scripts.path.split("/");
            vm.selectedPath[0] = "file.scripts";
            vm.folders = (vm.scripts.scripts.Folder) ? vm.scripts.scripts.Folder : [];
            vm.files = (vm.scripts.scripts.File) ? vm.scripts.scripts.File : [];
            vm.selected.path = vm.prompts.path;
            vm.params.path = vm.selected.path;

        }

        function select(itemF, type) {
            var item = angular.copy(itemF);
            console.log(vm.paths);
            if (type === 'file') {
                item.type = 'file';
                if (!item['variables']) {
                    ScriptService.getScriptVariables({ name: item.path + item.FileName }).then(function(response) {
                            item['variables'] = JSON.parse(response.variables);
                            vm.selected = item;
                        },
                        function(error) {
                            console.log(error);
                        });
                } else {
                    vm.selected = item;
                }
            } else {
                item.type = "Folder";
                item.FileName = item.FolderName;
                vm.selected = item;
            }



            //  else
            // {
            //   if(!vm.selected.variables[0].name)
            // vm.selected.variables = JSON.parse(vm.selected.variables);
            //}
        }

        function addNewVariables() {
            if (!vm.selected.variables || vm.selected.variables.length == 0 || vm.selected.variables == "[]")
                vm.selected.variables = [{ name: '', type: 'com.cisco.prompt.Playable' }];
            else
                vm.selected.variables.push({ name: "", type: 'com.cisco.prompt.Playable' });
        }

        function removeVariables(name) {
            _.remove(vm.selected.variables, function(vari) {
                return vari.name == name;
            });

        }

        function dateConvert(date) {
            var da = $filter('limitTo')(date, 22, 0);
            return moment(da).fromNow();
        };

        function openFolder(folderName, insertPath) {
            var fldNm = JSON.stringify({ name: folderName });
            iniIt(fldNm, insertPath);
            // if (vm.paths[vm.paths.length - 1].path != folderName) {
            //     iniIt(fldNm, insertPath);
            // }

        }

        function showFolderDialog(e) {
            if (!vm.selected) {
                vm.selected = { path: vm.path };
            }
            if (vm.selected.path.split('/').length == 2 && vm.params.team != 'all') {
                vm.selected.path = vm.selected.path + setTeamName() + "/";

            }
            $mdDialog.show({
                controller: 'ScriptFolderDialogController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/script/dialog/folder/create.html',
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
                    utilCustom.toaster('Folder has been created');
                    var team = setTeamName();
                    if (team != 'all') {
                        if (vm.params.path == "/") {
                            vm.params.path = vm.params.path + team + "/"
                        }

                    }

                    var pat = JSON.stringify({ "name": vm.params.path });
                    iniIt(pat, 'noInsert');



                }

            })
        }

        function showFileDialog(e) {
            if (!vm.selected) {
                vm.selected = { path: vm.path };
            }
            if (vm.selected.path.split('/').length == 2 && vm.params.team != 'all') {
                vm.selected.path = vm.selected.path + setTeamName() + "/";

            }
            $mdDialog.show({
                controller: 'ScriptFileDialogController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/script/dialog/file/create.html',
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
                    //    utilCustom.toaster($filter('translate')('file.fileCreated'));
                    var pat = JSON.stringify({ "name": vm.selected.path });
                    iniIt(pat, 'noInsert');
                }

            })
        }
        $scope.$on('CHANGE_TEAM', function(event, args) {
            iniIt('first', 'no', args.name);
        });

        function download(id, type) {
            var params = {};
            if (type === 'file') {
                params = { path: id.path + id.FileName, team: setTeamName() };
                utilCustom.toasterLoading();
                ScriptService.download(params).then(function(response) {
                    //console.log(response);

                    downloadFile(response, id.FileName);
                    utilCustom.hideToaster();
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.playError') + ' ' + $filter('translate')('file.script'));

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

        function updateVariables(script) {
            var params = { name: script.path + script.FileName, variables: JSON.stringify(script.variables), team: setTeamName() };
            utilCustom.toasterLoading();
            ScriptService.updateVariables(params).then(function(response) {
                utilCustom.toaster($filter('translate')('file.variables') + ' ' + $filter('translate')('data.updated'));
            }, function(error) {
                utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('file.variables'));
                //  console.log(error);
            })
        }





        function updateName(data, folderData) {
            var folder = angular.copy(folderData);
            var nameFolder = folder.FileName;
            var params;

            if (folder.type == 'file') {
                var re = /^[a-zA-Z0-9-_]+.aef/;
                if (!re.test(data)) {
                    utilCustom.toaster($filter('translate')('file.updateFileErrorMessageAef'));
                    return $filter('translate')('file.updateError');
                }
                params = { team: setTeamName(), script: { Script: { File: { FileName: folder.FileName, reNameTo: data, path: folder.path } } }, scriptPath: folder.path + folder.FileName, newPath: folder.path + data };
                utilCustom.toasterLoading();
                ScriptService.updateFile(params).then(function(response) {
                    PromptService.setLocalPrompt(folder.path + folder.FileName, 'update', folder.path + data, 'scriptList');
                    utilCustom.toaster($filter('translate')('generic.name') + ' ' + $filter('translate')('data.updated'));
                    var indx = _.findIndex(vm.files, { FileName: nameFolder });
                    if (indx != -1) {
                        vm.files[indx] = response.Script.File[0];
                    }
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('generic.name'));
                    //  console.log(error);

                })
            } else {
                re = /^[a-zA-Z0-9-_]+$/;

                if (!re.test(data)) {
                    utilCustom.toaster($filter('translate')('file.updateFolderErrorMessage'));
                    return $filter('translate')('file.updateError');

                }
                params = { team: setTeamName(), folder: { Script: { Folder: { FolderName: folder.FileName, reNameTo: data, path: folder.path } } }, folderPath: folder.path + folder.FileName + '/', newPath: folder.path + data + '/' };
                utilCustom.toasterLoading();
                ScriptService.updateFolder(params).then(function(response) {
                    // console.log(response);
                    utilCustom.toaster($filter('translate')('generic.name') + ' ' + $filter('translate')('data.updated'));
                    var indx = _.findIndex(vm.folders, { FolderName: nameFolder });
                    if (indx != -1) {
                        vm.folders[indx].FolderName = data;
                    }
                    ScriptService.recallScript();
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('generic.name'));
                    //  console.log(error);

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
                    type === 'file' ? params = { path: id.path + id.FileName, name: id.FileName, team: setTeamName() } : params = { path: id.path + id.FolderName + "/", team: setTeamName() };
                    utilCustom.toasterLoading();
                    ScriptService.delete(params).then(function(response) {
                        vm.selected = undefined;
                        if (type === 'file') {
                            _.remove(vm.files, function(fl) {
                                return id.FileName === fl.FileName;
                            });
                            utilCustom.toaster($filter('translate')('file.file') + ' ' + $filter('translate')('data.deleted'));
                            PromptService.setLocalPrompt(params.path, 'delete', '', 'scriptList');
                        } else {
                            _.remove(vm.folders, function(fl) {
                                return id.FolderName === fl.FolderName;
                            });
                            utilCustom.toaster($filter('translate')('file.folder') + ' ' + $filter('translate')('data.deleted'));
                            ScriptService.recallScript();
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
