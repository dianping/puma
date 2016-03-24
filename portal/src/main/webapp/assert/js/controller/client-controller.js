angular.module('puma')
    .controller('clientController', ['$scope', '$http', function ($scope, $http) {
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
                    return;
                }

                $scope.groupClients = data;
            });
        };

        (function () {
            $scope.getGroupsAll();
        })();
    }]);
