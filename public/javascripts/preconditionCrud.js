var baseUrl = "/";

/**
 * Factory that makes the interface with the server resources
 */
var RestFactory = ['$resource', function($resource) {
    // Crud Resource to create or edit a precondition, or to read all preconditions
    var urlSource = $resource(
            baseUrl+"preconditions",
        {},
        {
            'get': { method: "GET", isArray: true, Accept: "application/json" },
            'create': { method: "POST", Accept: "application/json" },
            'update': { method: "PUT", Accept: "application/json" }
        }
    );

    // Crud Resource to read or delete a precondition
    var urlID = $resource(
            baseUrl+"preconditions/:id",
        {
            id: -1
        },
        {
            'read': { method: "GET", Accept: "application/json" },
            'delete': { method: "DELETE", Accept: "application/json" }
        }
    );

    /**
     * Get all the preconditions
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var getPreconditions = function (success, failure) {
        urlSource.get(
            {},
            {},
            function(preconditions) {
                success(preconditions);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /**
     * Get a precondition
     * @param id id of the precondition
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var getPrecondition = function(id, success, failure) {
        urlID.read(
            {
                id: id
            },
            {},
            function(precondition) {
                success(precondition);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /**
     * Creates a precondition
     * @param precondition new precondition
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var createPrecondition = function(precondition, success, failure) {
        urlSource.create(
            {},
            precondition,
            function(precondition) {
                success(precondition);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /**
     * Updates a precondition
     * @param precondition new precondition
     * @param oldId id before the precondition was updated
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var updatePrecondition = function(precondition, oldId, success, failure) {
        urlSource.update(
            {
                id: oldId
            },
            precondition,
            function(precondition) {
                success(precondition);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /**
     * Delete a precondition
     * @param id id of the precondition
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     */
    var deletePrecondition = function(id, success, failure) {
        urlID.delete(
            {
                id: id
            },
            {},
            function(precondition) {
                success(precondition);
            },
            function(response) {
                failure(response);
            }
        )
    };

    return {
        getPreconditions: getPreconditions,
        getPrecondition: getPrecondition,
        createPrecondition: createPrecondition,
        updatePrecondition: updatePrecondition,
        deletePrecondition: deletePrecondition
    }
}];

/**
 * Controller for the home page of the CRUD
 * Displays docs, and existing preconditions
 */
var OverviewCtrl = ['$scope', 'RestFactory', function($scope, RestFactory) {
    $scope.error = "";
    $scope.loading = true;

    // List all preconditions
    RestFactory.getPreconditions(
        function(preconditions) {
            $scope.preconditions = preconditions;
        },
        function() {
            $scope.error = "Impossible to load preconditions";
        }
    );

    /**
     * Search a precondition from its id in the array with all preconditions
     * @param preconditionId id of the precondition searched
     * @returns {number} box of array where the precondition is or -1 if the precondition isn't in the array
     */
    var searchPrecondition = function(preconditionId) {
        var res = -1;
        for (var i=0; i<$scope.preconditions.length; i++) {
            if ($scope.preconditions[i].id == preconditionId) {
                res = i;
            }
        }
        return res;
    };

    /**
     * Delete a precondition
     * @param preconditionId id of the precondition
     */
    $scope.deletePrecondition = function(preconditionId) {
        RestFactory.deletePrecondition(
            preconditionId,
            function() {
                var preconditionLocation = searchPrecondition(preconditionId);
                if (preconditionLocation != -1) {
                    $scope.preconditions.splice(preconditionLocation, 1);
                }
            },
            function(response) {
                $scope.error = response.data;
            }
        );
    }
}];

/**
 * Factory that manages the precondition that is currently edited
 * It's used for creation and update
 */
var PreconditionFactory = ['RestFactory', function(RestFactory) {
    var _precondition,
        _notifyError;

    /**
     * Inits the factory for the current controller
     * Must be done in order to notify the correct controller
     */
    var init = function(notifyError) {
        _precondition = {};
        _notifyError = notifyError;
    };

    /**
     * Changes precondition
     * @param precondition
     */
    var setPrecondition = function(precondition) {
        _precondition = precondition;
    };

    /**
     * Submit the precondition to the REST interface
     * @param success function to apply in case of success
     * @param failure function to apply in case of failure
     * @param oldId id of the precondition to update if edition, undefined if creation
     */
    var submitPrecondition = function(success, failure, oldId) {
        var preconditionToSubmit = _precondition;
        if(typeof oldId !== "undefined") {
            RestFactory.updatePrecondition(preconditionToSubmit, oldId, success, failure);
        } else {
            RestFactory.createPrecondition(preconditionToSubmit, success, failure);
        }
    };

    return {
        init: init,
        setPrecondition: setPrecondition,
        submitPrecondition: submitPrecondition
    }
}];

/**
 * Controller managing the addition of a precondition
 */
var NewPreconditionCtrl = ['$scope', 'PreconditionFactory', '$location', function($scope, PreconditionFactory, $location) {
    $scope.submit_button = "Create";
    $scope.back_url = "#/";
    $scope.isEditController = false;
    $scope.precondition = { id: 0 };

    // Initializing the edition factory
    PreconditionFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    // Makes the submitPrecondition create a new Precondition
    $scope.submitPrecondition = function() {
        PreconditionFactory.setPrecondition($scope.precondition);
        PreconditionFactory.submitPrecondition(
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
 * Controller managing the edition of a precondition
 */
var EditPreconditionCtrl = ['$scope', '$routeParams', 'RestFactory', 'PreconditionFactory', '$location', function($scope, $routeParams, RestFactory, PreconditionFactory, $location) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";
    $scope.isEditController = true;
    var preconditionId = $routeParams.id;

    // Initializing the edition factory
    PreconditionFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    /**
     * Save the precondition in $scope and refresh the view
     * @param precondition
     */
    var displayPrecondition = function(precondition) {
        $scope.precondition = precondition;
    };

    // Get the old precondition
    RestFactory.getPrecondition(preconditionId, displayPrecondition);

    // Makes the submitPrecondition update a Precondition
    $scope.submitPrecondition = function() {
        PreconditionFactory.setPrecondition($scope.precondition);
        PreconditionFactory.submitPrecondition(
            function(success) {
                console.log(success);
                $location.path("/");
            }, function(failure) {
                $scope.error = failure.data;
                console.log(failure);
            },
            preconditionId
        );
    }
}];

/**
 * Declares all the stuff related to angular
 */
angular.module('preconditionEditor', ["ngResource", "ngRoute"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'assets/templates/precondition/overview.html',
                controller: 'OverviewCtrl'
            }).
            when('/new', {
                templateUrl: 'assets/templates/precondition/edit_precondition.html',
                controller: 'NewPreconditionCtrl'
            }).
            when('/:id/edit', {
                templateUrl: 'assets/templates/precondition/edit_precondition.html',
                controller: 'EditPreconditionCtrl'
            }).
            otherwise({
                redirectTo: '/'
            })
    }])
    .factory('RestFactory', RestFactory)
    .controller('OverviewCtrl', OverviewCtrl)
    .factory('PreconditionFactory', PreconditionFactory)
    .controller('NewPreconditionCtrl', NewPreconditionCtrl)
    .controller('EditPreconditionCtrl', EditPreconditionCtrl);