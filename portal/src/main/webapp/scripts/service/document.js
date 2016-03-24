angular.module('puma')
    .service('documentService', ['$http', function ($http) {

        return {
            read: function (filename, cb) {
                $http.get('/docs/' + filename + '.md').then(
                    function success(response) {
                        cb(response);
                    }
                )
            },
        }
    }]);
