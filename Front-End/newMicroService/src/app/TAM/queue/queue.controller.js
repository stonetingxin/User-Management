/**
 * Created by mumar on 1/11/2016.
 */

(function() {
    'use strict';
    angular
        .module('app.queue')
        .controller('QueueController', queueListCtrl)
        .controller('RightCtrl', RightCtrl);


    function queueListCtrl(MicroserviceFactory, $scope, $filter, queueService, $rootScope, agentService, groupService, skillService, msUtils, $mdSidenav, $log, $mdDialog, $document, utilCustom) {
        var vm = this;

        //Data

        vm.sortableListOptions = {
            axis: 'x',
            delay: 75,
            distance: 7,
            items: '> .list-wrapper',
            handle: '.list-header',
            placeholder: 'list-wrapper list-sortable-placeholder',
            tolerance: 'pointer',
            start: function(event, ui) {
                var width = ui.item[0].children[0].clientWidth;
                var height = ui.item[0].children[0].clientHeight;
                ui.placeholder.css({
                    'min-width': width + 'px',
                    'width': width + 'px',
                    'height': height + 'px'
                });
            }
        };
        vm.sortableCardOptions = {
            appendTo: 'body',
            connectWith: '.list-cards',
            delay: 75,
            distance: 7,
            forceHelperSize: true,
            forcePlaceholderSize: true,
            handle: msUtils.isMobile() ? '.list-card-sort-handle' : false,
            helper: function(event, el) {
                return el.clone().addClass('list-card-sort-helper');
            },
            stop: function(e, ui) {
                utilCustom.toasterLoading();
                var droptarget = ui.item.sortable.droptarget;
                var queueId = undefined;
                if (droptarget) {
                    queueId = Number(droptarget.children("", "id").attr("id"));
                }
                if (queueId) {
                    var agentReplace = ui.item.sortable.model;
                    /*var dropId = ui.item.sortable.droptargetModel[0];
                     if(agentReplace.queueId==dropId){
                     dropId = ui.item.sortable.droptargetModel[1];
                     }
                     var dragId = ui.item.sortable.sourceModel[0];
                     if(!dragId){dragId={queueId:-15}}*/
                    var queue = _.find(vm.queues, { id: queueId });
                    var orderedResources = [];
                    angular.forEach(ui.item.sortable.droptargetModel, function(value, key) {
                        var name = value.firstName;
                        if (value.lastName) {
                            name = name == '' ? value.lastName : ' ' + value.lastName;
                        }
                        orderedResources.push({ '@name': name, refURL: value.self });
                    });
                    queue.poolSpecificInfo.resourceGroup.orderedResources = orderedResources;
                    if (agentReplace.queueId != queueId) {
                        // var agent = ui.item.sortable.model;
                        agentReplace.resourceGroup = queue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair;
                        agentService.update({ agent: agentReplace, id: agentReplace.userID }).then(function(respone) {
                            utilCustom.toaster($filter('translate')('csq.agentAddedInGroup') + " \'" + agentReplace.resourceGroup['@name'] + "\'");
                            agentReplace.queueId = queueId;
                            updateQueue(queue);
                        }, function(error) {
                            console.log(error);
                        })
                    } else {
                        updateQueue(queue);
                    }
                } else {
                    utilCustom.toaster($filter('translate')('csq.dropAgent'));
                }





            },
            placeholder: 'list-card card-sortable-placeholder',
            tolerance: 'pointer',
            scroll: true,
            sort: function(event, ui) {
                var listContentEl = ui.placeholder.closest('.list-content');
                var boardContentEl = ui.placeholder.closest('#board');

                if (listContentEl) {
                    var listContentElHeight = listContentEl[0].clientHeight,
                        listContentElScrollHeight = listContentEl[0].scrollHeight;

                    if (listContentElHeight !== listContentElScrollHeight) {
                        var itemTop = ui.position.top,
                            itemBottom = itemTop + ui.item.height(),
                            listTop = listContentEl.offset().top,
                            listBottom = listTop + listContentElHeight;

                        if (itemTop < listTop + 25) {
                            listContentEl.scrollTop(listContentEl.scrollTop() - 25);
                        }

                        if (itemBottom > listBottom - 25) {
                            listContentEl.scrollTop(listContentEl.scrollTop() + 25);
                        }
                    }
                }

                if (boardContentEl) {
                    var boardContentElWidth = boardContentEl[0].clientWidth;
                    var boardContentElScrollWidth = boardContentEl[0].scrollWidth;

                    if (boardContentElWidth !== boardContentElScrollWidth) {
                        var itemLeft = ui.position.left,
                            itemRight = itemLeft + ui.item.width(),
                            boardLeft = boardContentEl.offset().left,
                            boardRight = boardLeft + boardContentElWidth;

                        if (itemLeft < boardLeft + 25) {
                            boardContentEl.scrollLeft(boardContentEl.scrollLeft() - 25);
                        }

                        if (itemRight > boardRight) {
                            boardContentEl.scrollLeft(boardContentEl.scrollLeft() + 25);
                        }
                    }
                }
            }
        };

        vm.openCreateQueueDialog = openCreateQueueDialog;
        vm.openEditQueueDialog = openEditQueueDialog;

        //Method
        vm.getSkillName = skillName;
        vm.getAgentName = agentName;
        vm.deleteCard = deleteCard;
        vm.deleteQueue = deleteQueue;
        vm.updateQueue = updateQueue;
        vm.filterCsq = filterCsq;
        vm.init = init;
        vm.pushQueues = pushQueues;

        vm.queueList = [];
        vm.agentList = [];
        vm.queues = [];
        vm.skillList = [];

        function init() {
            var params = {};
            var team = window.localStorage.getItem('currentTeam');
            if (team == "undefined" || team == undefined) {
                vm.getAllQueue = undefined;
                if (!MicroserviceFactory.APAdmin()) {
                    params = { teamId: 'noTeam' }
                }
                console.log('team Null');
            } else {
                team = JSON.parse(team);
                params = { teamId: team.id };

                // To get All queue
                queueService.list('').then(function(response) {
                    vm.getAllQueue = response;

                }, function(error) {})
            }
            queueService.list(params).then(function(respose) {
                vm.queueList = respose;
                vm.queues = [];
                new Promise(function(resolve, error) {
                    resolve(getList(vm.queueList))
                });

                // vm.queues = vm.queueList;
                // angular.forEach(vm.queues,function(value,key){
                //   if(value.queueType!=("VOICE")){
                //     _.remove(vm.queues,function(va){
                //       return va.queueType===value.queueType;
                //     })
                //   }
                //   if(key===vm.queues.length-1)utilCustom.hideToaster();
                //   if(vm.queues==0){utilCustom.toaster($filter('translate')('csq.noQueue'))}
                // });

            }, function(error) {
                console.log(error);
            });



        }
        vm.team = window.localStorage.getItem('currentTeam');
        if ((vm.team == "undefined" || vm.team == undefined)) {
          if(!MicroserviceFactory.APAdmin())
              vm.agents = [];
          else{
            getAgents();
            getSkills();
            getGroups();
          }
        } else {
            getAgents();
            getSkills();
            getGroups();
        }

        function filterCsq(queue) {
            if (!vm.queueFiter)
                return true;
            return queue.resourcePoolType.indexOf(vm.queueFiter) >= 0;
        }

        function setTeamName(teamName) {
            var name = "all";
            if (!teamName) {
                var team = window.localStorage.getItem('currentTeam');
                if (team == undefined || team == "undefined") {
                    if (!MicroserviceFactory.APAdmin()) {
                        vm.agents = [];
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

        function getAgents() {

            utilCustom.toasterLoading();
            if (!$rootScope.agentList) {
                agentService.list('').then(function(reponseAgent) {
                    vm.agentList = [];
                    vm.agents = reponseAgent.resource;
                    vm.agentList = reponseAgent.resource;
                    //  getTeamAgents();
                    init();

                    angular.forEach(vm.agentList, function(value, key) {

                        if (value.profileExists)
                            vm.agentList[key].avatar = window.appBaseUrl + '/base/assets1/images/agents/' + angular.lowercase(value.userID) + '.jpg';
                        else
                            vm.agentList[key].avatar = '/assets1/images/avatars/profile.jpg';
                    });


                });
            } else {
                vm.agentList = [];
                vm.agents = $rootScope.agentList;
                vm.agentList = $rootScope.agentList;
                //  getTeamAgents();
                init();
            }

        }

        function getSkills() {
            skillService.list('').then(function(response) {
                if (response.skill)
                    vm.skillList = response.skill;
                else
                    vm.skillList = response;

            }, function(error) { console.log(error); });
        }

        function getGroups() {
            groupService.list('').then(function(response) {
                if (response.resourceGroup) {
                    vm.groupList = response.resourceGroup;
                } else
                    vm.groupList = response;
            }, function(error) {
                console.log(error);
            })
        }

        function getTeamAgents() {
            var team = window.localStorage.getItem('currentTeam');
            if (team == "undefined" || team == undefined) {
                vm.agentList = [];
                //console.log('team Null');
                vm.agentList = vm.agents;
            } else {
                team = JSON.parse(team);
                vm.agentList = [];
                angular.forEach(vm.agents, function(agent, index) {
                    if (agent.team) {
                        var teamSpl = agent.team.refURL.split('/');
                        agent.teamId = Number(teamSpl[teamSpl.length - 1]);
                    }
                    if (team.id == agent.teamId) {
                        vm.agentList.push(agent);
                    }
                });
            }
        }

        function getList(queueList) {
            var queues = angular.copy(queueList);
            forWithDelay(0, queues.length, vm.pushQueues, queues, 0);

        }
        $scope.toggleRight = buildToggler('right');
        $scope.isOpenRight = function() {
            return $mdSidenav('right').isOpen('lg');
        };
        $scope.$on('CHANGE_TEAM', function() {
            init();
        });

        function updateQueue(queue) {
            utilCustom.toasterLoading();
            queueService.update({ queue: queue, id: queue.id }).then(function(reponse) {
                utilCustom.toaster($filter('translate')('csq.csq') + ' ' + $filter('translate')('data.updated'));
            }, function(error) {
                console.log(error);
            });
        }

        function forWithDelay(i, length, fn, response, delay) {
            setTimeout(function() {
                // console.log(i);
                fn(response[i]);
                i = i + 1;
                if (length - 1 == i) {
                    utilCustom.hideToaster();
                }
                if (i < length) {
                    forWithDelay(i, length, fn, response, delay);
                }
            }, delay);
        }

        function pushQueues(queue) {
            if (queue.queueType == ("VOICE")) {
                if (queue.resourcePoolType == 'SKILL_GROUP') {
                    queue['skillId'] = [];
                    if (queue.poolSpecificInfo.skillGroup) {
                        var skills = queue.poolSpecificInfo.skillGroup.skillCompetency;
                        angular.forEach(skills, function(val, ky) {
                            var skill = val;
                            queue.skillId.push({ competencelevel: skill.competencelevel, name: skill.skillNameUriPair['@name'] })
                        })
                    }
                    // queues[key].skillId = skillId;
                } else if (queue.resourcePoolType == 'RESOURCE_GROUP') {
                    queue['groupId'] = [];
                    if (queue.poolSpecificInfo.resourceGroup) {
                        var groups = queue.poolSpecificInfo.resourceGroup.orderedResources;
                        angular.forEach(groups, function(val, ky) {
                            var group = val;
                            var userId = group.refURL.split('/');
                            userId = userId[userId.length - 1];
                            var agents = _.find(vm.agentList, { userID: userId });
                            if (agents) {
                                agents.queueId = queue.id;
                                queue.groupId.push(agents)
                            }


                        })
                    }
                    //queues[key].groupId = groupId;
                }
                vm.queues.push(queue);
            }
            //  vm.queues.push(queue);

        }

        function buildToggler(navID) {
            return function() {
                $mdSidenav(navID)
                    .toggle()
                    .then(function() {
                        $log.debug("toggle " + navID + " is done");
                    });
            }
        }

        function skillName(id) {

            return _.result(_.find(vm.skillList, { id: id }), 'name');
        }

        function agentName(id) {
            return _.find(vm.agentList, { userId: id });
        }

        function deleteQueue(eent, queueId) {
            utilCustom.toasterConfirm().then(function(response) {
                if (response == 'ok' || response) {
                    utilCustom.toasterLoading();
                    queueService.delete({ id: queueId }).then(function(res) {
                        utilCustom.toaster($filter('translate')('csq.csq') + ' ' + $filter('translate')('data.deleted'));
                        _.remove(vm.queues, function(queue) {
                            return queue.id === queueId;
                        });
                        _.remove(vm.queueList, function(queue) {
                            return queue.id === queueId;
                        });
                        if (vm.getAllQueue) {
                            _.remove(vm.getAllQueue, function(queue) {
                                return queue.id === queueId;
                            });
                        }
                    }, function(error) {
                        utilCustom.toaster($filter('translate')('data.deleteError') + $filter('translate')('csq.csq'));
                    });
                } else {
                    utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
                }
            });


        }

        function openCreateQueueDialog(ev) {
            $mdDialog.show({
                templateUrl: 'app/TAM/queue/dialog/createQueue.html',
                controller: "CreateQueueController",
                controllerAs: "vm",
                parent: $document.find('#scrumboard'),
                targetEvent: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                locals: {
                    groupList: vm.groupList,
                    queueList: vm.getAllQueue ? vm.getAllQueue : vm.queueList,
                    team: setTeamName()
                }
            }).then(function(response) {

                if (response) {
                    if (vm.getAllQueue) {
                        vm.getAllQueue.push(response);
                    }
                    vm.queueList.push(response);
                    pushQueues(response);
                    // vm.queues=getList(vm.queueList);
                }

            })

        }

        function deleteCard(Id, queueId, type) {
            utilCustom.toasterConfirm().then(function(response) {
                if (response == 'ok' || response) {
                    var indx = _.findIndex(vm.queues, { id: queueId });
                    utilCustom.toasterLoading();
                    if (type === 'group') {
                        var agent = Id;
                        agent.resourceGroup = null;

                        agentService.update({ agent: agent, id: agent.userID }).then(function(response) {
                            utilCustom.toaster($filter('translate')('csq.csq') + ' ' + $filter('translate')('data.updated'));
                            _.remove(vm.queues[indx].groupId, function(id) {
                                return id === Id;

                            });
                            _.remove(vm.queueList[indx].groupId, function(id) {
                                return id === Id;

                            });
                            // init();
                        }, function(error) {

                        });

                    } else {
                        _.remove(vm.queues[indx].skillId, function(id) {
                            return id === Id;
                        });
                        _.remove(vm.queues[indx].poolSpecificInfo.skillGroup.skillCompetency, function(id) {
                            return id.skillNameUriPair['@name'] === Id.name;
                        });
                        updateQueue(vm.queues[indx]);
                    }
                } else {
                    utilCustom.toaster($filter('translate')('generic.noOptionSelected'));
                }
            });
        }

        function openEditQueueDialog(ev, queue) {
            $mdDialog.show({
                templateUrl: 'app/TAM/queue/dialog/editQueue.html',
                controller: "EditQueueController",
                controllerAs: "vm",
                parent: $document.find('#scrumboard'),
                targetEvent: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                locals: {
                    groupList: vm.groupList,
                    queue: queue,
                    queueList: vm.getAllQueue ? vm.getAllQueue : vm.queueList,
                    agentList: vm.agents
                }
            }).then(function(response) {
                if (response) {
                    if (vm.getAllQueue) {
                        vm.getAllQueue[_.findIndex(vm.getAllQueue, { id: response.id })] = response;
                    }
                    // vm.queues[_.findIndex(vm.queues,{id:response.id})]= response;
                    vm.queueList[_.findIndex(vm.queueList, { id: response.id })] = response;
                    // vm.queues=getList(vm.queueList);
                }



            })

        }
    }

    function RightCtrl($scope, $mdComponentRegistry, $stateParams) {
        var vm = this;
        $mdComponentRegistry
            .when('right')
            .then(function(sideNav) {

                $scope.isOpen = $stateParams.id;
                vm.toggle = angular.bind(sideNav, sideNav.toggle);

            });
    }
})();
