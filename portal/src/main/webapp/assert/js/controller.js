puma.controller('pumaDashboardController', function ($scope, $http, $interval) {
    $scope.options = {animation: false};

    var refresh = function () {
        $http.get("/a/dashboard?t=" + new Date().getTime()).success(
            function (response) {
                $scope.data = response;
            }
        );
    };
    refresh();

    var interval = $interval(refresh, 5000);

    $scope.$on('$destroy', function () {
        $interval.cancel(interval);
    });
});

puma.controller('pumaMarkDownController', function ($scope, $http, $routeParams, $timeout) {
    $scope.markdown = 'loading...'

    $http.get("/docs/" + $routeParams.file + ".md?t=" + new Date().getTime()).success(
        function (response) {
            $scope.markdown = response;

            $timeout(function () {
                $('pre code').each(function (i, block) {
                    hljs.highlightBlock(block);
                });
            }, 0);
        }
    );
});

puma.controller('pumaClientConfigController', function ($scope, $http) {
    $scope.dto = {};

    $scope.submit = function () {
        var json = {
            clientName: $scope.dto.clientName,
            beginTime: $scope.dto.beginTime ? new Date($scope.dto.beginTime).getTime() : 0
        };

        $http.post('/a/puma-client', json).success(function (response) {
            $.notify('Success!');
        });
    };
});

puma.controller('pumaTaskConfigController', function ($scope, $http) {

    $scope.pumaDto = {};

    $scope.allServers = [];
    $scope.allTables = [];
    $scope.createMode = false;

    $scope.showCreateTables = false;

    (function () {
        $http.get('/a/puma-create/server?t=' + new Date().getTime()).success(function (servers) {
            angular.forEach(servers, function (server) {
                $scope.allServers.push(server);
            });
        });
    })();

    $scope.search = function () {
        var url = '/a/puma/search?database=' + $scope.pumaDto.database + '&t=' + new Date().getTime();
        $http.get(url).success(
            function (response) {
                $scope.pumaDto = response;

                $scope.servers = [];
                angular.forEach($scope.pumaDto.serverNames, function (serverName) {
                    var server = {
                        name: serverName,
                        input: buildInput(serverName, $scope.pumaDto.serverNames),
                        output: [],
                        beginTime: setDate($scope.pumaDto.beginTimes[serverName]),
                    };
                    $scope.servers.push(server);
                });

                $scope.findTables();
                if (_.size($scope.servers) === 0) {
                    $scope.add();
                }
            }
        );
    };

    function setDate(date) {
        if (_.isUndefined(date) || _.isNull(date)) {
            return undefined;
        } else {
            return new Date(date).toString();
        }
    }

    $scope.findTables = function () {
        $scope.allTables = [];
        var database = $scope.pumaDto.database;
        var tables = $scope.pumaDto.tables;
        var url = '/a/puma-create/table?database=' + database + '&t=' + new Date().getTime();
        $http.get(url).success(function (allTables) {
            angular.forEach(allTables, function (table) {
                if (_.contains(tables, table)) {
                    $scope.allTables.push({name: table, selected: true});
                } else {
                    $scope.allTables.push({name: table});
                }
            });
        });
    };

    function buildInput(serverName, allServers) {
        var list = [];
        angular.forEach(allServers, function (server) {
            if (server === serverName) {
                list.push({name: server, selected: true});
            } else {
                list.push({name: server});
            }
        });
        return list;
    };

    function parseOutput(servers) {
        var list = [];
        angular.forEach(servers, function (server) {
            list.push(server.output[0].name);
        });
        return list;
    };

    function parseTables(tables) {
        var list = [];
        angular.forEach(tables, function (table) {
            list.push(table.name);
        });
        return list;
    };

    function parseBeginTime(servers) {
        var list = {};
        angular.forEach(servers, function (server) {
            try {
                list[server.output[0].name] = new Date(server.beginTime).getTime();
            } catch (err) {
            }
        });
        return list;
    };

    $scope.submit = function () {
        $scope.pumaDto.serverNames = parseOutput($scope.servers);
        $scope.pumaDto.tables = parseTables($scope.tables);
        $scope.pumaDto.beginTimes = parseBeginTime($scope.servers);

        var json = {
            serverNames: $scope.pumaDto.serverNames,
            tables: $scope.pumaDto.tables,
            database: $scope.pumaDto.database,
            beginTimestamps: $scope.pumaDto.beginTimes,
        };

        $http.post('/a/puma-create', json).success(function (response) {
            $.notify('success');
        });
    };

    $scope.add = function () {
        $scope.servers.push({
            name: '',
            input: buildInput('', $scope.allServers),
            output: []
        });
    };

    $scope.delete = function (server) {
        var index = $scope.servers.indexOf(server);
        $scope.servers.splice(index, 1);
    };

    $scope.checkShowWell = function () {
        return _.size($scope.pumaDto.serverNames) === 0 && !$scope.createMode;
    };

    $scope.checkShowNewServers = function () {
        return $scope.createMode;
    };

    $scope.checkShowOldServers = function () {
        return !$scope.createMode;
    };
});


