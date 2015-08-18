var puma = angular.module('puma', ['ngRoute']);

puma.config(function($routeProvider) {
  $routeProvider
    .when('/puma-monitor', {
      templateUrl: '/app/partials/puma-monitor.html'
    })
    .when('/puma-client', {
      templateUrl: '/app/partials/puma-client.html'
    })
    .when('/puma-server', {
      templateUrl: '/app/partials/puma-server.html'
    })
    .otherwise({
      'redirectTo': '/'
    });
});

