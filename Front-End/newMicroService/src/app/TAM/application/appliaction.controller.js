/**
 * Created by mumar on 2/17/2016.
 */
(function() {
    'use strict';
    angular
        .module('app.application')
        .controller('ApplicationController', applicationController);

    function applicationController(MicroserviceFactory, $scope, ApplicationService, ScriptService, utilCustom, $mdDialog, $document, msUtils, triggerService, PromptService, callControlGroupService, $filter) {
        var vm = this;

        vm.openCreateApplicationDialog = openCreateApplicationDialog;
        vm.translationData = {
            script: $filter('translate')('file.script')
        };



        vm.languageList = [{
                'id': 1,
                'name': 'English',
                'translation': 'TOOLBAR.ENGLISH',
                'code': 'en',
                'flag': 'us'
            },
            {
                'id': 2,
                'name': 'German',
                'translation': 'TOOLBAR.German',
                'code': 'ug',
                'flag': 'ug'
            },
            {
                'id': 3,
                'name': 'French',
                'translation': 'TOOLBAR.French',
                'code': 'fr',
                'flag': 'fr'
            }
        ];

        //methods
        vm.toggleSidenav = toggleSidenav;
        vm.toggleDetails = toggleDetails;
        vm.select = select;
        vm.filterLanguage = filterLanguage;
        vm.saveApplication = saveApplication;
        vm.languageName = languageName;
        vm.toggleInArray = msUtils.toggleInArray;
        vm.exists = msUtils.exists;
        vm.changeScript = changeScript;
        vm.createFormOr = undefined;
        vm.removeValidation = removeValidation;
        vm.languageQuerySearch = languageQuerySearch;
        vm.deleteApplication = deleteApplication;
        vm.addTrigger = addTrigger;
        vm.deleteTrigger = deleteTrigger;
        vm.getALlList = getALlList;
        vm.create = create;
        vm.changeLanguage = changeLanguage;
        vm.selected = { languages: [] };
        vm.appTrigger = [];
        vm.prompts = [];
        vm.triggers = [];
        vm.callControlGroups = [];
        vm.languages = ['en_AU', 'en_CA', 'en_GB', 'en_US', 'en', 'ur'];
        vm.types = ['Cisco Script Application', 'Busy', 'Ring-No-Answer'];
        vm.triggerType = ['Unified CM Telephony Trigger', 'Cisco Http Trigger'];
        vm.selected = { enabled: false };

        function init(teamName) {
            var params = { id: "all" };
            params.id = setTeamName(teamName);
            utilCustom.toasterLoading();
            ApplicationService.list(params).then(function(response) {
                vm.applications = response;

                ScriptService.getAllScripts({ team: params.id }).then(function(response) {
                    utilCustom.hideToaster();
                    vm.scripts = [];
                    vm.scriptList = [];
                    response.map(function(scpt) {
                        if (scpt != "") {
                            vm.scriptList.push(scpt);
                            vm.scripts.push(scpt);
                        }

                    })



                }, function(error) {
                    utilCustom.hideToaster();
                    console.log(error);
                });
            }, function(error) {
                utilCustom.hideToaster();
                console.log(error);
            });
            getALlList();



        }

        function setTeamName(teamName) {
            var name = "all";
            if (!teamName) {
                var team = window.localStorage.getItem('currentTeam');
                if (team == undefined || team == "undefined") {
                    if (!MicroserviceFactory.APAdmin()) {
                        vm.applications = [];
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

        init();

        $scope.$on('CHANGE_TEAM', function(event, args) {
            create();
            init(args.name);
        });

        function getALlList() {


            //  if(vm.prompts.length==0){
            getPrompt();
            //  }
            if (vm.triggers.length == 0) {
                getTrigger();
            }
            if (vm.callControlGroups.length == 0) {
                getCallControlGroup();
            }
        }

        function select(item) {
            if (vm.createForm) {
                // Use the following to reset dirty stat
                vm.createForm.$setPristine();
                vm.createForm.$setUntouched();
                // Use the following to reset to clear validation
                vm.createForm.$setValidity();
            }

            if(!/^[a-zA-Z0-9-_]+$/.test(item.applicationName)){
              utilCustom.toaster(escapeHtml($filter('translate')('data.specialCharacter')));
              return;
            }

            getALlList();
            vm.variables = [];
            vm.appTrigger = [];
            vm.trigger = { selectLanguage: 'en' };
            angular.forEach(vm.triggers, function(val, key) {
                if (val.application['@name'] === item.applicationName) {
                    vm.appTrigger.push(val);
                }
            });
            vm.scriptList = angular.copy(vm.scripts);
            vm.createFormOr = 'edit';
            vm.selected = angular.copy(item);
            vm.scriptName = vm.selected.ScriptApplication.script.split('[')[1].replace(']', '');
            vm.selected.script = vm.scriptName;
            var findScript = vm.scriptList.find(function(val) {
                return val == vm.scriptName;
            });
            if (!findScript) {
                vm.scriptList.push(vm.scriptName);
            }
            utilCustom.toasterLoading();
            ApplicationService.getApplication({ id: vm.selected.applicationName }).then(function(response) {
                setScriptVariables(response);
                vm.selected = response;
                vm.selected.script = vm.scriptName;

                ScriptService.getScriptVariables({ name: "/" + vm.scriptName }).then(function(response) {
                        if (response.variables)
                            vm.variables = JSON.parse(response.variables);
                        if (!vm.selected.ScriptApplication.scriptParams) {
                            vm.selected.ScriptApplication.scriptParams = [];
                            utilCustom.hideToaster();
                            angular.forEach(vm.variables, function(value, key) {
                                vm.selected.ScriptApplication.scriptParams.push({ name: value.name, type: value.type, value: "" });
                            })
                        } else {
                            if (vm.variables) utilCustom.hideToaster();
                            angular.forEach(vm.variables, function(svalue, skey) {
                                if (vm.variables.length - 1 == skey)
                                    utilCustom.hideToaster();
                                var found = _.find(vm.selected.ScriptApplication.scriptParams, { name: svalue.name });
                                if (!found) {
                                    var value = "";
                                    if (svalue.type === 'java.lang.Integer') {
                                        value = 0
                                    }
                                    vm.selected.ScriptApplication.scriptParams.push({ name: svalue.name, type: svalue.type, value: value });
                                } else {
                                    found = _.findIndex(vm.selected.ScriptApplication.scriptParams, { name: svalue.name });
                                    if (svalue.type === 'java.lang.Integer') {
                                        vm.selected.ScriptApplication.scriptParams[found].value = Number(vm.selected.ScriptApplication.scriptParams[found].value);
                                    } else if (svalue.type === 'com.cisco.prompt.Playable') {
                                        vm.selected.ScriptApplication.scriptParams[found].value = vm.selected.ScriptApplication.scriptParams[found].value;
                                    }

                                }
                            });
                            setScriptVariables(vm.selected);
                        }
                    },
                    function(error) {
                        utilCustom.hideToaster();
                        setScriptVariables(vm.selected);
                        console.log(error);
                    });




            }, function(error) {
                utilCustom.hideToaster();
                console.log(error);
            });

            vm.selected.languages = [];
        }

        function setScriptVariables(variables) {
            angular.forEach(variables.ScriptApplication.scriptParams, function(value, key) {
                if (value.type === 'java.lang.Integer') {
                    variables.ScriptApplication.scriptParams[key].value = Number(value.value);
                }
                if (value.type === 'com.cisco.prompt.Playable') {
                    variables.ScriptApplication.scriptParams[key].value = value.value;
                }

            });
        }

        function addTrigger(trigger, application) {
            var applicationUrl = application.self;
            applicationUrl = applicationUrl.replace('application', 'callControlGroup');
            var replaceId = applicationUrl.split('/');
            replaceId[replaceId.length - 1] = trigger.callControlGroup;
            applicationUrl = replaceId.join("/");
            var params = {
                trigger: {

                    "directoryNumber": trigger.directoryNumber,
                    "locale": trigger.language,
                    "application": {
                        "@name": application.applicationName,
                        "refURL": application.self
                    },
                    "deviceName": trigger.deviceName,
                    "description": trigger.directoryNumber,
                    "callControlGroup": {
                        "@name": trigger.callControlGroup,
                        "refURL": applicationUrl
                    },
                    "triggerEnabled": true,
                    "maxNumOfSessions": 10,
                    "idleTimeout": 5000,

                    "alertingNameAscii": "",
                    "devicePool": "Default",
                    "location": "Hub_None",
                    "partition": "None",
                    "voiceMailProfile": "None",
                    "callingSearchSpace": "None",
                    "callingSearchSpaceForRedirect": "default",
                    "presenceGroup": "Standard Presence group",
                    "forwardBusy": {
                        "forwardBusyVoiceMail": false,
                        "forwardBusyDestination": "",
                        "forwardBusyCallingSearchSpace": "None"
                    },
                    "display": "",
                    "externalPhoneMaskNumber": ""
                },
                directoryNumber: trigger.directoryNumber
            };
            utilCustom.toasterLoading(0);
            triggerService.save(params).then(function(response) {
                utilCustom.hideToaster();
                if (vm.addTrigger) {
                    vm.appTrigger.push(trigger);
                }
                getTrigger();
                vm.trigger = { selectLanguage: 'en' };
                utilCustom.toaster($filter('translate')('application.trigger.trigger') + ' ' + $filter('translate')('data.added'));
            }, function(error) {
                checkError(error);
            });

        }

        function checkError(errorg) {
            if (angular.isArray(errorg.data.apiError)) {
                var errMessage = "";
                angular.forEach(errorg.data.apiError, function(er) {
                    errMessage = errMessage + '(' + er.errorData + ')' + er.errorMessage + "\n";
                });


                utilCustom.toaster(errMessage);
            } else {
                utilCustom.toaster($filter('translate')('data.addError') + ' ' + $filter('translate')('application.trigger.trigger'));
            }
        }

        function getPrompt() {

            PromptService.getAllPrompts({ team: setTeamName() }).then(function(response) {
                vm.prompts = [];
                vm.prompts.push($filter('translate')('application.noPrompt'));
                response.map(function(prmpt) {
                    if (prmpt != "")
                        vm.prompts.push(prmpt);
                });
            });
        }

        function getTrigger() {
            triggerService.list('s').then(function(response) {
                vm.triggers = response.trigger;


            }, function(error) {
                console.log(error);
            });
        }

        function getCallControlGroup() {
            callControlGroupService.list('fir').then(function(response) {
                vm.callControlGroups = response.callControlGroup;
            }, function(error) {
                console.log(error);
            })
        }

        function changeLanguage() {
            vm.trigger.language = vm.trigger.selectLanguage;
        }

        function deleteApplication(app) {
            utilCustom.toasterConfirm().then(function(response) {
                if (response == 'ok' || response) {
                    utilCustom.toasterLoading();
                    var params = { id: app.applicationName };
                    ApplicationService.delete(params).then(function(responseT) {
                        utilCustom.toaster($filter('translate')('application.application') + ' ' + $filter('translate')('data.deleted'));
                        var found = _.findIndex(vm.applications, { applicationName: params.id });
                        vm.applications.splice(found, 1);
                        vm.appTrigger.forEach(function(trigger) {
                            deleteTrigger(trigger.directoryNumber, 'noShwo');
                        });
                        create()
                    }, function(error) {
                        utilCustom.toaster($filter('translate')('data.deleteError') + ' ' + $filter('translate')('application.application'));
                    })
                } else {
                    utilCustom.toaster($filter('translate')('generic.noOptionSelected'));

                }
            });

        }

        function changeScript(applic) {
            var scriptVariables;
            ScriptService.getScriptVariables({ name: "/" + applic.script }).then(function(response) {
                    scriptVariables = JSON.parse(response.variables);
                    if (scriptVariables) {
                        if (vm.createFormOr && applic.ScriptApplication)
                            applic.ScriptApplication.scriptParams = [];
                        else
                            applic.ScriptApplication = { scriptParams: [] };
                        var variables = [];
                        if (scriptVariables)
                            variables = scriptVariables;
                        if (variables.length != 0) {
                            angular.forEach(variables, function(svalue, skey) {
                                //if(!_.find(applic.ScriptApplication.scriptParams,{name:svalue.name})){
                                var value = "";
                                if (svalue.type === 'java.lang.Integer') {
                                    value = 0
                                }
                                applic.ScriptApplication.scriptParams.push({ name: svalue.name, type: svalue.type, value: value });
                                //}
                            });
                        }

                    } else {
                        applic.ScriptApplication = { scriptParams: [] };
                    }
                },
                function(error) {
                    console.log(error);
                });

        }
        function escapeHtml(unsafe) {
          return unsafe
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;","'")
            .replace("&#34;","\"");
        }
        function saveApplication(appli, trigger) {
            var application = angular.copy(appli);
            if (!application.applicationName || !application.maxsession || !application.id || !application.script || vm.createForm.$invalid) {
                utilCustom.toaster($filter('translate')('data.fillAllField'));
                return;
            }
            utilCustom.toasterLoading();
            if(!/^([A-Za-z0-9\-_,\/\s]+(\.aef))$/.test(application.script)){
              utilCustom.toaster(escapeHtml($filter('translate')('data.specialCharacter')));
              return;
            }
            var params = {
                application: {
                    "self": application.self,
                    "ScriptApplication": application.ScriptApplication,
                    "id": application.id,
                    "applicationName": application.applicationName,
                    "type": application.type,
                    "description": application.applicationName,
                    "maxsession": application.maxsession,
                    "enabled": application.enabled
                },
                applicationName: application.applicationName
            };

            if (application.script) {
                if (vm.createFormOr === 'edit')
                    params.application.ScriptApplication.script = "SCRIPT[" + application.script + "]";
                else
                    params.application.ScriptApplication = { script: "SCRIPT[" + application.script + "]" };
                if (application.hasOwnProperty('ScriptApplication')) {
                    params.application['ScriptApplication']['scriptParams'] = application.ScriptApplication.scriptParams;
                }
            }

            var scriptParams = [];
            angular.forEach(params.application.ScriptApplication.scriptParams, function(value, key) {
                if (value.value) {
                    if (value.type == "com.cisco.prompt.Playable" && value.value == "No Prompt") {} else {
                        scriptParams.push(value);
                    }

                }
            });
            params.application.ScriptApplication.scriptParams = scriptParams;
            params.team = setTeamName();
            if (vm.createFormOr === 'edit') {
                ApplicationService.update(params).then(function(response) {
                    var idx = _.findIndex(vm.applications, { 'applicationName': params.applicationName });
                    if (idx) {
                        vm.applications[idx] = params.application;
                        select(vm.applications[idx]);
                    }

                    utilCustom.toaster($filter('translate')('application.application') + ' ' + $filter('translate')('data.updated'));
                }, function(error) {
                    utilCustom.toaster($filter('translate')('data.updateError') + ' ' + $filter('translate')('application.application'));
                })
            } else {
                params.application.type = "Cisco Script Application";
                params.application.description = application.applicationName;
                params.team = setTeamName();

                ApplicationService.save(params).then(function(data) {
                    utilCustom.toaster($filter('translate')('application.application') + ' ' + $filter('translate')('data.created'));
                    vm.applications.push(data.application);
                    select(data.application);
                }, function(error) {
                    checkError(error);
                    //   utilCustom.toaster($filter('translate')('data.createError')+' '+$filter('translate')('application.application')); utilCustom.toaster('Error while creating');
                })
            }

        }

        function openCreateApplicationDialog(ev) {
            $mdDialog.show({
                templateUrl: 'app/TAM/application/dialog/create.html',
                controller: "CreateApplicationController",
                controllerAs: "vm",
                parent: $document.find('#scrumboard'),
                targetEvent: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                locals: {
                    event: ev,
                    scripts: vm.scripts
                }
            }).then(function(response) {
                vm.applications.push(response);
            })

        }

        function deleteTrigger(directoryNumber, showLoading) {
            if (!showLoading) {
                utilCustom.toasterConfirm().then(function(response) {
                    if (response == 'ok' || response) {
                        if (!showLoading)
                            utilCustom.toasterLoading();
                        performDeleteActionForTrigger(directoryNumber, showLoading)
                    } else {
                        if (vm.addTrigger)
                            vm.appTrigger.push(_.find(vm.triggers, { directoryNumber: directoryNumber }));
                        //  utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
                    }
                }, function(error) {
                    if (vm.addTrigger)
                        vm.appTrigger.push(_.find(vm.triggers, { directoryNumber: directoryNumber }));
                });
            } else {
                performDeleteActionForTrigger(directoryNumber, showLoading);
            }


        }

        function performDeleteActionForTrigger(directoryNumber, showLoading) {
            triggerService.delete({ id: directoryNumber }).then(function(reponse) {
                if (!showLoading)
                    utilCustom.toaster($filter('translate')('application.trigger.trigger') + ' ' + $filter('translate')('data.deleted'));
                getTrigger();
            }, function(error) {
                if (!showLoading)
                    utilCustom.toaster($filter('translate')('data.deleteError') + ' ' + $filter('translate')('application.trigger.trigger'));
            })
        }

        function toggleDetails(item) {
            vm.selected = item;
            toggleSidenav('details-sidenav');
        }

        function toggleSidenav(sidenavId) {
            $mdSidenav(sidenavId).toggle();
        }

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

        function create() {
            vm.scriptList = angular.copy(vm.scripts); // vm.scripts;
            vm.createFormOr = undefined;
            vm.appTrigger = [];
            vm.getALlList();
            vm.selected = { enabled: false };
        }

        function removeValidation(form) {
            console.log(form);
        }
    }


})();
