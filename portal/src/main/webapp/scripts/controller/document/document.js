angular
    .module('puma')
    .controller(
        'documentController',
        [
            '$scope',
            '$stateParams',
            'documentService',
            function ($scope, $stateParams, documentService) {

                var filename = $stateParams.filename;

                switch(filename) {
                    case 'api':
                        $scope.title = '客户端接口';
                        break;
                    case 'register-api':
                        $scope.title = '客户端注册接口';
                        break;
                    case 'design':
                        $scope.title = 'Puma设计';
                        break;
                }

                documentService.read(filename, function (response) {
                    $scope.markdown = response.data;
                })
            }
        ]
    );
