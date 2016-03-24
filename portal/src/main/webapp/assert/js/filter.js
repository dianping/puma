puma.filter('toLocaleString', function () {
    return function (value) {
        return new Date(value).toLocaleString();
    };
});

puma.filter('urlEncode', [function () {
    return window.encodeURIComponent;
}]);

puma.filter('delay', [function () {
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

puma.filter('brJoin', function() {
    return function (strings) {
        return strings.join('<br/>');
    };
});