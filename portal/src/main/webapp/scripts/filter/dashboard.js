angular.module('puma')
    .filter('delay', [function () {
        return function (value) {
            if (value < 60) {
                return value + "秒"
            } else if (value < 60 * 60) {
                return (value / 60).toFixed(1) + "分钟"
            } else if (value < 60 * 60 * 24) {
                return (value / 60 / 60).toFixed(1) + "小时"
            } else {
                return (value / 60 / 60 / 24).toFixed(1) + "天"
            }
        };
    }]);