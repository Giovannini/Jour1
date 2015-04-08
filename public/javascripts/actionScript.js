var baseUrl = "/";

/**
 * Gestion du DOM pour l'affichage des actions
 * Created by vlynn on 27/01/15.
 */
var ActionController = ['$scope', function($scope) {
    $scope.isTileSelected = false;
    $scope.showChoice = true;
    $scope.choices = [];
    $scope.selectedInstance = -1;
    $scope.actions = [];
    var needToApply;

    // Selected tile
    $scope.tile = {
        x: -1,
        y: -1
    };

    // Event listener that says a tile has been clicked
    document.addEventListener(TAG+'selectTile', function(event) {
        $scope.loadingTile = true;
        $scope.$apply();
        selectTile(
            event.detail.x,
            event.detail.y,
            event.detail.instances
        )
    });
    
    // Update scope to show the instances of the tile(x,y)
    function selectTile(x, y, instances) {
        $scope.tile.x = x;
        $scope.tile.y = y;
        $scope.instances = Map.getInstances(instances);

        $scope.isTileSelected = true;
        $scope.selectedInstance = -1;
        $scope.loadingTile = false;
        $scope.$apply();
    }
    
    // Show the actions of the selected instance 
    function applyActions(relations) {
        var actions = [],
            relatedConcept;
        
        for(var id in relations) {
            var conceptId = relations[id].relatedConcept;
            relatedConcept = Graph.getConcepts([conceptId])[conceptId];
            actions.push({
                id: id,
                relationId: relations[id].id,
                label: relations[id].label + "(" + relatedConcept.label + ")",
                conceptId: relatedConcept.id
            });
        }
        $scope.actions[$scope.selectedInstance] = actions;
        $scope.loadingActions = -1;
        if(needToApply)
            $scope.$apply();
    }
    
    // Select an instance and get its relations
    $scope.selectInstance = function(id) {
        $scope.showChoice = false;
        $scope.selectedAction = -1;
        if($scope.selectedInstance == id) {
            $scope.selectedAction = -1;
            $scope.loadingActions = -1;
        } else {
            $scope.loadingActions = id;
            $scope.selectedInstance = id;
            var conceptId = $scope.instances[id].conceptId;
            var concept = Graph.getConcepts([conceptId])[conceptId];

            // Cette ligne est nécessaire car elle permet de savoir correctement si on a besoin d'utiliser $apply dans le callback ou non
            needToApply = false;

            if (typeof concept !== "undefined") {
                needToApply = concept.getRelations(applyActions);
            } else {
                $scope.loadingActions = -1;
            }
        }
    };
    
    function applyChoices(instances) {
        $scope.choices = instances;
        $scope.loadingChoice = false;
        $scope.showChoice = true;
        $scope.$apply();
    }
    
    $scope.selectAction = function(id) {
        $scope.choices = [];
        $scope.showChoice = false;

        if($scope.selectedAction == id) {
            $scope.selectedAction = -1;
        } else {
            $scope.loadingChoice = true;
            $scope.selectedAction = id;
            var action = $scope.actions[$scope.selectedInstance][id];
            Map.getInstancesByConcept($scope.instances[$scope.selectedInstance].id, action.relationId, action.conceptId, applyChoices);
        }
    };
    
    
    $scope.sendAction = function(initInstanceId, actionId, destInstanceId) {
        var action = $scope.actions[$scope.selectedInstance][actionId];
        $scope.actionDone = false;
        Rest.action.sendAction(initInstanceId, action.relationId, destInstanceId)(
            function(responseText) {
                $scope.actionDone = true;
            },
            function(status, responseText) {
                $scope.actionDone = true;
            }
        );
    };

    /**
     * Create an overlay on an instance
     * Used by example when an instance can do an action on different others instances, on the hover of one of this other instance in the list
     * @param instanceId id of the instance
     * @param highlight true to create the overlay, false to destroy it
     */
    $scope.highlightInstance = function(instanceId, highlight) {
        MapController.highlightInstance(instanceId, highlight);
    };

    /**
     * Try to delete an instance
     * @param instanceId id of the instance among those placed in the same location from the map
     */
    $scope.deleteInstance = function(instanceId) {
        Rest.instances.deleteInstance($scope.instances[instanceId].id)(
            function(responseText) {
                $scope.instances.splice(instanceId, 1);
                $scope.$apply();
            },
            function(status, responseText) {
            }
        );
    }
}];

/**
 * Tool for the CRUD of instances
 * @type {*[]}
 */
