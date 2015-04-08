var baseUrl = "/";

/*
 * Factory that makes the interface with the server resources
 */
var RestFactory = ['$resource', function($resource) {
    var _properties = {},
        _preconditions = {},
        _actions = {};

    /*
     * Crud resource
     */
    var submitAction = $resource(
        baseUrl+"actions/:label",
        { label: "" },
        {
            'create': { method: "POST", Accept: "application/json"},
            'read': { method: "GET", Accept: "application/json"},
            'update': { method: "PUT", Accept: "application/json"},
            'delete': { method: "DELETE", Accept: "application/json"}
        }
    );

    /* Create a new action : action must have a specific format -> see TODO */
    var createAction = function(action, success, failure) {
        submitAction.create(
            {label: action.label},
            action,
            function(action) {
                success(action);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /* Updates an action :
        action must have a specific format -> see TODO
        oldLabel is the one before the action was updated
    */
    var updateAction = function(action, oldLabel, success, failure) {
        submitAction.update(
            {label: oldLabel},
            action,
            function(action) {
                success(action);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /* Returns an action already existing on the server side */
    var getAction = function(label, success, failure) {
        submitAction.read(
            {label: label},
            function(action) {
                success(action);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /* Gets all the properties elligible */
    var getProperties = function getProperties(success, failure) {
        var request = $resource(
            baseUrl+'properties',
            {},
            { 'get': {
                method: "GET",
                isArray: true,
                Accept: "application/json"
            }}
        );
        request.get(
            {},
            function(response) {
                _properties = response;
                success(_properties);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /* Gets all the existing preconditions */
    var getPreconditions = function getPreconditions(success, failure) {
        var request = $resource(
            baseUrl+'preconditions',
            {},
            { 'get': {
                method: "GET",
                isArray: true,
                Accept: "application/json"
            }}
        );
        request.get(
            {},
            function(response) {
                _preconditions = response;
                success(_preconditions);
            },
            function(response) {
                failure(response);
            }
        );
    };

    /* Get all the actions available in the DB */
    var getActions = function getActions(success, failure) {
        var request = $resource(
            baseUrl+'actions',
            {},
            { 'get': {
                method: "GET",
                isArray: true,
                Accept: "application/json"
            }}
        );
        request.get(
            {},
            function(response) {
                _actions = response;
                success(_actions);
            },
            function(response) {
                failure(response);
            }
        );
    };

    return {
        getAction: getAction,
        createAction: createAction,
        updateAction: updateAction,

        getProperties: getProperties,
        getPreconditions: getPreconditions,
        getActions: getActions
    }
}];

/*
 Controller for the home page of the CRUD
 Displays docs, and existing actions
 */
var OverviewCtrl = ['$scope', 'RestFactory', function($scope, RestFactory) {
    $scope.error = "";
    $scope.loading = true;
    RestFactory.getActions(
        function(actions) {
            $scope.actions = actions;
        },
        function(failure) {
            $scope.error = "Impossible to load actions.";
        }
    );
}];

/*
 Factory that manages the action that is currently edited
 It's used for creation and update
 */
var EditActionFactory = ['RestFactory', function(RestFactory) {
    var _action,

        // Vars used for selects
        _paramTypes,
        _properties,
        _preconditions,
        _actions,

        // Functions called for notifying a Controller
        _notifyError,
        _updateProperties,
        _updatePreconditions,
        _updateActions;

    /* Types of parameters */
    _paramTypes = [
        {value: "Int", label: "Int"},
        {value: "Long", label: "Id"},
        {value: "Property", label: "Property"}
    ];

    /* Gets all kind of properties */
    _properties = null;
    RestFactory.getProperties(
        function(properties) {
            _properties = properties;
            _updateProperties(_properties);
        },
        function(failure) {
            _notifyError("Impossible to load properties");
        }
    );

    /* Get all actions */
    _actions = null;
    RestFactory.getActions(
        function(actions) {
            _actions = actions;
            _updateActions(_actions);
        },
        function(failure) {
            _notifyError("Impossible to load actions");
        }
    );

    _preconditions = null;
    RestFactory.getPreconditions(
        function(preconditions) {
            _preconditions = preconditions;
            _updatePreconditions(_preconditions);
        },
        function(failure) {
            _notifyError("Impossible to load preconditions");
        }
    );

    /*
    When a parameter of a precondition or a subaction is a reference to the parameters of the current action
    It displays only the elligible parameters
    */
    var parametersFilteredByType = function(type) {
        return _action.parameters.filter(function(param) {
            return param.type == type;
        });
    };

    /*
    Remove a parameter previously added
     */
    var removeParameter = function(parameterIndex) {
        _action.parameters.splice(parameterIndex, 1);
    };

    /*
    Add a new parameter
     */
    var addParameter = function() {
        _action.parameters.push({
            reference: "",
            type: _paramTypes[0]
        });
    };

    var updateParameters = function(parameters) {
        var arguments = [];
        for (var param in parameters) {
            console.log("");
            console.log(param);
            console.log(parameters[param].reference);
            arguments[parameters[param].reference] = {
                isParam: false,
                value: {
                    value: "",
                    type: parameters[param].type
                }
            };
        }
        return arguments;
    };

    var updatePreconditionParameters = function(preconditionIndex) {
        _action.preconditions[preconditionIndex].arguments =
            updateParameters(_action.preconditions[preconditionIndex].precondition.parameters);
    };

    var updateActionParameters = function(actionIndex) {
        console.log(_action.actions[actionIndex]);
        _action.actions[actionIndex].arguments =
            updateParameters(_action.actions[actionIndex].action.parameters);
    };

    /*
    Remove an existing precondition
     */
    var removePrecondition = function(preconditionIndex) {
        _action.preconditions.splice(preconditionIndex, 1);
    };

    /*
    Add a new precondition to the action
     */
    var addPrecondition = function() {
        _action.preconditions.push({
            precondition: _preconditions[0],
            arguments: []
        });
        updatePreconditionParameters(_action.preconditions.length - 1);
    };

    /*
    remove a subaction
     */
    var removeAction = function(actionIndex) {
        _action.actions.splice(actionIndex, 1);
    };

    /*
    Add a subaction to the current action
     */
    var addAction = function() {
        _action.actions.push({
            action: _actions[0],
            arguments: ""
        });
    };

    /*
    Cleans the object action so that it's usable on the REST things
     */
    function getActionToSubmit(action) {
        // Clean preconditions
        var preconditionIndex, parameterIndex,
            preconditions, precondition,
            parameters, parameter,
            arguments, argument,
            subActions, subAction, actionIndex,
            current;

        var actionToSubmit = {
            "label": action.label,
            "parameters": action.parameters
        };

        function getParameters(parameters, arguments) {
            var result = [];
            for(parameterIndex in parameters) {
                parameter = parameters[parameterIndex];

                argument = arguments[parameter.reference];

                result.push(argument);
            }
            return result;
        }

        preconditions = [];
        for(preconditionIndex in action.preconditions) {
            current = action.preconditions[preconditionIndex];

            precondition = {};
            precondition.id = current.precondition.id;
            precondition.parameters = getParameters(current.precondition.parameters, current.arguments);

            preconditions.push(precondition);
        }
        actionToSubmit.preconditions = preconditions;

        subActions = [];
        for(actionIndex in action.actions) {
            current = action.actions[actionIndex];

            subAction = {};
            subAction.id = current.action.id;
            subAction.parameters = getParameters(current.action.parameters, current.arguments);

            subActions.push(subAction);
        }
        actionToSubmit.subActions = subActions;

        return actionToSubmit;
    }

    /*
    submit the action to the rest interface
     */
    var submitAction = function(action, success, failure, oldLabel) {
        var actionToSubmit = getActionToSubmit(action);
        if(typeof oldLabel !== "undefined") {
            RestFactory.updateAction(actionToSubmit, oldLabel, success, failure);
        } else {
            RestFactory.createAction(actionToSubmit, success, failure);
        }

    };

    /*
     Inits the factory for the current controller
     Must be done in order to notify the correct cotnroller
    */
    var init = function init(
        notifyError,
        updateProperties,
        updatePreconditions,
        updateActions
    ) {
        _action = {
            label: "",
            preconditions: [],
            parameters: [],
            actions: []
        };

        _notifyError = notifyError;
        _updateProperties = updateProperties;
        _updatePreconditions = updatePreconditions;
        _updateActions = updateActions;
    };

    return {
        action: function() { return _action; },
        setAction: function(action) { _action = action; },

        properties: function() { return _properties; },
        preconditions: function() { return _preconditions; },
        actions: function() { return _actions; },
        paramTypes: function() { return _paramTypes; },

        init: init,
        parametersFilteredByType: parametersFilteredByType,

        removeParameter: removeParameter,
        addParameter: addParameter,

        removePrecondition: removePrecondition,
        addPrecondition: addPrecondition,
        updatePreconditionParameters: updatePreconditionParameters,
        updateActionParameters: updateActionParameters,

        removeAction: removeAction,
        addAction: addAction,

        submitAction: submitAction
    };
}];

/*
Controller managing the addition of an action
 */
var NewActionCtrl = ['$scope', 'EditActionFactory', '$location', function($scope, EditActionFactory, $location) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";

    /*
     * Initializing the edition factory
     */
    EditActionFactory.init(
        function(error) {
            $scope.error = error;
        },
        function updateProperties(properties) {
            $scope.properties = properties;
        },
        function updatePreconditions(preconditions) {
            $scope.preconditions = preconditions;
        },
        function updateActions(actions) {
            $scope.actions = actions;
        }
    );

    $scope.action = EditActionFactory.action();

    $scope.properties = EditActionFactory.properties();
    $scope.preconditions = EditActionFactory.preconditions();
    $scope.actions = EditActionFactory.actions();
    $scope.paramTypes = EditActionFactory.paramTypes();

    $scope.parametersFilteredByType = EditActionFactory.parametersFilteredByType;

    $scope.removeParameter = EditActionFactory.removeParameter;
    $scope.addParameter = EditActionFactory.addParameter;

    $scope.removePrecondition = EditActionFactory.removePrecondition;
    $scope.addPrecondition = EditActionFactory.addPrecondition;
    $scope.updatePreconditionParameters = EditActionFactory.updatePreconditionParameters;
    $scope.updateActionParameters = EditActionFactory.updateActionParameters;

    $scope.removeAction = EditActionFactory.removeAction;
    $scope.addAction = EditActionFactory.addAction;

    /* Makes the submitAction create a new InstanceAction */
    $scope.submitAction = function() {
        EditActionFactory.setAction($scope.action);
        EditActionFactory.submitAction(
            $scope.action,
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

/*
Controller managing the edition of an action
 */
var EditActionCtrl = ['$scope', '$routeParams', 'RestFactory', 'EditActionFactory', function($scope, $routeParams, RestFactory, EditActionFactory) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/"+$routeParams.label;
    $scope.action  = {};
    var actionInitialized = false;

    /* Init the edition factory */
    /* We must wait for properties, perconditions and actions to be initialized before we can fetch the action */
    EditActionFactory.init(
        function(error) {
            $scope.error = error;
        },
        function updateProperties(properties) {
            $scope.properties = properties;
            initAction();
        },
        function updatePreconditions(preconditions) {
            $scope.preconditions = preconditions;
            initAction();
        },
        function updateActions(actions) {
            $scope.actions = actions;
            initAction();
        }
    );

    /* Once we have all the objects needed to the initialization,
     we can look for the action and clean it so that it fits with the angular form

     the ending action must be like so :

     *      {
     *          label: action_label,
     *          actions: [
     *              { // the original object is from $scope.actions
     *                  action: $scope.actions[i],
     *                  arguments: [{
     *                      isParam: boolean,
     *                      value: {
     *
     *                      }
     *                  }]
     *              }
     *          ],
     *          preconditions: [
     *              { // the original object is from $scope.actions
     *                  precondition: $scope.actions[i],
     *                  arguments: [{
     *                      isParam: boolean,
     *                      value: {
     *
     *                      }
     *                  }]
     *              }
     *          ],
     *          parameters: [
     *              {
     *                  reference: string,
     *                  type: string
     *              }
     *          ]
      */
    function initAction() {
        if(!actionInitialized
            && $scope.properties != null && $scope.properties.length > 0
            && $scope.preconditions != null && $scope.preconditions.length > 0
            && $scope.actions != null && $scope.actions.length > 0) {
            actionInitialized = true;
            RestFactory.getAction(
                $routeParams.label,
                function(action) {
                    /* Transform the action to an object with the form : */

                    /* match parameters to arguments */
                    function matchParameters(currentParameters) {
                        /* Find a parameter matching for each parameters */
                        var arguments = {};
                        for (var parameterIndex in currentParameters) {
                            var parameterInitialized = false;

                            /* First look in the parameters of the action */
                            //if(currentParameters[parameterIndex].value.reference)
                            for (var j = 0; j < action.parameters.length && !parameterInitialized; j++) {
                                if (currentParameters[parameterIndex].value.reference == action.parameters[j].reference) {
                                    arguments[currentParameters[parameterIndex].reference] = {
                                        isParam: true,
                                        value: action.parameters[j]
                                    };
                                    parameterInitialized = true;
                                }
                            }

                            /* if it hasn't been found, set the value directly */
                            if (!parameterInitialized) {
                                var value = currentParameters[parameterIndex].value;
                                if(value.type == 'Property') {
                                    for(var propertyIndex in $scope.properties) {
                                        if($scope.properties[propertyIndex].label == value.value.label) {
                                            value = {
                                                type: "Property",
                                                value: $scope.properties[propertyIndex]
                                            };
                                            break;
                                        }
                                    }
                                }

                                arguments[currentParameters[parameterIndex].reference] = {
                                    isParam: false,
                                    value: value
                                };
                            }
                        }
                        return arguments;
                    }

                    console.log("PRECONDITIONS");
                    /* Treating preconditions */
                    var newPreconditions = [];
                    for(var preconditionIndex in action.preconditions) {
                        for(var id in $scope.preconditions) {
                            if(action.preconditions[preconditionIndex].id === $scope.preconditions[id].id) {
                                var currentPrecondition = action.preconditions[preconditionIndex];

                                arguments = matchParameters(currentPrecondition.parameters);

                                newPreconditions[preconditionIndex] = {
                                    id: $scope.preconditions[id].id,
                                    precondition: $scope.preconditions[id],
                                    arguments: arguments
                                };
                                break;
                            }
                        }
                    }
                    action.preconditions = newPreconditions;

                    console.log("ACTIONS");
                    /* Treating actions */
                    var newActions = [];
                    for(var actionIndex in action.subActions) {
                        for(var id in $scope.actions) {
                            if(action.subActions[actionIndex].id == $scope.actions[id].id) {
                                var currentAction = action.subActions[actionIndex];

                                arguments = matchParameters(currentAction.parameters);

                                newActions[actionIndex] = {
                                    id: $scope.actions[id].id,
                                    action: $scope.actions[id],
                                    arguments: arguments
                                };

                                break;
                            }
                        }
                    }
                    action.actions = newActions;
                    console.log(action);

                    $scope.action = action;
                    EditActionFactory.setAction(action);
                },
                function(failure) {
                    console.log(failure);
                }
            );
        }
    }

    $scope.action = EditActionFactory.action;

    $scope.properties = EditActionFactory.properties();
    $scope.preconditions = EditActionFactory.preconditions();
    $scope.actions = EditActionFactory.actions();
    $scope.paramTypes = EditActionFactory.paramTypes();

    $scope.parametersFilteredByType = EditActionFactory.parametersFilteredByType;

    $scope.removeParameter = EditActionFactory.removeParameter;
    $scope.addParameter = EditActionFactory.addParameter;

    $scope.removePrecondition = EditActionFactory.removePrecondition;
    $scope.addPrecondition = EditActionFactory.addPrecondition;

    $scope.removeAction = EditActionFactory.removeAction;
    $scope.addAction = EditActionFactory.addAction;

    /* Sending the action to update the action */
    $scope.submitAction = function() {
        EditActionFactory.setAction($scope.action);
        EditActionFactory.submitAction(
            $scope.action,
            function(success) {
                console.log(success);
                $location.path("/");
            }, function(failure) {
                $scope.error = failure.data;
                console.log(failure);
            },
            $routeParams.label
        );
    };

    initAction();
}];

/*
Controller managing the display of an action
 */
var ShowActionCtrl = ['$scope', '$routeParams', 'RestFactory', function($scope, $routeParams, NodesFactory) {
    $scope.action = null;
    $scope.message = "Loading...";

    $scope.isShowingAction = function() {
        return $scope.action !== null;
    };

    var success = function(action) {
        $scope.action = action;
        console.log(action);
    };

    var error = function(response) {
        $scope.message = "Error loading";
    };

    NodesFactory.getAction($routeParams.label, success, error);
}];

/* Declares all the stuff related to angular */
angular.module('actionEditor', ["ngResource", "ngRoute"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'assets/templates/action/overview.html',
                controller: 'OverviewCtrl'
            }).
            when('/new', {
                templateUrl: 'assets/templates/action/edit_action.html',
                controller: 'NewActionCtrl'
            }).
            when('/:label', {
                templateUrl: 'assets/templates/action/show_action.html',
                controller: 'ShowActionCtrl'
            }).
            when('/:label/edit', {
                templateUrl: 'assets/templates/action/edit_action.html',
                controller: 'EditActionCtrl'
            }).
            otherwise({
                redirectTo: '/'
            })
    }])
    .filter('displayListParameter', function() {
        return function(input) {
            return input
                .map(function(parameter) {
                    if(parameter.isParam) {
                        return parameter.value.reference + ": " + parameter.value.type;
                    } else {
                        if(parameter.value.type === "Property") {
                            return parameter.value.value.label + ": " + parameter.value.type;
                        } else {
                            return parameter.value.value;
                        }
                    }
                })
                .join(", ");
        };
    })
    .factory('RestFactory', RestFactory)
    .controller('OverviewCtrl', OverviewCtrl)
    .controller('ShowActionCtrl', ShowActionCtrl)
    .factory('EditActionFactory', EditActionFactory)
    .controller('NewActionCtrl', NewActionCtrl)
    .controller('EditActionCtrl', EditActionCtrl);
    
