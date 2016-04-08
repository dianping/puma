angular
    .module('puma')
    .controller(
        'clientUpdateController',
        [
            '$scope',
            '$stateParams',
            'ngToast',
            'clientService',
            'dto2pageFilter',
            'page2dtoFilter',
            'date2timestampFilter',
            'timestamp2dateFilter',
            function (
                $scope,
                $stateParams,
                ngToast,
                clientService,
                dto2pageFilter,
                page2dtoFilter,
                date2timestampFilter,
                timestamp2dateFilter
            ) {

                var clientName = $stateParams.clientName;

                clientService.readByParam(clientName, function (data) {
                    $scope.client = data;

                    $scope.client.date = timestamp2dateFilter($scope.client.timestamp);

                    $scope.client = dto2pageFilter($scope.client);
                });

                $scope.onClickSave = function () {
                    $scope.client.timestamp = date2timestampFilter($scope.client.date);

                    $scope.client = page2dtoFilter($scope.client);

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
