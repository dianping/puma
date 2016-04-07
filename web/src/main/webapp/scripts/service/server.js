angular.module('puma')
    .service('serverService', ['$http', function ($http) {

        var baseUrl = '/a/servers';
        var alarmUrl = '/alarm';

        return {
            readAlarmServers: function (cb) {
                $http.get(baseUrl + alarmUrl).then(
                    function success(response) {
                        cb(response.data);
                    }
                )
            }
        }
    }]);