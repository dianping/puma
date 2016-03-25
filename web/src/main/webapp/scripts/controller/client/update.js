angular
    .module('puma')
    .controller(
        'clientUpdateController',
        [
            '$scope',
            '$stateParams',
            'ngToast',
            'clientService',
            'date2timestampFilter',
            'timestamp2dateFilter',
            function ($scope, $stateParams, ngToast, clientService, date2timestampFilter, timestamp2dateFilter) {

                var clientName = $stateParams.clientName;

                clientService.readByParam(clientName, function (data) {
                    $scope.client = data;

                    $scope.client.date = timestamp2dateFilter($scope.client.timestamp);
                });

                $scope.onClickSave = function () {
                    $scope.client.timestamp = date2timestampFilter($scope.client.date);

                    clientService.updateByParam(clientName, $scope.client, function () {
                        ngToast.create("修改成功");
                    });
                };

                $scope.onClickReset = function () {
                    $scope.client = {};
                }
            }
        ]
    );
