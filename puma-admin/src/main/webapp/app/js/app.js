/*
var underscore = angular.module('underscore', []);
underscore.factory('_', ['$window', function() {
  return $window._; // assumes underscore has already been loaded on the page
}]);*/

var puma = angular.module('puma', ['ngRoute']);

puma.controller('pumaCreateController', function($scope, $http) {

  $scope.submit = function() {
    var database = $scope.database;
    var tables = $scope.tables.split(',');
    var serverNames = $scope.servers.split(',');
    var beginTimes = {};
    _.each(serverNames, function(serverName) {
      beginTimes[serverName] = $scope.beginTime;
    });

    var json = {
      database: database,
      tables: tables,
      serverNames: serverNames,
      beginTimes: beginTimes
    };

    $http.post('/a/puma-create', json).then(
      function(response) {
        alert('success');
      },
      function(response) {
        alert('failure, ' + response.data.msg);
      }
    )
  }

  $scope.reset = function() {
    $scope.database = null;
    $scope.tables = null;
    $scope.servers = null;
    $scope.beginTime = null;
  }

});

puma.controller('pumaTargetController', function($scope, $http) {

  $scope.init = true;
  $scope.pumaDto = {};

  $scope.search = function() {
    var postJson = {
      'database': $scope.pumaDto.database
    };
    $http.post('/a/puma-target', postJson).then(
      function(response) {
        if (response.data.status == "success") {
          $scope.pumaDto = response.data.result;
        }
      },
      function(response) {
        alert("failure");
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
    .when('/puma-create', {
      templateUrl: '/app/partials/puma-create.html',
      controller: 'pumaCreateController'
    })
    .otherwise({
      'redirectTo': '/'
    });
});

