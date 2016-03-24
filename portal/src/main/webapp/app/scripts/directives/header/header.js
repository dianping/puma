'use strict';

/**
 * @ngdoc directive
 * @name izzyposWebApp.directive:adminPosHeader
 * @description
 * # adminPosHeader
 */
angular.module('puma')
	.directive('header',function(){
		return {
        templateUrl:'assert/js/directives/header/header.html',
        restrict: 'E',
        replace: true,
    	}
	});


