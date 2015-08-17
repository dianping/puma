var puma = angular.module('puma', ['ngRoute']);

puma.controller("Controller", function($scope) {

});

puma.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  $routeProvider.when('/puma-server', {
    templateUrl: '/partials/puma-server/list.html'
  }).otherwise({
    redirectTo: '/error'
  });

  $locationProvider.html5Mode(false);
}]);