puma.controller('pumaCheckStatusController', function ($scope, $http) {
    $scope.reset = function () {
        $scope.queryModel = {
            "page": 1
        };
        $scope.query();

    }

    $scope.query = function () {
        $http.get('/a/puma-check?t=' + new Date().getTime(), {
            params: $scope.queryModel
        }).then(
            function (response) {
                $scope.list = response.data.list;
                $scope.pageModel = response.data.page;
            }
        );
    }

    $scope.remove = function (taskName, skipConfirm) {
        if (skipConfirm || confirm('Remove it?')) {
            $http.delete('/a/puma-check/' + encodeURIComponent(taskName)).then(
                function (response) {
                    $scope.list = _.without($scope.list, _.findWhere($scope.list, {taskName: taskName}));
                }
            );
        }
    }

    $scope.reset();
});

puma.controller('pumaCheckConfigController', function ($scope, $http, $location) {
    $scope.templateBase = 'partials/puma-check/model/';

    if ($location.search()['name']) {
        $scope.baseTemplate = 'empty';
        $scope.model = {
            'comparisonProp': {
                'className': 'empty'
            },
            'sourceFetcherProp': {
                'className': 'empty'
            },
            'targetFetcherProp': {
                'className': 'empty'
            },
            'mapperProp': {
                'className': 'empty'
            },
            'targetDsBuilderProp': {
                'className': 'empty'
            },
            'sourceDsBuilderProp': {
                'className': 'empty'
            },
        };

        $http.get('/a/puma-check/' + encodeURIComponent($location.search()['name']) + '?t=' + new Date().getTime()).then(
            function (response) {
                if (response.data) {
                    $scope.baseTemplate = 'base';
                    $scope.model = response.data;
                }
            }
        )
    } else {
        $scope.baseTemplate = 'base';
        $scope.model = {
            'baseInfo': {},
            'comparisonProp': {
                'className': 'FullComparison'
            },
            'sourceFetcherProp': {
                'className': 'UpdateTimeAndIdSourceFetcher'
            },
            'targetFetcherProp': {
                'className': 'SingleLineTargetFetcher'
            },
            'mapperProp': {
                'className': 'DefaultRowMapper'
            },
            'targetDsBuilderProp': {
                'className': 'GroupDataSourceBuilder'
            },
            'sourceDsBuilderProp': {
                'className': 'GroupDataSourceBuilder'
            },
        };
    }

    $scope.create = function () {
        $http.post('/a/puma-check', $scope.model).then(
            function (response) {
                $.notify('Success');
            }
        )
    }
});

