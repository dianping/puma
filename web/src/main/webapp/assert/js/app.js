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
            debug: false,
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
                        })
                    }
                }
            })

            .state('dashboard.home', {
                url: '/home',
                templateUrl: 'views/dashboard/home.html',
                resolve: {

                }
            })

            .state('dashboard.client', {
                url: '/client',
                templateUrl: 'views/client/list.html',
                controller: 'clientListController',
                resolve: {
                    clientListController: function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/client/list.js',
                                'scripts/service/client.js'
                            ]
                        });
                    },
                }
            })
            .state('dashboard.client-config', {
                url: '/client-config',
                templateUrl: 'views/client/config.html',
                controller: "clientConfigController",
                resolve: {
                    //loadNgAnimate: function ($ocLazyLoad) {
                    //    $ocLazyLoad.load({
                    //        name: 'ngAnimate',
                    //        files: [
                    //            'bower_components/angular-animate/angular-animate.min.js'
                    //        ]
                    //    });
                    //},
                    //loadNgSanitize: function ($ocLazyLoad) {
                    //    $ocLazyLoad.load({
                    //        name: 'ngSanitize',
                    //        files: [
                    //            'bower_components/angular-sanitize/angular-sanitize.min.js'
                    //        ]
                    //    })
                    //},
                    loadNgToast: function ($ocLazyLoad) {
                        $ocLazyLoad.load({
                            name: 'ngToast',
                            files: [
                                'bower_components/ngtoast/dist/ngToast.min.js',
                                'bower_components/ngtoast/dist/ngToast.min.css',
                                'bower_components/ngtoast/dist/ngToast-animations.min.css',
                            ]
                        });
                    },
                    clientConfigController: function ($ocLazyLoad) {
                        return $ocLazyLoad.load({
                            name: 'puma',
                            files: [
                                'scripts/controller/client/config.js',
                                'scripts/service/client.js'
                            ]
                        });
                    },
                }
            });

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

