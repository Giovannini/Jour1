var baseUrl = "/";

/**
 * Factory that makes the interface with the server resources
 */
var RestFactory = ['$resource', function($resource) {
    // Crud Resource to create or edit a property, or to read all properties
    var urlSource = $resource(
        baseUrl+"properties",
        {},
        {
            'get': { method: "GET", isArray: true, Accept: "application/json" },
            'create': { method: "POST", Accept: "application/json" },
            'update': { method: "PUT", Accept: "application/json" }
        }
    );

    // Crud Resource to read or delete a property
    var urlID = $resource(
        baseUrl+"properties/:id",
        {
            id: -1
        },
        {
            'read': { method: "GET", Accept: "application/json" },
            'delete': { method: "DELETE", Accept: "application/json" }
        }
    );

    /**
     * Get all the properties
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var getProperties = function getProperties(success, failure) {
        urlSource.get(
            {},
            {},
            function(properties) {
                success(properties);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /**
     * Get a property
     * @param id id of the property
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var getProperty = function(id, success, failure) {
        urlID.read(
            {
                id: id
            },
            {},
            function(property) {
                success(property);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /**
     * Creates a property
     * @param property new property
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var createProperty = function(property, success, failure) {
        urlSource.create(
            {},
            property,
            function(action) {
                success(action);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /**
     * Updates a property
     * @param property new property
     * @param oldId id before the property was updated
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var updateProperty = function(property, oldId, success, failure) {
        urlSource.update(
            {
                id: oldId
            },
            property,
            function(action) {
                success(action);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /**
     * Delete a property
     * @param id id of the property
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var deleteProperty = function(id, success, failure) {
        urlID.delete(
            {
                id: id
            },
            {},
            function(property) {
                success(property);
            },
            function(response) {
                failure(response);
            }
        )
    };

    return {
        getProperties: getProperties,
        getProperty: getProperty,
        createProperty: createProperty,
        updateProperty: updateProperty,
        deleteProperty: deleteProperty
    }
}];

/**
 * Controller for the home page of the CRUD
 * Displays docs, and existing properties
 */
var OverviewCtrl = ['$scope', 'RestFactory', function($scope, RestFactory) {
    $scope.error = "";
    $scope.loading = true;

    // List all properties
    RestFactory.getProperties(
        function(properties) {
            $scope.properties = properties;
        },
        function() {
            $scope.error = "Impossible to load properties.";
        }
    );

    /**
     * Search a property from its id in the array with all properties
     * @param propertyId id of the property searched
     * @returns {number} box of array where the property is or -1 if the property isn't in the array
     */
    var searchProperty = function(propertyId) {
        var res = -1;
        for (var i=0; i<$scope.properties.length; i++) {
            if ($scope.properties[i].id == propertyId) {
                res = i;
            }
        }
        return res;
    };

    /**
     * Delete a property
     * @param id id of the property
     */
    $scope.deleteProperty = function(propertyId) {
        RestFactory.deleteProperty(
            propertyId,
            function() {
                var propertyLocation = searchProperty(propertyId);
                console.log(propertyLocation);
                if (propertyLocation != -1) {
                    $scope.properties.splice(propertyLocation, 1);
                }
            },
            function(response) {
                $scope.error = response.data;
            }
        );
    }
}];

/**
 * Factory that manages the action that is currently edited
 * It's used for creation and update
 */
var PropertyFactory = ['RestFactory', function(RestFactory) {
    var _property,
        _notifyError;

    /**
     * Inits the factory for the current controller
     * Must be done in order to notify the correct cotnroller
     */
    var init = function(notifyError) {
        _property = {};
        _notifyError = notifyError;
    };

    /**
     * Changes property
     * @param property
     */
    var setProperty = function(property) {
        _property = property;
    };

    /**
     * Submit the property to the REST interface
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     * @param oldId id of the property to update if edition, undefined if creation
     */
    var submitProperty = function(success, failure, oldId) {
        var propertyToSubmit = _property;
        if(typeof oldId !== "undefined") {
            RestFactory.updateProperty(propertyToSubmit, oldId, success, failure);
        } else {
            RestFactory.createProperty(propertyToSubmit, success, failure);
        }
    };

    return {
        init: init,
        setProperty: setProperty,
        submitProperty: submitProperty
    }
}];

/**
 * Controller managing the addition of a property
 */
var NewPropertyCtrl = ['$scope', 'PropertyFactory', '$location', function($scope, PropertyFactory, $location) {
    $scope.submit_button = "Create";
    $scope.back_url = "#/";
    $scope.isEditController = false;
    $scope.property = { id: 0 };

    // Initializing the edition factory
    PropertyFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    // Makes the submitProperty create a new Property
    $scope.submitProperty = function() {
        PropertyFactory.setProperty($scope.property);
        PropertyFactory.submitProperty(
            function(success) {
                console.log(success);
                $location.path("/");
            }, function(failure) {
                $scope.error = failure.data;
                console.log(failure);
            }
        );
    }
}];

/**
 * Controller managing the edition of a property
 */
var EditPropertyCtrl = ['$scope', '$routeParams', 'RestFactory', 'PropertyFactory', '$location', function($scope, $routeParams, RestFactory, PropertyFactory, $location) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";
    $scope.isEditController = true;
    var propertyId = $routeParams.id;

    // Initializing the edition factory
    PropertyFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    /**
     * Save the property in $scope and refresh the view
     * @param property
     */
    var displayProperty = function(property) {
        $scope.property = property;
    };

    // Get the old property
    RestFactory.getProperty(propertyId, displayProperty);

    // Makes the submitProperty update a Property
    $scope.submitProperty = function() {
        PropertyFactory.setProperty($scope.property);
        PropertyFactory.submitProperty(
            function(success) {
                console.log(success);
                $location.path("/");
            }, function(failure) {
                $scope.error = failure.data;
                console.log(failure);
            },
            propertyId
        );
    }
}];

/**
 * Declares all the stuff related to angular
 */
angular.module('propertyEditor', ["ngResource", "ngRoute"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'assets/templates/property/overview.html',
                controller: 'OverviewCtrl'
            }).
            when('/new', {
                templateUrl: 'assets/templates/property/edit_property.html',
                controller: 'NewPropertyCtrl'
            }).
            when('/:id/edit', {
                templateUrl: 'assets/templates/property/edit_property.html',
                controller: 'EditPropertyCtrl'
            }).
            otherwise({
                redirectTo: '/'
            })
    }])
    .factory('RestFactory', RestFactory)
    .controller('OverviewCtrl', OverviewCtrl)
    .factory('PropertyFactory', PropertyFactory)
    .controller('NewPropertyCtrl', NewPropertyCtrl)
    .controller('EditPropertyCtrl', EditPropertyCtrl);


