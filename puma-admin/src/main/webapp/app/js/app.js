var puma = angular.module('puma', ['ngRoute']);

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
    .otherwise({
      'redirectTo': '/'
    });
});