puma.controller('pumaSyncConfigController', function ($scope, $http, $location) {

    $scope.template = {
        model: 'partials/puma-sync/model/',
        node: 'partials/puma-sync/model/etl/node.html',
        etl: 'partials/puma-sync/model/etl/',
        extract: 'partials/puma-sync/model/etl/extract/',
        transform: 'partials/puma-sync/model/etl/transform/',
        load: 'partials/puma-sync/model/etl/load/'
    };

    $scope.data = {
        servers: []
    };

    $scope.model = {
        head: {},
        server: {},
        nodes: [],
        init: {},
        ctrl: {},
        info: {}
    };

    $scope.findServers = function (cb) {
        $http.get('/a/puma-sync/servers').then(
            function success(response) {
                cb(null, response.data);
            },
            function failure(response) {
                cb(response.status + ":" + response.statusText);
            }
        );
    };

    /**
     * Add a node to the synchronization task config page by the given type.
     *
     * @param pipeIndex pipeline index.
     * @param type extract, transform or load.
     */
    $scope.createNode = function (pipeIndex, type) {
        var newNodes = [];

        // Copy etl nodes before pipe index.
        for (var i = 0; i != pipeIndex; ++i) {
            newNodes.push($scope.model.nodes[i]);
        }

        // Generate a new etl node.
        var newNode = $scope.genNode(type);
        newNodes.push(newNode);

        // Copy etl nodes after pipe index.
        for (var j = pipeIndex; j != $scope.model.nodes.length; ++j) {
            var node = $scope.model.nodes[i];
            newNodes.push(node);
        }

        $scope.model.nodes = newNodes;
    };

    /**
     * Remove etl node by the pipeline index.
     *
     * @param pipeIndex
     */
    $scope.removeNode = function (pipeIndex) {
        var newNodes = [];

        for (var i = 0; i != $scope.model.nodes.length; ++i) {
            if (pipeIndex != i) {
                newNodes.push($scope.model.nodes[i]);
            }
        }

        $scope.model.nodes = newNodes;
    };

    /**
     * Generate an etl node by the given type with their default configs.
     *
     * @param type extract, transform or load.
     * @returns {*}
     */
    $scope.genNode = function (type) {
        switch (type) {
            case 'extract':
                return {
                    type: 'extract',
                    nodeConfigRef: 'binlog-puma-extract',
                    nodeConfigJson: {
                        concurrent: 1
                    }
                };

            case 'transform':
                return {
                    type: 'transform',
                    nodeConfigRef: 'binlog-to-mysql-transform',
                    nodeConfigJson: {
                        concurrent: 1
                    }
                };

            case 'load':
                return {
                    type: 'load',
                    nodeConfigRef: 'group-ds-sql-load',
                    nodeConfigJson: {
                        concurrent: 1
                    }
                };

            default:
                $.notify('Internal error: unsupported etl node category.');
        }
    };

    $scope.save = function () {
        $scope.create($scope.createCallback);
    };

    $scope.normalize = function (model) {
        return {
            syncTaskHeadDto: model.head,
            syncTaskServerDto: model.server,
            syncTaskNodeDtos: model.nodes,
            syncTaskInitDto: model.init,
            syncTaskCtrlDto: model.ctrl,
            syncTaskInfoDto: model.info
        };
    };

    $scope.create = function (cb) {
        $scope.model.init.inited = false;
        $scope.model.ctrl.ctrlConstant = 'START';
        $scope.model.info.statusConstant = 'PREPARING';

        _.each($scope.model.nodes, function (node, index) {
            node.nodeConfigJson.pipeIndex = index;
        });

        $scope.model = $scope.normalize($scope.model);

        $http.post('/a/puma-sync/create', $scope.model).then(
            function success(response) {
                cb(null, response.data);
            },
            function failure(response) {
                cb(response.status + ":" + response.statusText);
            }
        );
    };

    $scope.createCallback = function (err, result) {
        if (err) {
            $.notify({
                icon: 'glyphicon glyphicon-warning-sign',
                message: sprintf("Creating sync task failure: [%s]", err)
            }, {
                type: "warning"
            });
        } else {
            // Jump to status page after creating.
            $location.url('/puma-sync/status');

            $.notify({
                icon: 'glyphicon glyphicon-ok',
                message: sprintf("Success to create sync task.")
            }, {
                type: "info"
            });
        }
    };

    (function () {
        $scope.createNode(0, 'extract');

        $scope.findServers(function (err, result) {
            if (err) {
                $.notify(
                    {
                        icon: 'glyphicon glyphicon-warning-sign',
                        message: sprintf("Loading available servers failure: [%s]", err)
                    },
                    {
                        type: "warning"
                    }
                );
            } else {
                $scope.data.servers = result;
            }
        })
    })();

    $scope.changeCurrentMapping = function (nodeJosn, index) {
        nodeJosn.maps.forEach(function (item) {
            item.active = false;
        })

        nodeJosn.maps[index].active = true;

        $scope.currentMapping = nodeJosn.maps[index];
    }

    $scope.addColumnMapping = function (currentMapping) {
        if (!currentMapping.maps) {
            currentMapping.maps = [];
        }

        currentMapping.maps.push({});
    }

    $scope.removeColumnMapping = function (currentMapping, index) {
        currentMapping.maps.splice(index, 1)
    }

    $scope.removeMapping = function (nodeJosn) {
        var index = nodeJosn.maps.indexOf($scope.currentMapping);
        if (index >= 0) {
            nodeJosn.maps.splice(index, 1)
        }

        if (nodeJosn.maps.length == 0) {
            $scope.currentMapping = null;
        } else {
            $scope.currentMapping = index == 0 ? nodeJosn.maps[0] : nodeJosn.maps[index - 1];
            $scope.currentMapping.active = true;
        }
    }

    $scope.addMapping = function (nodeJosn) {
        if (!nodeJosn.maps) {
            nodeJosn.maps = [];
        }

        nodeJosn.maps.forEach(function (item) {
            item.active = false;
        })

        var newMapping = {
            'sourceTable': 'new table',
            'targetTable': '',
            'active': true
        };
        nodeJosn.maps.push(newMapping)

        $scope.currentMapping = newMapping;
    }
});

