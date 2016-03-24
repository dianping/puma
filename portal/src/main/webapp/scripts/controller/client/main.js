angular.module('puma')
    .controller(
        'clientListController',
        [
            '$scope',
            'clientService',
            'NgTableParams',
            function ($scope, clientService, NgTableParams) {

                clientService.read(function (data) {

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
