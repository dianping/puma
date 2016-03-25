angular.module('puma')
    .controller('instanceController', ['$scope', '$http', '$uibModal', function ($scope, $http, $uibModal) {
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
                controller: 'modalController',
                resolve: {
                    item: function () {
                        return item;
                    }
                }
            });
        };
    }]);