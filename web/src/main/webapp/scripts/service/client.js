angular.module('puma')
    .service('clientService', ['$http', function ($http) {

        var baseUrl = '/a/clients/';

        var generateUrl = function (baseUrl, param) {
            return baseUrl + param;
        };

        return {
            read: function (cb) {
                $http.get(baseUrl).then(
                    function success(response) {
                        cb(response.data);
                    }
                )
            },

            create: function (client, cb) {
                $http.post(baseUrl, client).then(
                    function success() {
                        cb();
                    }
                );
            },

            update: function (client, cb) {
                $http.put(baseUrl, client).then(
                    function success() {
                        cb();
                    }
                )
            },

            remove: function (cb) {
                $http.delete(baseUrl).then(
                    function success() {
                        cb();
                    }
                )
            },

            readByParam: function (clientName, cb) {
                $http.get(generateUrl(baseUrl, clientName)).then(
                    function success(response) {
                        cb(response.data);
                    }
                )
            },

            createByParam: function (clientName, client, cb) {
                $http.post(generateUrl(baseUrl, clientName), client).then(
                    function success(response) {
                        cb();
                    }
                )
            },

            updateByParam: function (clientName, client, cb) {
                $http.put(generateUrl(baseUrl, clientName), client).then(
                    function success(response) {
                        cb();
                    }
                )
            },

            removeByParam: function (clientName, cb) {
                $http.delete(generateUrl(baseUrl, clientName)).then(
                    function success(response) {
                        cb();
                    }
                )
            }
        }
    }]);
