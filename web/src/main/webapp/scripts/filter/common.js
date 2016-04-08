angular.module('puma')
    .filter('truncator', function () {
        return function (string, number) {
            if (!S(string).isEmpty()) {
                return S(string).truncate(number).s;
            }
        }
    });