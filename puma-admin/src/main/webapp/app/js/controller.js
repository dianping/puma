puma.config(function ($httpProvider) {
    $httpProvider.interceptors.push(function ($q) {
        return {
            response: function (response) {
                return response;
            },
            responseError: function (responseError) {
                if (responseError.status === 500) {
                    alert('failure');
                }
                return $q.reject(responseError);
            }
        };
    });
});

puma.controller('pumaCreateController', function ($scope, $http) {

    $scope.inputDatabases = [];
    $scope.outputDatabase = undefined;
    (function () {
        $http.get('/a/puma-create/database').success(function (inputDatabases) {
            $scope.inputDatabases = inputDatabases;
            jQuery(function ($) {
                $("#form-database").autocomplete({
                    source: $scope.inputDatabases
                });
            });
        });
    })();

    $scope.inputTables = [];
    $scope.outputTables = [];
    $scope.findTables = function () {
        jQuery(function ($) {
            $scope.outputDatabase = $("#form-database").val()
        });
        if ($scope.outputDatabase !== undefined) {
            var url = '/a/puma-create/table?database=' + $scope.outputDatabase;
            $http.get(url).success(function (tables) {
                angular.forEach(tables, function (table) {
                    $scope.inputTables.push({name: table});
                });
            });
        }
    };

    $scope.inputServers = [];
    $scope.outputServers = [];
    (function () {
        $http.get('/a/puma-create/server').success(function (servers) {
            angular.forEach(servers, function (server) {
                $scope.inputServers.push({name: server});
            });
        });
    })();

    $scope.submit = function () {
        jQuery(function ($) {
            $scope.outputDatabase = $("#form-database").val();
            $scope.outputBeginTime = $("#form-begin-time").val();
            $scope.outputBeginTime = moment($scope.outputBeginTime).format();
        });

        var tables = [];
        angular.forEach($scope.outputTables, function (table) {
            tables.push(table['name']);
        });
        var servers = [];
        angular.forEach($scope.outputServers, function (server) {
            servers.push(server['name']);
        });
        var database = $scope.outputDatabase;

        var beginTimes = {};
        angular.forEach(servers, function (server) {
            beginTimes[server] = $scope.outputBeginTime;
        });

        var json = {
            database: database,
            tables: tables,
            serverNames: servers,
            beginTimes: beginTimes
        };

        $http.post('/a/puma-create', json);
    };

    $scope.reset = function () {
        $scope.database = null;
        $scope.tables = null;
        $scope.servers = null;
        $scope.beginTime = null;
    }
});

puma.controller('pumaTargetController', function ($scope, $http) {

    $scope.pumaDto = {};

    $scope.allServers = [];
    $scope.allTables = [];
    $scope.createMode = false;

    $scope.showCreateTables = false;


    (function () {
        $http.get('/a/puma-create/server').success(function (servers) {
            angular.forEach(servers, function (server) {
                $scope.allServers.push(server);
            });
        });
    })();

    $scope.search = function () {
        var url = '/a/puma/search?database=' + $scope.pumaDto.database;
        $http.get(url).success(
            function (response) {
                $scope.pumaDto = response;

                $scope.servers = [];
                angular.forEach($scope.pumaDto.serverNames, function(serverName) {
                    var server = {
                        name: serverName,
                        input: buildInput(serverName, $scope.pumaDto.serverNames),
                        output: [],
                        beginTime: setDate($scope.pumaDto.beginTimes[serverName]),
                        registry: $scope.pumaDto.registries[serverName]
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
        var url = '/a/puma-create/table?database=' + database;
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
        angular.forEach(allServers, function(server) {
            if (server === serverName) {
                list.push({ name: server, selected: true });
            } else {
                list.push({ name: server });
            }
        });
        return list;
    };

    function parseOutput(servers) {
        var list = [];
        angular.forEach(servers, function(server) {
             list.push(server.output[0].name);
        });
        return list;
    };

    function parseTables(tables) {
        var list = [];
        angular.forEach(tables, function(table) {
            list.push(table.name);
        });
        return list;
    };

    function parseBeginTime(servers) {
        var list = {};
        angular.forEach(servers, function(server) {
            try {
                list[server.output[0].name] = new Date(server.beginTime).getTime();
            } catch (err) {}
        });
        return list;
    };

    function parseRegistry(servers) {
        var list = {};
        angular.forEach(servers, function(server) {
           list[server.output[0].name] = server.registry;
        });
        return list;
    }

    $scope.submit = function() {
        $scope.pumaDto.serverNames = parseOutput($scope.servers);
        $scope.pumaDto.tables = parseTables($scope.tables);
        $scope.pumaDto.beginTimes = parseBeginTime($scope.servers);
        $scope.pumaDto.registries = parseRegistry($scope.servers);

        var json = {
            serverNames: $scope.pumaDto.serverNames,
            tables: $scope.pumaDto.tables,
            database: $scope.pumaDto.database,
            beginTimestamps: $scope.pumaDto.beginTimes,
            registries: $scope.pumaDto.registries
        };

        $http.post('/a/puma-create', json).success(function(response) {
            alert('success');
        });
    };

    $scope.add = function() {
        $scope.servers.push({
            name: '',
            input: buildInput('', $scope.allServers),
            output: [],
            registry: true
        });
    };

    $scope.delete = function(server) {
        var index = $scope.servers.indexOf(server);
        $scope.servers.splice(index, 1);
    };

    $scope.checkShowWell = function() {
        return _.size($scope.pumaDto.serverNames) === 0 && !$scope.createMode;
    };

    $scope.checkShowNewServers = function() {
        return $scope.createMode;
    };

    $scope.checkShowOldServers = function() {
        return !$scope.createMode;
    };
});