puma.controller('pumaSyncStatusController', function ($scope, $http) {

    $scope.template = {
        ack: 'partials/puma-sync/model/ack/'
    };

    $scope.data = {
        tasks: []
    };

    $scope.normalize = function (task) {
        return {
            taskId: task.taskId,
            head: task.syncTaskHeadDto,
            server: task.syncTaskServerDto,
            nodes: task.syncTaskNodeDtos,
            init: task.syncTaskInitDto,
            ctrl: task.syncTaskCtrlDto,
            info: task.syncTaskInfoDto
        };
    };

    /**
     * Find sync tasks information, including the following things:
     * 1. Task name.
     * 2. Server host on which the task running.
     *
     * @param cb
     */
    $scope.findTasks = function (cb) {
        $http.get('/a/puma-sync/tasks').then(
            function success(response) {
                cb(null, response.data);
            },
            function failure(response) {
                cb(response.status + ":" + response.statusText);
            }
        )
    };

    /**
     * Callback of finding sync tasks information.
     *
     * @param err
     * @param result
     */
    $scope.findTasksCallback = function (err, result) {
        if (err) {
            $.notify({
                icon: 'glyphicon glyphicon-warning-sign',
                message: sprintf("Loading sync tasks failure: [%s]", err)
            }, {
                type: "warning"
            });
        } else {
            $scope.data.tasks = result;
            _.each($scope.data.tasks, function (task, index, tasks) {
                tasks[index] = $scope.normalize(task);
            });

            $.notify({
                icon: 'glyphicon glyphicon-ok',
                message: sprintf("Success to finding sync tasks.")
            }, {
                type: "info"
            });
        }
    };

    var remove = function (taskId, cb) {
        $http.delete("/a/puma-sync/remove?taskId=" + encodeURIComponent(taskId)).then(
            function success(response) {
                cb(null, response.data);
                var task = _.find($scope.data.tasks, function (task) {
                    return task.taskId === taskId;
                });
                $scope.data.tasks = _.without($scope.data.tasks, task);
            },
            function failure(response) {
                cb(response.status + ":" + response.statusText);
            }
        )
    };

    var removeCallback = function (err, result) {
        if (err) {
            $.notify({
                icon: 'glyphicon glyphicon-warning-sign',
                message: sprintf("Fail to remove sync task: [%s]", err)
            }, {
                type: "warning"
            });
        } else {
            $.notify({
                icon: 'glyphicon glyphicon-ok',
                message: sprintf("Success to remove sync task.")
            }, {
                type: "info"
            });
        }
    };

    $scope.remove = function (taskId) {
        remove(taskId, removeCallback);
    };

    (function () {
        $scope.findTasks($scope.findTasksCallback);
    })();
});

puma.controller('simpleModalController', function ($scope, item) {
    $scope.item = item;
});

puma.controller('pumaTaskStatusController', function ($scope, $http, $uibModal) {
    $scope.query = function () {
        $http.get('/a/puma-status?t=' + new Date().getTime()).then(
            function (response) {
                $scope.servers = response.data.servers;

                _.each($scope.servers, function (item) {
                    item.targetStr = _.map(_.first(item.target.tables, 10), function (table) {
                        return table.schemaName + '.' + table.tableName;
                    }).join('<br/>');

                    if (item.target.tables.length > 10) {
                        item.targetStr += '<br/>Click to show more...'
                    }
                });
            }
        );
    };

    $scope.query();

    $scope.openTarget = function (item) {
        $uibModal.open({
            templateUrl: 'target.html',
            controller: 'simpleModalController',
            resolve: {
                item: function () {
                    return item;
                }
            }
        });
    };
});

puma.controller('pumaClientController', function ($scope, $http, notify) {

    $scope.groupClients = {};

    var getGroupsAll0 = function (cb) {
        $http.get('/a/puma-client/group/list').then(
            function success(response) {
                cb(null, response.data);
            },
            function failure(response) {
                cb(response.status + ":" + response.statusText);
            }
        );
    };

    $scope.getGroupsAll = function () {
        getGroupsAll0(function (err, data) {
            if (err != null) {
                notify.error(data);
                return;
            }

            $scope.groupClients = data;
        });
    };

    (function() {
        $scope.getGroupsAll();
    })();

});

puma.controller('pumaClientStatusController', function ($scope, $http, $uibModal) {
    $scope.query = function () {
        $http.get('/a/puma-status?t=' + new Date().getTime()).then(
            function (response) {
                $scope.clients = response.data.clients;

                _.each($scope.clients, function (item) {
                    item.tablesStr = _.first(item.tables, 10).join('<br/>');

                    if (item.tables.length > 10) {
                        item.tablesStr += '<br/>Click to show more...'
                    }
                });
            }
        );
    };

    $scope.query();

    $scope.openTables = function (item) {
        $uibModal.open({
            templateUrl: 'tables.html',
            controller: 'simpleModalController',
            resolve: {
                item: function () {
                    return item;
                }
            }
        });
    };
});