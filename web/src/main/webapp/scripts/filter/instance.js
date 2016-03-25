angular.module('puma')
    .filter('toLocaleString', function () {
        return function (value) {
            return new Date(value).toLocaleString();
        };
    });