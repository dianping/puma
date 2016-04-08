angular.module('puma')
    .filter('truncator', function () {
        return function (string, number) {
            if (!S(string).isEmpty()) {
                return S(string).truncate(number).s;
            }
        }
    })
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