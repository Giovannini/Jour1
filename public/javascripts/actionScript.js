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
    }

    $scope.highlightInstance = function(instanceId, highlight) {
        console.log("instance : "+instanceId+" -> "+highlight);
        MapController.highlightInstance(instanceId, highlight);
    }

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


var InstanceFactory = ['$routeParams', 'Scopes', function($routeParams, Scopes) {
    return {};
}];

/**
 * Tool to edit an instance
 * @type {*[]}
 */
var EditInstanceFactory = ['$routeParams', '$resource', /*'InstanceFactory',*/ function($routeParams, $resource/*, InstanceFactory*/) {
    var _instance,
        _notifyError;

    /**
     * Initialization of factory
     * @param notifyError
     */
    var init = function(notifyError) {
        _instance = {
            label: ""
        }
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

    // Getters
    return {
        instance: function() { return _instance; },
        init: init,
        setInstance: setInstance,
        submitInstance: submitInstance
    }
}];

/**
 * Controller to edit an instance
 * @type {*[]}
 */
var EditInstanceCtrl = ['$scope', '$routeParams', 'EditInstanceFactory', function($scope, $routeParams, EditInstanceFactory) {
    $scope.isEdition = true;
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
    }

    /**
     * Save the instance in $scope and refresh the view
     * @param instance
     */
    var displayInstance = function(instance) {
        $scope.instance = instance;
        $scope.$apply();

        // Get the concept of the instance
        Graph.getConcept($scope.instance.concept, displayConcept);
    };

    // Get the instance to edit
    Map.getInstance($scope.instanceId, displayInstance);

    // Initialization of factory to edit an instance
    EditInstanceFactory.init(
        function(error) {
            $scope.error = error;
        }
    );

    // Send the form to the server
    $scope.submitInstance = function() {
        EditInstanceFactory.setInstance($scope.instance);
        console.log($scope.instance);
        EditInstanceFactory.submitInstance(baseUrl+'instances/update')();
    };
}];

angular.module('actionManagerApp', ["ngRoute", "ngResource"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/overview', {
                templateUrl: 'assets/templates/map/overview.html',
                controller: 'ActionController'
            }).
            when('/instance/:id/edit', {
                templateUrl: 'assets/templates/map/instance/edit_instance.html',
                controller: 'EditInstanceCtrl'
            }).
            otherwise({
                redirectTo: '/overview'
            });
    }])
    .controller('ActionController', ActionController)
    .controller('EditInstanceCtrl', EditInstanceCtrl)
    .factory('InstanceFactory', InstanceFactory)
    .factory('EditInstanceFactory', EditInstanceFactory)