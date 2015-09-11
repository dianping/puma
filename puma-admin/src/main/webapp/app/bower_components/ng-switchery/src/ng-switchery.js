'use strict';

/**
 * Module to use Switchery as a directive for angular.
 * @TODO implement Switchery as a service, https://github.com/abpetkov/switchery/pull/11
 */
angular.module('NgSwitchery', [])
    .directive('uiSwitch', ['$window', '$timeout','$log', '$parse', function($window, $timeout, $log, $parse) {

        /**
         * Initializes the HTML element as a Switchery switch.
         *
         * $timeout is in place as a workaround to work within angular-ui tabs.
         *
         * @param scope
         * @param elem
         * @param attrs
		 * @param ngModel
         */
        function linkSwitchery(scope, elem, attrs, ngModel) {
            if(!ngModel) return false;
            var options = {};
            try {
                options = $parse(attrs.uiSwitch)(scope);
            }
            catch (e) {}
            var switcher;
            var previousDisabledValue;
            // Watch for attribute changes to recreate the switch if the 'disabled' attribute changes
            attrs.$observe('disabled', function(value) {
              if (value == undefined || value == previousDisabledValue) {
                return;
              } else {
                previousDisabledValue = value;
              }
              initializeSwitch();
            });

            function initializeSwitch() {
              $timeout(function() {
                // Remove any old switcher
                if (switcher) {
                  angular.element(switcher.switcher).remove();
                }
                // (re)create switcher to reflect latest state of the checkbox element
                switcher = new $window.Switchery(elem[0], options);
                var element = switcher.element;
                element.checked = scope.initValue;
                switcher.setPosition(false);
                element.addEventListener('change',function(evt) {
                    scope.$apply(function() {
                        ngModel.$setViewValue(element.checked);
                    })
                })
              }, 0);
            }
            initializeSwitch();
          }

        return {
            require: 'ngModel',
            restrict: 'AE',
            scope : {initValue : '=ngModel'},
            link: linkSwitchery
        }
    }]);
