angular.module('puma')
    .filter('date2timestamp', function () {
        return function (date) {
            return moment(date).unix();
        };
    })
    .filter('timestamp2date', function () {
        return function (timestamp) {
            return new Date(timestamp * 1000);
        }
    });
