angular
    .module('puma', [
        'oc.lazyLoad',
        'ui.router',
        'ui.bootstrap',
        'angular-loading-bar',
        'ngToast'
    ])
    .config(['$stateProvider', '$urlRouterProvider', '$ocLazyLoadProvider', function ($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {

        $ocLazyLoadProvider.config({
            debug: true,
            events: true
        });

        $urlRouterProvider.otherwise('/dashboard/home');

        $stateProvider
            .state('dashboard', {
                url: '/dashboard',
                templateUrl: 'views/dashboard/main.html',
                resolve: {
                    loadDirectives: function ($ocLazyLoad) {
                        $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/directives/header/header.js',
                                'scripts/directives/header/header-notification/header-notification.js',
                            ]
                        });

                        $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/directives/sidebar/sidebar.js',
                                'scripts/directives/sidebar/sidebar-search/sidebar-search.js'
                            ]
                        });
                    },

                    loadModules: function ($ocLazyLoad) {
                        $ocLazyLoad.load({
                            name: 'toggle-switch',
                            files: [
                                'bower_components/angular-toggle-switch/angular-toggle-switch.min.js',
                                'bower_components/angular-toggle-switch/angular-toggle-switch.css'
                            ]
                        });

                        $ocLazyLoad.load({
                            name: 'ngAnimate',
                            files: ['bower_components/angular-animate/angular-animate.js']
                        });

                        $ocLazyLoad.load({
                            name: 'ngCookies',
                            files: ['bower_components/angular-cookies/angular-cookies.js']
                        });

                        $ocLazyLoad.load({
                            name: 'ngResource',
                            files: ['bower_components/angular-resource/angular-resource.js']
                        });

                        $ocLazyLoad.load({
                            name: 'ngSanitize',
                            files: ['bower_components/angular-sanitize/angular-sanitize.js']
                        });

                        $ocLazyLoad.load({
                            name: 'ngTouch',
                            files: ['bower_components/angular-touch/angular-touch.js']
                        });
                    }
                }
            })

            .state('dashboard.home', {
                url: '/home',
                templateUrl: 'views/dashboard/home.html',
                controller: 'dashboardHomeController',
                resolve: {
                    loadControllers: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/dashboard/home.js'
                            ]
                        })
                    }],

                    loadFilters: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/filter/dashboard.js'
                            ]
                        })
                    }],

                    loadChart: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            serie: true,
                            name: 'chart.js',
                            files: [
                                'bower_components/Chart.js/Chart.min.js',
                                'bower_components/angular-chart.js/dist/angular-chart.css',
                                'bower_components/angular-chart.js/dist/angular-chart.min.js',
                            ]
                        })
                    }],
                }
            })

            .state('dashboard.document', {
                url: '/document/{filename}',
                templateUrl: 'views/document/document.html',
                controller: 'documentController',
                resolve: {
                    loadControllers: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/document/document.js'
                            ]
                        })
                    }],

                    loadServices: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/service/document.js'
                            ]
                        })
                    }],

                    loadNgShowDown: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            serie: true,
                            name: 'ng-showdown',
                            files: [
                                'bower_components/showdown/dist/showdown.min.js',
                                'bower_components/ng-showdown/dist/ng-showdown.min.js'
                            ]
                        })
                    }],
                }
            })

            .state('dashboard.instance', {
                url: '/instance',
                templateUrl: 'views/instance/main.html',
                controller: 'instanceController',
                resolve: {
                    loadControllers: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/instance/main.js',
                                'scripts/controller/modal/main.js',
                            ]
                        })
                    }],

                    loadFilters: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/filter/instance.js'
                            ]
                        })
                    }]
                }
            })

            .state('dashboard.instance-config', {
                url: '/instance-config',
                templateUrl: 'views/instance/config.html',
                controller: 'instanceConfigController',
                resolve: {
                    loadControllers: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/instance/config.js'
                            ]
                        })
                    }],

                    loadModules: ['$ocLazyLoad', function ($ocLazyLoad) {
                        $ocLazyLoad.load({
                            name: 'ui.bootstrap.datetimepicker',
                            files: [
                                'bower_components/angular-bootstrap-datetimepicker/src/css/datetimepicker.css',
                                'bower_components/angular-bootstrap-datetimepicker/src/js/datetimepicker.js',
                            ]
                        });

                        $ocLazyLoad.load({
                            name: 'isteven-multi-select',
                            files: [
                                'bower_components/isteven-angular-multiselect/isteven-multi-select.css',
                                'bower_components/isteven-angular-multiselect/isteven-multi-select.js'
                            ]
                        });
                    }],
                }
            })

            .state('dashboard.client', {
                url: '/client',
                templateUrl: 'views/client/main.html',
                controller: 'clientListController',
                resolve: {
                    loadModules: function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'ngTable',
                            files: [
                                'bower_components/ng-table/dist/ng-table.min.css',
                                'bower_components/ng-table/dist/ng-table.min.js'
                            ]
                        })
                    },

                    clientListController: function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/client/main.js',
                                'scripts/service/client.js'
                            ]
                        });
                    },
                }
            })

            .state('dashboard.client-create', {
                url: '/client/create/',
                templateUrl: 'views/client/create.html',
                controller: "clientCreateController",
                resolve: {
                    loadModules: ['$ocLazyLoad', function ($ocLazyLoad) {
                        $ocLazyLoad.load({
                            name: 'ngToast',
                            files: [
                                'bower_components/ngtoast/dist/ngToast.min.js',
                                'bower_components/ngtoast/dist/ngToast.min.css',
                                'bower_components/ngtoast/dist/ngToast-animations.min.css',
                            ]
                        });

                        $ocLazyLoad.load({
                            name: 'ui.bootstrap.datetimepicker',
                            files: [
                                'bower_components/angular-bootstrap-datetimepicker/src/css/datetimepicker.css',
                                'bower_components/angular-bootstrap-datetimepicker/src/js/datetimepicker.js',
                            ]
                        });
                    }],

                    loadFilters: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/filter/client.js'
                            ]
                        })
                    }],

                    clientCreateController: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/client/create.js',
                                'scripts/service/client.js'
                            ]
                        })
                    }],
                }
            })

            .state('dashboard.client-update', {
                url: '/client/update/{clientName}',
                templateUrl: 'views/client/update.html',
                controller: "clientUpdateController",
                resolve: {
                    loadModules: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'ngToast',
                            files: [
                                'bower_components/ngtoast/dist/ngToast.min.js',
                                'bower_components/ngtoast/dist/ngToast.min.css',
                                'bower_components/ngtoast/dist/ngToast-animations.min.css',
                            ]
                        }),

                        $ocLazyLoad.load({
                            name: 'ui.bootstrap.datetimepicker',
                            files: [
                                'bower_components/angular-bootstrap-datetimepicker/src/css/datetimepicker.css',
                                'bower_components/angular-bootstrap-datetimepicker/src/js/datetimepicker.js',
                            ]
                        });
                    }],

                    loadFilters: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/filter/client.js'
                            ]
                        })
                    }],

                    loadController: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/client/update.js',
                                'scripts/service/client.js'
                            ]
                        })
                    }],
                }
            })

            .state('dashboard.server-alarm', {
                url: '/server/alarm',
                templateUrl: 'views/server/alarm.html',
                controller: "serverAlarmController",
                resolve: {
                    loadNgTable: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'ngTable',
                            files: [
                                'bower_components/ng-table/dist/ng-table.min.css',
                                'bower_components/ng-table/dist/ng-table.min.js'
                            ]
                        })
                    }],

                    loadService: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/service/server.js',
                            ]
                        })
                    }],

                    loadController: ['$ocLazyLoad', function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/server/alarm.js',
                            ]
                        })
                    }],
                }
            })

    }]);

angular.module('puma')
    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q) {
            return {
                response: function (response) {
                    return response;
                },
                responseError: function (response) {
                    var status = response.status;

                    if (status === 404) {
                        window.location = './404.html';
                    }

                    return $q.reject(response);
                }
            };
        });
    });

