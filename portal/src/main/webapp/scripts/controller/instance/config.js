angular.module('puma')
    .controller('instanceConfigController', ['$scope', '$http', function ($scope, $http) {

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
    }]);
