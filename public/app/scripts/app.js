'use strict';

angular.module('email-client', [
  'infinite-scroll',
  'ngResource',
  'ngRoute',
  'xeditable',
  'ngAnimate',
  'ui.bootstrap',
  'ui.bootstrap.tpls',
  'angularFileUpload',
  'ui.bootstrap.datetimepicker',
  'validator',
  'validator.rules',
  'angularSpinner' ,
  'ngSanitize'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: '/assets/app/views/home.html',
        controller: 'SearchController'	
      })
      
      .otherwise({
          redirectTo: '/'
      });
  })
  .run(function(editableOptions) {
     editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

