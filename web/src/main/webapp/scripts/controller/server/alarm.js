angular.module('puma')
    .controller(
        'serverAlarmController',
        [
            '$scope',
            'serverService',
            'NgTableParams',
            function ($scope, serverService, NgTableParams) {

                serverService.readAlarmServers(function (data) {

                    $scope.tableParams = new NgTableParams(
                        {},
                        {
                            dataset: data
                        }
                    );
                });
            }
        ]
    );