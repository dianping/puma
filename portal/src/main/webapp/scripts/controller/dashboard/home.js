angular
    .module('puma')
    .controller('dashboardHomeController', [
        '$scope',
        '$http',
        function ($scope, $http) {
            $scope.options = {animation: false};

            var refresh = function () {
                $http.get("/a/dashboard?t=" + new Date().getTime()).success(
                    function (response) {
                        $scope.data = response;
                    }
                );
            };
            refresh();
        }]
    );
