/**
 * Created by mumar on 1/11/2016.
 */
(function() {
    'use strict';

    angular
        .module('app.queue')
        .controller('msSbAddSkillController', msSbAddSkillController)
        .directive('msSbAddSkill', msSbAddSkill);

    /** @ngInject */
    function msSbAddSkillController($scope, $timeout, utilCustom, api, $filter, agentService, queueService) {
        var vm = this;
        // Methods
        vm.addNewSkill = addNewSkill;
        vm.findName = findName;
        vm.getIds = getIds;


        var formData = { id: '', skill_level: '' };
        vm.newCardName = '';
        vm.msQueueId = $scope.msQueue.id;
        vm.msQueue = $scope.msQueue;
        vm.skillIds = [];
        vm.ids = [];
        vm.skillList = $scope.msSkillList;
        vm.groupList = $scope.msGroupList;
        vm.init = $scope.msInitFunction;
        vm.data = {};
        vm.clearSearchTerm = clearSearchTerm;


        function clearSearchTerm() {
            vm.searchTerm = '';
        };

        // vm.getIds();
        function getIds() {
            vm.skillList = [];
            vm.agentList = [];
            vm.formData = { competencelevel: 5 };
            vm.skillList[vm.msQueueId] = $scope.msSkillList;
            vm.agentList[vm.msQueueId] = $scope.msAgentList;
            vm.skillIds[vm.msQueueId] = [];
            vm.ids[vm.msQueueId] = [];
            if (vm.msQueue.resourcePoolType === "SKILL_GROUP") {

                vm.ids[vm.msQueueId] = _.difference(_.map(vm.skillList[vm.msQueueId], 'skillName'), _.map(vm.msQueue.skillId, 'name'));

            }

            if (vm.msQueue.resourcePoolType === "RESOURCE_GROUP") {
                var agentList = angular.copy(vm.agentList).pop();
                var agentGroupList = [];
                var queueGroup = undefined;
                if (vm.msQueue.poolSpecificInfo.resourceGroup) {
                    if (vm.msQueue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair)
                        queueGroup = vm.msQueue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair['@name'];
                }


                angular.forEach(agentList, function(value, key) {
                    if (value.resourceGroup) {
                        var agentGroup = value.resourceGroup['@name'];
                        if (agentGroup === queueGroup && queueGroup != undefined) {
                            agentGroupList.push(value);
                        }
                    } else {
                        agentGroupList.push(value);
                    }

                });

                if (agentGroupList.length > 0) {
                    var agentListName = _.difference(_.map(vm.agentList[vm.msQueueId], 'userID'), _.map(vm.msQueue.groupId, 'userID'));
                    angular.forEach(agentListName, function(value, key) {
                        var found = _.find(agentGroupList, { userID: value });
                        if (found)
                            vm.ids[vm.msQueueId].push(found)
                    })
                }




            }



        };

        function findName(id) {
            if (vm.msQueue.resourcePoolType === "SKILL_GROUP")
                return id;
            if (vm.msQueue.resourcePoolType === "RESOURCE_GROUP")
                return id;
        };
        /////


        /**
         * Add New Card
         */
        function addNewSkill() {
            if (vm.formData.id === '' || vm.formData.skill_level === '') {
                return;
            }
            vm.searchTerm = '';
            utilCustom.toasterLoading();
            if (vm.msQueue.resourcePoolType === "SKILL_GROUP") {

                var skillList = angular.copy(vm.skillList).pop();
                var skillGroup = vm.formData;
                if (vm.msQueue.skillId === undefined)
                    vm.msQueue.skillId = [];
                var skill = _.find(skillList, { skillName: skillGroup.name });
                var addSkill = { competencelevel: skillGroup.competencelevel, skillNameUriPair: { '@name': skill.skillName, refURL: skill.self } };
                var skillCompetence = vm.msQueue.poolSpecificInfo;
                if (skillCompetence) {
                    if (skillCompetence.skillGroup) {
                        if (!skillCompetence.skillGroup.skillCompetency) {
                            vm.msQueue.poolSpecificInfo.skillGroup.skillCompetency = [];

                        }
                    } else {
                        vm.msQueue.poolSpecificInfo = { skillGroup: { skillCompetency: [] } }
                    }
                    vm.msQueue.poolSpecificInfo.skillGroup.skillCompetency.push(addSkill);

                    queueService.update({ queue: vm.msQueue, id: vm.msQueue.id }).then(function(response) {
                        $scope.closeForm();
                        vm.msQueue.skillId.push(vm.formData);
                        vm.formData = '';
                        utilCustom.toaster($filter('translate')('csq.skillAddedInGroup'));
                    }, function(error) {
                        console.log(error);
                    })
                }

            }

            if (vm.msQueue.resourcePoolType === "RESOURCE_GROUP") {

                if (vm.msQueue.groupId === undefined) {
                    vm.msQueue.groupId = [];
                }
                var queueGroup = undefined;
                if (vm.msQueue.poolSpecificInfo.resourceGroup) {
                    if (vm.msQueue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair)
                        queueGroup = vm.msQueue.poolSpecificInfo.resourceGroup.resourceGroupNameUriPair['@name'];
                }
                var agent = vm.formData.name;
                if (queueGroup) {
                    var group = _.find(vm.groupList, { name: queueGroup });
                    if (group) {
                        var agentUrl = group.self.split('/');
                        agentUrl[agentUrl.length - 1] = group.id;
                        agent.resourceGroup = { '@name': group.name, 'refURL': agentUrl.join('/') };
                    }

                }
                agentService.update({ agent: agent, id: agent.userID }).then(function(response) {
                    vm.formData = '';
                    utilCustom.toaster($filter('translate')('csq.agentAddedInGroup'));
                    var found = _.findIndex($scope.msAgentList, { userID: agent.userID });
                    if (found > -1) {
                        agent.queueId = vm.msQueue.id;
                        $scope.msAgentList[found] = agent;
                        vm.msQueue.groupId.push(agent);
                        $scope.closeForm();
                        vm.formData = '';

                    }


                }, function(error) {
                    console.log(error);
                });


            }



            $timeout(function() {
                $scope.scrollListContentBottom();
            });


            //vm.formData.skill_level = '';
        }
    }

    /** @ngInject */
    function msSbAddSkill($document, $compile, $timeout, $filter) {
        return {
            restrict: 'A',
            controller: 'msSbAddSkillController',
            controllerAs: 'vm',
            // priority:9999,
            scope: {
                msQueueId: '=',
                msQueue: '=',
                msAgentList: '=',
                msSkillList: '=',
                msGroupList: '=',
                msInitFunction: '=',
                ids: "="
            },
            link: function(scope, iElement, attrs) {
                scope.closeForm = closeForm;
                scope.scrollListContentBottom = scrollListContentBottom;
                var translateValue = $filter('translate')('generic.selectOne');

                var form = '<md-divider></md-divider><div class="addSkillAndGroup">' +
                    '<form ng-submit="vm.addNewSkill()" name="addingInQueue"  layout="column">\n\n   ' +
                    ' <md-input-container  flex md-no-float>\n        ' +
                    '<md-select required ng-model="vm.formData.name" md-on-close="vm.clearSearchTerm" name="skill" placeholder="' + "{{'generic.selectOne'|translate}}" + '">' +
                    ' <md-select-header class="demo-select-header" >' +
                    '<input ng-model="vm.searchTerm" id="searchText" md-autofocus onclick="event.stopPropagation()" onkeydown="event.stopPropagation()" type="search" placeholder="' + "{{'generic.search'|translate}} ..." + '" class="demo-header-searchbox md-text">' +
                    '</md-select-header>' +
                    '<md-optgroup label="vegetables">' +
                    '<md-option ng-value="sk"  ng-repeat="sk in vm.ids[vm.msQueueId] | filter : vm.searchTerm">{{vm.msQueue.resourcePoolType==="SKILL_GROUP"?sk:sk.firstName+" "+sk.lastName }}' +
                    '</md-option>' +
                    '</md-optgroup>' +
                    '</md-select>\n    </md-input-container>\n\n ' +
                    ' <md-input-container flex md-no-float>\n       ' +
                    ' <div ng-messages="addingInQueue.skill.$error" ng-show="addingInQueue.skill.$touched" role="alert">' +
                    '<div ng-message="required">' +
                    '<span translate="generic.selectOne"></span>' +
                    '</div>' +
                    '</div>';
                if (scope.msQueue.resourcePoolType === "SKILL_GROUP")
                    form = form + '<md-slider md-discrete="" flex="60" min="1" max="10" ng-model="vm.formData.competencelevel" aria-label="competencelevel" class="">  </md-slider>';
                if (scope.msQueue.resourcePoolType === "RESOURCE_GROUP")
                    form = form + ' <input placeholder="Competence Level" type="hidden" autocomplete="off"\n       ' +
                    '        ng-model="vm.formData.competencelevel" >\n   ';

                form = form + ' </md-input-container>\n\n   ' +
                    '    <div layout="row" layout-align="space-between center">\n       ' +
                    ' <md-button type="submit"\n   ng-disabled="addingInQueue.$invalid || addingInQueue.$pristine"                class="add-button md-accent md-raised"\n                   aria-label="add" >\n            <span translate="generic.add"></span>\n        </md-button>\n        <md-button ng-click="closeForm()" class="cancel-button md-icon-button"\n                   aria-label="cancel" >\n            <md-icon md-font-icon="icon-close"></md-icon>\n        </md-button>\n    </div>\n\n</form></div>';
                var formEl = '',
                    listContent = iElement.prev();

                /**
                 * Click Event
                 */
                iElement.on('click', function(event) {
                    event.preventDefault();
                    openForm();
                });

                /**
                 * Open Form
                 */
                function openForm() {
                    scope.vm.getIds();
                    iElement.hide();

                    formEl = $compile(form)(scope);

                    listContent.append(formEl);

                    scrollListContentBottom();


                    formEl.find('input').focus();

                    $timeout(function() {
                        $document.on('click', outSideClick);
                    });
                }


                /**
                 * Close Form
                 */
                function closeForm() {
                    formEl.remove();

                    iElement.next().remove();

                    iElement.show();

                    PerfectScrollbar.update(listContent[0]);

                    // Clean
                    $document.off('click', outSideClick);
                    scope.$on('$destroy', function() {
                        $document.off('click', outSideClick);
                    });
                }


                /**
                 * Scroll List to the Bottom
                 */
                function scrollListContentBottom() {
                    listContent[0].scrollTop = listContent[0].scrollHeight;
                }

                /**
                 * Click Outside Event Handler
                 * @param event
                 */
                var outSideClick = function(event) {
                    var isChild = formEl.has(event.target).length > 0;
                    var isSelf = formEl[0] == event.target;
                    var isInside = isChild || isSelf;

                    if (!isInside) {
                        closeForm();
                    }
                }

            }
        };
    }
})();