'use strict';

angular.module('votenorestauranteApp')
    .controller('RestaurantController', function ($scope, $state, $modal, Restaurant) {
      
        $scope.restaurants = [];
        $scope.loadAll = function() {
            Restaurant.query(function(result) {
               $scope.restaurants = result;
            });
        };
        $scope.loadAll();


        $scope.refresh = function () {
            $scope.loadAll();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.restaurant = {
                name: null,
                votes: null,
                id: null
            };
        };
    });
