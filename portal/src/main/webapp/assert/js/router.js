//puma.config(function ($routeProvider) {
//    $routeProvider
//        .when('/dashboard', {
//            templateUrl: '/partials/dashboard/index.html',
//            controller: 'pumaDashboardController'
//        })
//        .when('/docs/:file', {
//            templateUrl: '/partials/document/markdown.html',
//            controller: 'pumaMarkDownController'
//        })
//
//
//        .when('/puma-task/config', {
//            templateUrl: '/partials/puma-task/config.html',
//            controller: 'pumaTaskConfigController'
//        })
//        .when('/puma-task', {
//            templateUrl: '/partials/puma-task/status.html',
//            controller: 'pumaTaskStatusController'
//        })
//
//        .when('/puma-client/config', {
//            templateUrl: '/partials/puma-client/config.html',
//            controller: 'pumaClientConfigController'
//        })
//        .when('/puma-client', {
//            templateUrl: '/partials/puma-client/status.html',
//            controller: 'pumaClientStatusController'
//        })
//        .when('/puma-client/group-list', {
//            templateUrl: '/partials/puma-client/group-list.html',
//            controller: 'pumaClientController'
//        })
//
//        .when('/puma-check/config', {
//            templateUrl: '/partials/puma-check/config.html',
//            controller: 'pumaCheckConfigController'
//        })
//        .when('/puma-check', {
//            templateUrl: '/partials/puma-check/status.html',
//            controller: 'pumaCheckStatusController'
//        })
//
//        .when('/puma-sync/config', {
//            templateUrl: '/partials/puma-sync/config.html',
//            controller: 'pumaSyncConfigController'
//        })
//        .when('/puma-sync/status', {
//            templateUrl: '/partials/puma-sync/status.html',
//            controller: 'pumaSyncStatusController'
//        })
//
//        .otherwise({
//            'redirectTo': '/dashboard'
//        });
//});