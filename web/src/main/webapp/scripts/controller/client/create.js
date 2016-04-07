angular
    .module('puma')
    .controller(
        'clientCreateController',
        [
            '$scope',
            'ngToast',
            'clientService',
            'date2timestampFilter',
            function ($scope, ngToast, clientService, date2timestampFilter) {

                $scope.onClickSave = function () {
                    $scope.client.timestamp = date2timestampFilter($scope.client.date);

                    switch($scope.client.alarmStrategy) {
                        case 'no':
                            $scope.client.noAlarm = true;
                            break;
                        case 'linear':
                            $scope.client.linearAlarm = true;
                            break;
                        case 'exponential':
                        default:
                            $scope.client.exponentialAlarm = true;
                    }

                    clientService.create($scope.client, function () {
                        ngToast.create("创建成功");
                    });
                };

                $scope.onClickReset = function () {
                    $scope.client = {};
                }
            }
        ]
    );