var InstanceFactory = ['$resource', function($resource) {
    var _instance,
        _notifyError;

    /**
     * Initialization of factory
     * @param notifyError
     */
    var init = function(notifyError) {
        _instance = {};
        _notifyError = notifyError;
    };

    /**
     * Setter for instance
     * @param instance
     */
    var setInstance = function(instance) {
        _instance = instance;
    }

    /**
     * Send of the form to controller
     * @param url url of the controller
     * @returns {Function}
     */
    var submitInstance = function(url) {
        return function() {
            var submit = $resource(
                url,
                {},
                {
                    'save': {
                        method: "POST",
                        headers: [{'Content-Type': 'application/json'}]
                    }
                }
            );
            submit.save(
                {},
                _instance,
                function (response) {
                    console.log(response);
                },
                function (response) {
                    _notifyError("Impossible to save instance");
                }
            )
        };
    };

    /**
     * Get a concept
     * @param idConcept id of the concept
     * @param callback code to execute once we have the concept
     */
    var getConcept = function(idConcept, callback) {
        Rest.concepts.getById(idConcept)(
            function(responseText) {
                callback(JSON.parse(responseText));
            },
            function(status, responseText) {
                console.log(status, responseText);
            }
        );
    };

    /**
     * Get all concepts
     * @param callback code to execute once we have concepts
     */
    var getConcepts = function(callback) {
        Rest.concepts.get(
            function(responseText) {
                callback(JSON.parse(responseText));
            },
            function(status, responseText) {
                console.log(status, responseText);
            }
        );
    };

    return {
        instance: function() { return _instance; },
        init: init,
        setInstance: setInstance,
        submitInstance: submitInstance,
        getConcept: getConcept,
        getConcepts: getConcepts
    }
}];

/**
 * Controller to edit an instance
 * @type {*[]}
 */
var EditInstanceCtrl = ['$scope', '$routeParams', 'InstanceFactory', function($scope, $routeParams, InstanceFactory) {
    $scope.instanceId = $routeParams.id;
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";

    /**
     * Save the concept in $scope and refresh the view
     * @param concept
     */
    var displayConcept = function(concept) {
        $scope.concept = concept;
        $scope.$apply();
    };

    /**
     * Save the instance in $scope and refresh the view
     * @param instance
     */
    var displayInstance = function(instance) {
        $scope.instance = instance;
        $scope.$apply();

        // Get the concept of the instance
        InstanceFactory.getConcept($scope.instance.concept, displayConcept);
    };

    // Get the instance to edit
    Map.getInstance($scope.instanceId, displayInstance);

    // Initialization of factory to edit an instance
    InstanceFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    // Send the form to the server
    $scope.submitInstance = function() {
        InstanceFactory.setInstance($scope.instance);
        InstanceFactory.submitInstance(baseUrl+'instances/update')();
    };
}];

/**
 * Controller to create an instance
 * @type {*[]}
 */
var CreateInstanceCtrl = ['$scope', '$routeParams', 'InstanceFactory', function($scope, $routeParams, InstanceFactory) {
    $scope.submit_button = "Create";
    $scope.back_url = "#/";
    $scope.instance = { id:0 };

    /**
     * Save concepts in $scope and refresh the view
     * @param concepts
     */
    var displayConcepts = function(concepts) {
        $scope.concepts = concepts;
        $scope.$apply();
    };

    // Get concepts to choice the type of instance
    $scope.concepts = InstanceFactory.getConcepts(displayConcepts);

    // Case where coordinates already are choisen
    if (typeof $routeParams.x != "undefined" && typeof $routeParams.y != "undefined") {
        $scope.instance.coordinates = {};
        $scope.instance.coordinates.x = $routeParams.x;
        $scope.instance.coordinates.y = $routeParams.y;
    }

    /**
     * Refresh the view according to the concept
     * When the user choise a concept for his instance, properties to fill are different.
     * That's why we refresh the list of properties when concept changes.
     */
    $scope.refreshProperties = function() {
        $scope.instance.properties = [];
        for (var i in $scope.instance.concept.properties) {
            // foreach property of the concept, we create an element to save as a property for the instance
            var toAdd = {
                // property is the property of the concept
                property: $scope.instance.concept.properties[i],
                // value is the value of this property
                value: $scope.instance.concept.properties[i].defaultValue
            };

            // we add the element to the array of properties of the instance
            $scope.instance.properties.push(toAdd);
        }
    };

    // Initialization of factory to edit an instance
    InstanceFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    // Send the form to the server
    $scope.submitInstance = function() {
        // We want just send the id of concept
        $scope.instance.concept = $scope.instance.concept.id;
        InstanceFactory.setInstance($scope.instance);
        InstanceFactory.submitInstance(baseUrl+'instances/create')();
    };
}];

angular.module('actionManagerApp', ["ngRoute", "ngResource"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/overview', {
                templateUrl: 'assets/templates/map/overview.html',
                controller: 'ActionController'
            }).
            when('/instance/new', {
                templateUrl: 'assets/templates/map/instance/new_instance.html',
                controller: 'CreateInstanceCtrl'
            }).
            when('/instance/new/:x/:y', {
                templateUrl: 'assets/templates/map/instance/new_instance.html',
                controller: 'CreateInstanceCtrl'
            }).
            when('/instance/:id', {
                templateUrl: 'assets/templates/map/instance/edit_instance.html',
                controller: 'EditInstanceCtrl'
            }).
            otherwise({
                redirectTo: '/overview'
            });
    }])
    .controller('ActionController', ActionController)
    .controller('EditInstanceCtrl', EditInstanceCtrl)
    .controller('CreateInstanceCtrl', CreateInstanceCtrl)
    .factory('InstanceFactory', InstanceFactory);