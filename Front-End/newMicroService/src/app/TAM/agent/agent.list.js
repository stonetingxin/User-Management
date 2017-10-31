/**
 * Created by mumar on 1/7/2016.
 */
(function() {

    'use strict';
    // agGrid.initialiseAgGridWithAngular1(angular);

    angular
        .module('app.agent')
        .controller('AgentController', agentController);

    function agentController(MicroserviceFactory, utilCustom, $scope, $document, $filter, $element, agentService, $rootScope, $mdDialog, groupService, skillService, teamService) {
        var vm = this;
        // vm.agentList =  AgentData.agentList;

        vm.selectedAgent = selectedAgent;
        vm.addNewSkill = addNewSkill;
        vm.dialogShowDetail = dialogShowDetail;
        vm.skillQuerySearch = skillQuerySearch;
        vm.filterSkill = filterSkill;
        vm.toggleInArraySkill = toggleInArraySkill;
        vm.existsSkill = existsSkill;
        vm.findSkill = findSkill;
        vm.selectedSkill = selectedSkill;
        vm.saveAgent = saveAgent;
        vm.searchAgent = searchAgent;
        vm.searchResultClick = searchResultClick;
        vm.clearSearchTerm = clearSearchTerm;
        vm.stopPropagation = stopPropagation;

        vm.searchTerm = '';

        function iniIt() {

            vm.agents = [{}];
            utilCustom.toasterLoading();
            //if(!$rootScope.agentList){
            agentService.list({}).then(function(response) {

                vm.agentList = response.resource;
                $rootScope.agentList = vm.agentList;
                parseAgentList(vm.agentList);
                //vm.agents = vm.agentList;
                getTeamAgents();

            }, function(error) {
                if (error.status === 403)
                    utilCustom.toaster($filter('translate')('login.authFailure') + "agent list.");
            });

            function ayncAgentList(agentList) {
                vm.agentList = agentList;
                parseAgentList();
                return vm.agentList;

            }


            var promise = new Promise(function(resolve, error) {

                resolve(syncOtherList())
            });
            promise.then(function(resolve) {

            })
        }

        function clearSearchTerm() {
            vm.searchTerm = '';
        };

        var team = window.localStorage.getItem('currentTeam');
        if (team == "undefined" || team == undefined) {
            if (!MicroserviceFactory.APAdmin()) {
                vm.agents = [];
            } else {
                iniIt();
            }
        } else {
            iniIt();
        }

        function getTeamAgents() {
            var team = window.localStorage.getItem('currentTeam');
            if (team == "undefined" || team == undefined) {
                if (!MicroserviceFactory.APAdmin()) {
                    vm.agents = [];
                } else {
                    vm.agentList.forEach(function(agnt) {
                        vm.agents.push(agnt);
                    })
                }
            } else {
                team = JSON.parse(team);
                vm.agents = [];
                angular.forEach(vm.agentList, function(agent, index) {
                    if (team.id == agent.teamId) {
                        vm.agents.push(agent);
                    }
                });
            }
        }

        $scope.$on('CHANGE_TEAM', function() {
            vm.agents = null;
            getTeamAgents();
        });

        function syncOtherList() {
            groupService.list().then(function(response) {
                if (response.resourceGroup)
                    vm.groupList = response.resourceGroup;
                else
                    vm.groupList = response;

            }, function(error) {
                console.log(error);
            });
            skillService.list().then(function(response) {
                if (response.skill)
                    vm.skillList = response.skill;
                else
                    vm.skillList = response;

            }, function(error) {
                console.log(error);
            });
            teamService.list().then(function(response) {
                if (response.team)
                    vm.teamList = response.team;

                else
                    vm.teamList = response;
                var userID = window.localStorage.getItem('username') ? window.localStorage.getItem('username') : window.sessionStorage.getItem('username');
                if (userID) {
                    vm.teams = [];
                    if (!MicroserviceFactory.APAdmin()) {
                        agentService.getAgentTeam({ id: userID }).then(function(response) {
                            var teamsAgent = [];
                            teamsAgent = response;


                            angular.forEach(teamsAgent, function(value, key) {
                                angular.forEach(vm.teamList, function(val, ky) {
                                    if (value.id == val.teamId) {
                                        vm.teams.push(val);
                                    }
                                })
                            })
                        }, function(error) {

                        })
                    } else {
                        vm.teams = vm.teamList;
                    }

                } else {
                    vm.teams = vm.teamList;
                }

            }, function(error) {
                console.log(error);
            });
        }

        function searchAgent(agent) {
            if (!vm.search)
                return true;
            return angular.lowercase(agent.userID).indexOf(vm.search) >= 0 || angular.lowercase(agent.firstName).indexOf(vm.search) >= 0 || angular.lowercase(agent.lastName).indexOf(vm.search) >= 0;

        }


        function searchResultClick(item) {
            console.log(item);
        }

        function toggleInArraySkill(item, array) {

            var arraySkillId = _.map(array, 'skillId');
            var sk = item.skillId.toString();
            if (arraySkillId.indexOf(sk) == -1) {

                if (!_.find(array, { skillId: item.skillId.toString() }))
                    array.push({ skillId: item.skillId.toString(), skillName: item.skillName, competencelevel: 5 });
            } else {
                array.splice(arraySkillId.indexOf(sk), 1);
            }
        }

        function existsSkill(item, list) {
            var listId = _.map(list, 'skillId');

            return listId.indexOf(item.toString()) > -1;
        }

        function stopPropagation(ev) {
            ev.stopPropagation();
        }

        function findSkill(skill) {
            if (skill) {
                var agentFound = _.find(vm.agentList, { userID: skill.agent });

            }
            if (agentFound) {

                var fou = _.find(agentFound.skills, { skillId: skill.id.toString() });
                return fou
            }

        }

        function selectedSkill(skillChip) {
            console.log(skillChip);
        }

        function parseAgentList(agentList) {
            angular.forEach(vm.agentList, function(value, key) {
                if (key === vm.agentList.length - 1) {
                    utilCustom.hideToaster();
                }
                var skills = [];
                angular.forEach(value.skillMap.skillCompetency, function(va, k) {
                    var skillId = va.skillNameUriPair.refURL.split('/');
                    var skill = {
                        competencelevel: va.competencelevel,
                        skillId: skillId[skillId.length - 1],
                        skillName: va.skillNameUriPair['@name'],
                        refURL: va.refURL
                    };
                    skills.push(skill);
                });
                if (value.resourceGroup) {
                    var group = value.resourceGroup.refURL.split('/');
                    vm.agentList[key].groupId = Number(group[group.length - 1]);
                }
                if (value.team) {
                    var team = value.team.refURL.split('/');
                    vm.agentList[key].teamId = Number(team[team.length - 1]);
                }
                vm.agentList[key].skills = skills;
                if (value.profileExists)
                    vm.agentList[key].avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(value.userID) + '.jpg?timestamp=' + new Date().getTime();
                else
                    vm.agentList[key].avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();


            });
        }

        vm.selectUserIndex = function(index) {
            if (vm.selectedUserIndex !== index) {
                vm.selectedUserIndex = index;
            } else {
                vm.selectedUserIndex = undefined;
            }
        };
        vm.getCheckedUsers = function() {
            return $filter('filter')(vm.agentList, { checked: true });
        };
        vm.skillName = function(id) {
            return _.result(_.find(vm.skillList, { id: Number(id) }), 'name');
        };
        vm.groupName = function(id) {
            return _.result(_.find(vm.groupList, { id: Number(id) }), 'name');
        };
        vm.teamName = function(id) {
            return _.result(_.find(vm.teamList, { teamId: Number(id) }), 'teamname');
        };
        vm.allChecked = function() {

            return vm.getCheckedUsers().length == vm.agentList.length;
        };
        vm.checkAll = function(value) {

            angular.forEach(vm.agentList, function(bu) {
                bu.checked = value;
            })
        };
        vm.getCheckedIds = function() {
            return _.pluck(vm.getCheckedUsers(), 'id');
        };

        function dialogShowDetail(agent, e) {
            $mdDialog.show({
                controller: 'AgentUploadPicController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/agent/upload.html',
                parent: angular.element($document.body),
                targetEvent: e,
                clickOutsideToClose: true,
                locals: {
                    userID: agent.userID,
                    agent: agent
                }
            }).then(function(agentData) {
                if (agentData) {
                    var ind = _.findIndex(vm.agents, { userID: agentData.userID });
                    var indAge = _.findIndex(vm.agentList, { userID: agentData.userID });
                    if (ind != -1) {
                        if (agentData.message == 'upload') {
                            vm.agentList[indAge].avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(agentData.userID) + '.jpg?tiagentData.userIDmestamp=' + new Date().getTime();
                            vm.agents[ind].avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(agentData.userID) + '.jpg?timestamp=' + new Date().getTime();
                        } else if (agentData.message == 'delete') {
                            vm.agentList[indAge].avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();
                            vm.agents[ind].avatar = '/assets1/images/avatars/profile.jpg?timestamp=' + new Date().getTime();
                        }
                    }
                }


            })
        }

        function selectedAgent(agent) {
            vm.agent = agent;
        }

        function saveAgent(agent) {
            var params = angular.copy(agent);
            var tUrl = '';
            if (params.teamId) {
                if (params.team) {
                    tUrl = params.team.refURL.split('/');
                    tUrl[tUrl.length - 1] = params.teamId;
                    params.team.refURL = tUrl.join('/');
                    params.team['@name'] = vm.teamName(params.teamId);
                } else {
                    tUrl = agent.self.replace('resource', 'team').split('/');
                    tUrl[tUrl.length - 1] = params.teamId;
                    params.team = { '@name': vm.teamName(params.teamId), refURL: tUrl.join('/') };

                }

            }
            if (params.groupId) {
                if (params.resourceGroup) {
                    tUrl = params.resourceGroup.refURL.split('/');
                    tUrl[tUrl.length - 1] = params.groupId;
                    params.resourceGroup.refURL = tUrl.join('/');
                    params.resourceGroup['@name'] = vm.groupName(params.groupId);
                } else {
                    tUrl = agent.self.replace('resource', 'resourceGroup').split('/');
                    tUrl[tUrl.length - 1] = params.groupId;
                    params.resourceGroup = { '@name': vm.groupName(params.groupId), refURL: tUrl.join('/') };
                }

            }
            if (params.skills) {
                if (params.skillMap) {
                    var skillCompe = [];
                    params.skillMap.skillCompetency = [];
                    angular.forEach(params.skills, function(va, ke) {
                        tUrl = agent.self.replace('resource', 'skill').split('/');
                        tUrl[tUrl.length - 1] = va.skillId;
                        var skillCompetency = {
                            competencelevel: va.competencelevel,
                            skillNameUriPair: { '@name': va.skillName, refURL: tUrl.join('/') }
                        };
                        params.skillMap.skillCompetency.push(skillCompetency);
                    })
                }
            }

            var pam = {
                firstName: params.firstName,
                lastName: params.lastName,
                userID: params.userID,
                extension: params.extension,
                alias: params.alias,
                primarySupervisorOf: [],
                secondarySupervisorOf: [],
                self: params.self,
                type: params.type,
                skillMap: params.skillMap,
                resourceGroup: params.resourceGroup,
                team: params.team,
                autoAvailable: params.autoAvailable
            };
            utilCustom.toasterLoading();
            agentService.update({ agent: pam, id: agent.userID }).then(function(response) {
                if ($rootScope.agentList) {
                    var indx = _.findIndex($rootScope.agentList, { userID: agent.userID });
                    if (indx != -1) {
                        $rootScope.agentList[indx] = agent;
                        vm.agents[indx] = agent;
                        getTeamAgents();
                    }
                }
                utilCustom.toaster($filter('translate')('agent.agent') + $filter('translate')('data.updated'));
            }, function(error) {
                utilCustom.toaster($filter('translate')('data.updateError') + $filter('translate')('agent.agent'));
            })
        }

        function addNewSkill() {
            var newSkill = vm.agent.skillId.pop();
            var foundSkill = _.find(skillList, { name: newSkill });
            if (foundSkill === undefined) {
                var count = skillList.length + 1;
                skillList.push({ id: count, name: newSkill });
                vm.agent.skillId.push({ id: count });

            }
        }

        function skillQuerySearch(query) {
            return query ? vm.skillList.filter(createFilterFor(query)) : [];
        }

        function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(item) {
                return angular.lowercase(item.skillName).indexOf(lowercaseQuery) >= 0;
            };
        }

        function filterSkill(label) {
            if (!vm.skillSearchText || vm.skillSearchText === '') {
                return true;
            }

            return angular.lowercase(label.skillName).indexOf(angular.lowercase(vm.skillSearchText)) >= 0;
        }

        function agentEditForm(agent, skillList, groupList, e) {
            $mdDialog.show({
                controller: 'AgentEditController',
                controllerAs: 'vm',
                templateUrl: 'app/TAM/agent/upload.html',
                parent: angular.element($document.body),
                targetEvent: e,
                clickOutsideToClose: true,
                locals: {
                    agent: agent,
                    skillList: skillList,
                    groupList: groupList
                }
            }).then(function(agentData) {
                var ind = _.findIndex(vm.agentList, { userId: agentData.userId });
                vm.agentList[ind] = agentData;
            })
        }

    }


})();
