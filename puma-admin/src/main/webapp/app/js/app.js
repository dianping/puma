/*
var underscore = angular.module('underscore', []);
underscore.factory('_', ['$window', function() {
  return $window._; // assumes underscore has already been loaded on the page
}]);*/

var puma = angular.module('puma', ['ngRoute']);

puma.controller('pumaTargetController', function($scope, $http) {

  $scope.puma = {};
  $scope.servers = {};

  $scope.search = function() {
    var postJson = {
      'database': $scope.database
    };
    $http.post('/a/puma-task/list', postJson).then(
      function(response) {
        if (response.data.status == "success") {
          $scope.puma = response.data.result;
          $scope.servers = $scope.puma.servers;
        }
      },
      function(response) {

      }
    )
  }
});

puma.controller('pumaTaskCreateController', function($scope, $http) {

  $scope.task = {
    targets: {
      database: ['table_0', 'table_1']
    },
    hosts: ['0.0.0.0', '1.1.1.1'],
    beginTime: 0
  };

  $scope.targets = [{
    database: null,
    tables: null
  }];

  $scope.submit = function() {



    $scope.task.targets[target.database]

    $http.post('/a/puma-task/create', $scope.pumaTaskJson).then(
      function(response) {
        alert('success');
      },
      function(response) {
        alert('failure');
      }
    );
  }
});

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
    .when('/puma-task', {
      templateUrl: '/app/partials/puma-task.html'
    })
    .when('/puma-target', {
      templateUrl: '/app/partials/puma-target.html',
      controller: 'pumaTargetController'
    })
    .otherwise({
      'redirectTo': '/'
    });
});

