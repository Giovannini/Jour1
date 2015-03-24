var baseUrl = "/";

var Scopes = [function() {
    var scopes = {};
    /* The keys are viewer, editNode and search */

    return {
        add: function(key, scope) {
            scopes[key] = scope;
        },
        get: function(key) {
            return scopes[key];
        }
    }
}];

var NodesFactory = ['$resource', '$routeParams', 'Scopes', function($resource, $routeParams, Scopes) {
    var currentNode = -1;
    var _displayedNodes = [];
    var _edges = [];
    var _properties = {};
    var _preconditions = {};
    var options = {
        deepness: 1
    };

    var search = $resource(
        baseUrl+'graph/:search/:deepness',
        { 'search': $routeParams.label, 'deepness': options.deepness },
        { 'get': { method: "GET", Accept: "application/json" } }
    );

    var getCurrentNode = function() {
        for(var id in _displayedNodes) {
            if(_displayedNodes.hasOwnProperty(id) && _displayedNodes[id].id === currentNode) {
                return _displayedNodes[id];
            }
        }
        return null;
    };

    var getProperty = function(id, success, failure) {
        if(_properties.hasOwnProperty(id)) {
            success(_properties[id]);
        } else {
            var request = $resource(
                baseUrl+'graph/properties/:propertyId',
                { 'propertyId': "0" },
                { 'get': { method: "GET", Accept: "application/json" } }
            );
            request.get(
                { propertyId: id },
                function(response) {
                    _properties[id] = response;
                    success(response);
                },
                function(response) {
                    failure(response);
                }
            );
        }
    };

    var getProperties = function getProperties(success, failure) {
        var request = $resource(
            baseUrl+'graph/properties',
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
                success(response);
            },
            function(response) {
                failure(response);
            }
        )
    };

    var getPreconditions = function getPreconditions(success, failure) {
        var request = $resource(
            baseUrl+'graph/preconditions',
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
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

    var getActions = function getActions(success, failure) {
        var request = $resource(
            baseUrl+'graph/actions',
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
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

    var getRelation = function(label, success, failure) {
        var request = $resource(
            baseUrl+'graph/action/:label',
            { label: "" },
            { 'get': { method: "GET", Accept: "application/json" } }
        );
        request.get(
            {label: label},
            function(edge) {
                success(edge);
            },
            function(response) {
                failure(response);
            }
        )
    };

    /**
     * Triggers a research to display a new set of nodes
     */
    var searchNodes = function(success, error) {
        return function (searchLabel, displayLabel, deepness, updateGraph) {
            search.get(
                {'search': searchLabel, 'deepness': deepness},
                function (res) {
                    var root = null,
                        display = null;

                    var nodes = [];
                    res.nodes.forEach(function (val) {
                        if (nodes.filter(function (node) {
                                return node.id === val.id;
                            }).length <= 0) {
                            if (val.label === searchLabel) {
                                val.root = true;
                                root = val;
                            }
                            if(val.label === displayLabel) {
                                display = val;
                            }
                            nodes.push(val);
                        }
                    });

                    var edges = [];
                    res.edges.forEach(function (val) {
                        edges.push(val);
                    });

                    if(updateGraph) {
                        _edges = edges;
                        _displayedNodes = nodes;
                        currentNode = root.id;
                        Scopes.get('viewer').$emit('Viewer_displayNodes');
                    }

                    success(root, display);
                },
                function (res) {
                    var message;
                    if (res.status === 404) {
                        message = searchLabel + " est introuvable.";
                    } else {
                        message = "Erreur inconnue.";
                    }
                    error(message);
                }
            );
        }
    };

    return {
        getCurrentNode: getCurrentNode,
        setCurrentNode: function(nodeId) { currentNode = nodeId; },
        setEdges: function(edges) { _edges = edges; },
        getEdges: function() { return _edges; },
        setDisplayedNodes: function(displayedNodes) { _displayedNodes = displayedNodes; },
        getDisplayedNodes: function() { return _displayedNodes; },
        getProperty: getProperty,
        getProperties: getProperties,
        getPreconditions: getPreconditions,
        getActions: getActions,
        getRelation: getRelation,
        options: options,
        searchNodes: searchNodes
    }
}];

var ViewerController = ['$scope', '$location', '$rootScope', 'Scopes', 'NodesFactory', function($scope, $location, $rootScope, Scopes, NodesFactory) {
    Scopes.add('viewer', $scope);
    $scope.deepness = NodesFactory.options.deepness;

    $scope.updateDeepness = function() {
        NodesFactory.options.deepness = $scope.deepness;
    };

    var selectNode = function(label) {
        $location.path('/node/'+Scopes.get('search').search+'/'+label);
        $rootScope.$apply();
    };

    var selectEdge = function(label) {
        $location.path('/relation/'+label);
        $rootScope.$apply();
    };

    var lastClick = 0;
    var alchemy = null;

    var toAlchemy = function(nodes, edges) {
        return {
            nodes: nodes,
            edges: edges
        }
    };

    var getColor = function(node) {
        return node.getProperties().display.color;
    };

    var nodeClick = function(node) {
        var label = node.getProperties().label;
        if(lastClick === label) {
            Scopes.get('search').$emit('Search_searchNode', label);
            lastClick = 0;
        } else {
            lastClick = label;
        }
        selectNode(label);
    };

    var edgeClick = function(edge) {
        var label = edge.getProperties().label;
        selectEdge(label);
    };

    var displayNodes = function() {

        var config = {
            divSelector: "#alchemy",
            dataSource: toAlchemy(NodesFactory.getDisplayedNodes(), NodesFactory.getEdges()),

            /* Labels */
            nodeCaption: "label",
            edgeCaption: "label",
            directedEdges: true,

            /* Positionning */
            forceLocked: false,

            /* Graph size */
            graphHeight: function(){
                return document.getElementById("alchemy").offsetHeight;
            },

            /* Styling */
            "backgroundColour": null,
            nodeStyle: {
                all: {
                    borderWidth: 0,
                    color: getColor,
                    captionSize: function() { return 10 },
                    opacity: 1,
                    radius: 18,
                    selected: {
                        color: getColor
                    },
                    highlighted: {
                        color: getColor
                    }
                }
            },
            edgeStyle: {
                all: {
                    color: "#999999",
                    width: 1,
                    opacity: 1,
                    curved: false,
                    directed: false
                }
            },

            /* events */
            nodeClick: nodeClick,
            edgeClick: edgeClick
        };

        document.getElementById("alchemy").innerHTML = "";
        alchemy = new Alchemy(config);
    };

    $scope.$on('Viewer_displayNodes', function(event) {
        displayNodes();
    });
}];

var PropertyDirective = ['NodesFactory', function(NodesFactory) {
    var link = function($scope, element, attrs) {
        var propertyId = attrs.propertyId;
        NodesFactory.getProperty(
            propertyId,
            function(property) {
                $scope.isDefined = true;
                $scope.label = property.label;
            },
            function(response) {
                $scope.isDefined = false;
                $scope.error = "Undefined property";
            }
        )
    };

    return {
        restrict: 'E',
        templateUrl: baseUrl+'assets/templates/directives/property.html',
        link: link
    }
}];

var SearchController = [
    '$scope',
    '$rootScope',
    '$location',
    'Scopes',
    function($scope, $rootScope, $location, Scopes) {
        Scopes.add('search', $scope);

        $scope.launchSearch = function() {
            $location.path("/node/"+$scope.search);
            $rootScope.$apply();
        };

        Scopes.get('search').$on('Search_searchNode', function(event, search) {
            $scope.search = search;
            $scope.launchSearch();
        });
    }
];

var OverviewCtrl = ['$scope', '$location', function($scope, $location) {
    $scope.search_relation = function() {
        $location.path("/relation/"+$scope.relation_search);
    };

    $scope.search_node = function() {
        $location.path("/node/"+$scope.node_search);
    };
}];

var EditRelationFactory = ['NodesFactory', function(NodesFactory) {
    var _paramTypes,
        _properties,
        _preconditions,
        _actions,
        _relation,
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
    _properties = [];
    NodesFactory.getProperties(
        function(properties) {
            _properties = properties;
            _updateProperties(_properties);
        },
        function(failure) {
            _notifyError("Impossible to load properties");
        }
    );

    /* Get all actions */
    _actions = [];
    NodesFactory.getActions(
        function(actions) {
            _actions = actions;
            _updateActions(_actions);
        },
        function(failure) {
            _notifyError("Impossible to load actions");
        }
    );

    _preconditions = [];
    NodesFactory.getPreconditions(
        function(preconditions) {
            _preconditions = preconditions;
            _updatePreconditions(_preconditions);
        },
        function(failure) {
            _notifyError("Impossible to load preconditions");
        }
    );

    var parametersFilteredByType = function(type) {
        return _relation.parameters.filter(function(param) {
            return param.type == type;
        });
    };

    var removeParameter = function(parameterIndex) {
        _relation.parameters.splice(parameterIndex, 1);
    };

    var addParameter = function() {
        _relation.parameters.push({
            reference: "",
            type: _paramTypes[0]
        });
    };

    var removePrecondition = function(preconditionIndex) {
        _relation.preconditions.splice(preconditionIndex, 1);
    };

    var addPrecondition = function() {
        _relation.preconditions.push({
            precondition: _preconditions[0],
            arguments: []
        });
        initPrecondition(_relation.preconditions.length - 1);
    };

    var removeAction = function(actionIndex) {
        _relation.actions.splice(actionIndex, 1);
    };

    var addAction = function() {
        _relation.actions.push({
            action: _actions[0],
            arguments: ""
        });
    };

    var submitRelation = function() {
        console.log(_relation);
        console.log(_relation.stringify());
    };

    var initPrecondition = function(index) {
        if(_relation.preconditions[index].arguments.length <= 0) {
            var arguments = [];
            for (var param in _relation.preconditions[index].precondition.parameters) {
                arguments.push({
                    isParam: false,
                    value: 0
                });
            }
            _relation.preconditions[index].arguments = arguments;
        }
    };

    var init = function init(
            notifyError,
            updateProperties,
            updatePreconditions,
            updateActions
    ) {
        _relation = {
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
        relation: function() { return _relation; },
        setRelation: function(relation) { _relation = relation; },
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
        initPrecondition: initPrecondition,
        removeAction: removeAction,
        addAction: addAction,
        submitRelation: submitRelation
    };
}];

var NewRelationCtrl = ['$scope', 'EditRelationFactory', function($scope, EditRelationFactory) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";

    EditRelationFactory.init(
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

    $scope.relation = EditRelationFactory.relation();
    $scope.properties = EditRelationFactory.properties();
    $scope.preconditions = EditRelationFactory.preconditions();
    $scope.actions = EditRelationFactory.actions();
    $scope.paramTypes = EditRelationFactory.paramTypes();
    $scope.parametersFilteredByType = EditRelationFactory.parametersFilteredByType;
    $scope.removeParameter = EditRelationFactory.removeParameter;
    $scope.addParameter = EditRelationFactory.addParameter;
    $scope.removePrecondition = EditRelationFactory.removePrecondition;
    $scope.initPrecondition = EditRelationFactory.initPrecondition;
    $scope.addPrecondition = EditRelationFactory.addPrecondition;
    $scope.removeAction = EditRelationFactory.removeAction;
    $scope.addAction = EditRelationFactory.addAction;
    $scope.submitRelation = function() {
        EditRelationFactory.setRelation($scope.relation);
        EditRelationFactory.submitRelation();
    }
}];

var EditRelationCtrl = ['$scope', '$routeParams', 'NodesFactory', 'EditRelationFactory', function($scope, $routeParams, NodesFactory, EditRelationFactory) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/relation/"+$routeParams.label;
    $scope.relation  = {};
    var relationInitialized = false;

    EditRelationFactory.init(
        function(error) {
            $scope.error = error;
        },
        function updateProperties(properties) {
            $scope.properties = properties;
            initRelation();
        },
        function updatePreconditions(preconditions) {
            $scope.preconditions = preconditions;
            initRelation();
        },
        function updateActions(actions) {
            $scope.actions = actions;
            console.log("initActions", actions);
            initRelation();
        }
    );

    function initRelation() {
        if(!relationInitialized
                && $scope.properties.length > 0
                && $scope.preconditions.length > 0
                && $scope.actions.length > 0) {
            relationInitialized = true;
            NodesFactory.getRelation(
                $routeParams.label,
                function(relation) {
                    /* Transform the relation to an object with the form :
                     *      {
                     *          label: relation_label,
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

                    /* match parameters to arguments */
                    function matchParameters(currentParameters) {
                        /* Find a parameter matching for each parameters */
                        var arguments = {};
                        for (var parameterIndex in currentParameters) {
                            var parameterInitialized = false;
                            /* First look in the parameters of the relation */
                            for (var j = 0; j < relation.parameters.length && !parameterInitialized; j++) {
                                if (currentParameters[parameterIndex].value.reference == relation.parameters[j].reference) {
                                    arguments[currentParameters[parameterIndex].reference] = {
                                        isParam: true,
                                        value: relation.parameters[j]
                                    };
                                    parameterInitialized = true;
                                }
                            }

                            /* if it hasn't been found, set the value directly */
                            if (!parameterInitialized) {
                                arguments[currentParameters[parameterIndex].reference] = {
                                    isParam: false,
                                    value: relation.parameters[j]
                                }
                            }
                        }
                        return arguments;
                    }

                    /* Treating preconditions */
                    var newPreconditions = [];
                    for(var preconditionIndex in relation.preconditions) {
                        for(var id in $scope.preconditions) {
                            if(relation.preconditions[preconditionIndex].id === $scope.preconditions[id].id) {
                                var currentPrecondition = relation.preconditions[preconditionIndex];

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
                    relation.preconditions = newPreconditions;

                    /* Treating actions */
                    var newActions = [];
                    console.log(relation.subActions);
                    console.log($scope.actions);
                    for(var actionIndex in relation.subActions) {
                        for(var id in $scope.actions) {
                            if(relation.subActions[actionIndex].id == $scope.actions[id].id) {
                                var currentAction = relation.subActions[actionIndex];

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
                    console.log(newActions);
                    relation.actions = newActions;

                    $scope.relation = relation;
                    EditRelationFactory.setRelation(relation);
                },
                function(failure) {
                    console.log(failure);
                }
            );
        }
    }

    $scope.relation = EditRelationFactory.relation;
    $scope.properties = EditRelationFactory.properties();
    $scope.preconditions = EditRelationFactory.preconditions();
    $scope.actions = EditRelationFactory.actions();
    $scope.paramTypes = EditRelationFactory.paramTypes();
    $scope.parametersFilteredByType = EditRelationFactory.parametersFilteredByType;
    $scope.removeParameter = EditRelationFactory.removeParameter;
    $scope.addParameter = EditRelationFactory.addParameter;
    $scope.removePrecondition = EditRelationFactory.removePrecondition;
    $scope.addPrecondition = EditRelationFactory.addPrecondition;
    $scope.removeAction = EditRelationFactory.removeAction;
    $scope.addAction = EditRelationFactory.addAction;
    $scope.submitRelation = function() {
        EditRelationFactory.setRelation($scope.relation);
        EditRelationFactory.submitRelation();
    };

    initRelation();
}];

var ShowRelationCtrl = ['$scope', '$routeParams', 'NodesFactory', function($scope, $routeParams, NodesFactory) {
    $scope.relation = null;
    $scope.message = "Loading...";

    $scope.isShowingRelation = function() {
        return $scope.relation !== null;
    };

    var success = function(relation) {
        $scope.relation = relation;
        console.log($scope.relation);
    };

    var error = function(response) {
        $scope.message = "Error loading";
    };

    NodesFactory.getRelation($routeParams.label, success, error);
}];

var EditNodeFactory = ['$routeParams', '$resource', 'NodesFactory', function($routeParams, $resource, NodesFactory) {
    var _node,
        _properties,
        _notifyError,
        _updateProperties,
        _submitUrl;

    _properties = null;
    NodesFactory.getProperties(
        function(properties) {
            _properties = properties;
            _updateProperties(_properties);
        },
        function(failure) {
            _notifyError("Impossible to load properties")
        }
    );

    var init = function(notifyError, updateProperties) {
        _node = {
            label: "",
            properties: [],
            rules: [],
            displayProperty: {
                color: "",
                zindex: ""
            }
        };

        _notifyError = notifyError;
        _updateProperties = updateProperties;
        if(_properties != null) {
            _updateProperties(_properties);
        }
    };

    var setNode = function(node) {
        _node = node;
    };

    var addProperty = function() {
        _node.properties.push({});
    };

    var removeProperty = function(propertyId) {
        _node.properties.splice(propertyId, 1);
    };

    var newRuleType = function(ruleId) {
        _node.rules[ruleId].value = _node.rules[ruleId].property.defaultValue;
    };

    var addRule = function() {
        _node.rules.push({
            property: _properties[0],
            value: ""
        });
        newRuleType(_node.rules.length - 1);
    };

    var removeRule = function(ruleId) {
        _node.rules.splice(ruleId, 1);
    };

    var submitNode = function(url) {
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
                _node,
                function (response) {
                    console.log(response);
                }, function (response) {
                    _notifyError("Impossible to save node");
                }
            )
        }
    };

    return {
        node: function() { return _node; },
        properties: function() { return _properties; },
        init: init,
        setNode: setNode,
        addProperty: addProperty,
        removeProperty: removeProperty,
        addRule: addRule,
        removeRule: removeRule,
        submitNode: submitNode
    };
}];

var NewNodeCtrl = ['$scope', '$routeParams', 'EditNodeFactory', function($scope, $routeParams, EditNodeFactory) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";
    $scope.message = "Loading...";

    EditNodeFactory.init(
        function(error) {
            $scope.error = error;
        },
        function(properties) {
            $scope.properties = properties;
        }
    );

    $scope.node = EditNodeFactory.node();
    $scope.properties = EditNodeFactory.properties();

    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };

    $scope.submitNode = function() {
        EditNodeFactory.setNode($scope.node);
        EditNodeFactory.submitNode(baseUrl+'graph/node/new')();
    }
}];

var EditNodeCtrl = ['$scope', '$routeParams', 'Scopes', 'NodesFactory', 'EditNodeFactory', function($scope, $routeParams, Scopes, NodesFactory, EditNodeFactory) {
    Scopes.get('search').search = $routeParams.label;
    $scope.submit_button = "Edit";
    $scope.back_url = "#/node/"+$routeParams.label;
    $scope.message = "Loading...";

    var searchNode = function() {
        NodesFactory.searchNodes(
            function (root, display) {
                for (var ruleIndex in display.rules) {
                    for (var i = 0; i < $scope.properties.length; i++) {
                        if (display.rules[ruleIndex].property.label === $scope.properties[i].label) {
                            display.rules[ruleIndex].property = $scope.properties[i];
                        }
                    }
                }

                for (var propertyIndex in display.properties) {
                    for (var i = 0; i < $scope.properties.length; i++) {
                        if (display.properties[propertyIndex].label === $scope.properties[i].label) {
                            display.properties[propertyIndex] = $scope.properties[i];
                        }
                    }
                }

                display.displayProperty = display.display;
                delete display.display;

                $scope.node = display;
                EditNodeFactory.setNode(display);
            },
            function (error) {
                $scope.error = error;
            }
        )($routeParams.label, $routeParams.label, NodesFactory.options.deepness, true);
    };

    EditNodeFactory.init(
        function(error) {
            $scope.error = error;
        },
        function(properties) {
            $scope.properties = properties;
            searchNode();
        }
    );

    $scope.properties = EditNodeFactory.properties();
    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };
    $scope.submitNode = function() {
        EditNodeFactory.setNode($scope.node);
        EditNodeFactory.submitNode(baseUrl+'graph/node/'+$routeParams.label+'/edit')();
    }
}];

var ShowNodeCtrl = ['$scope', '$rootScope', '$location', '$routeParams', '$resource', 'Scopes', 'NodesFactory', function($scope, $rootScope, $location, $routeParams, $resource, Scopes, NodesFactory) {
    $scope.node = NodesFactory.getCurrentNode();
    $scope.message = "Loading...";
    $scope.back_url = "#/";


    Scopes.get('search').search = $routeParams.label;

    $scope.isShowingNode = function() {
        return $scope.node != null;
    };

    var success = function(root, display) {
        $scope.node = display;
    };

    var error = function(message) {
        $scope.message = message;
    };

    if($routeParams.hasOwnProperty('display')) {
        if($scope.node == null) {
            NodesFactory.searchNodes(success, error)($routeParams.label, $routeParams.display, NodesFactory.options.deepness, true);
        } else {
            NodesFactory.searchNodes(success, error)($routeParams.display, $routeParams.display, 0, false);
        }
    } else {
        NodesFactory.searchNodes(success, error)($routeParams.label, $routeParams.label, NodesFactory.options.deepness, true);
    }
}];

angular.module('graphEditor', ["ngResource", "ngRoute"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when('/overview', {
                templateUrl: 'assets/templates/graph/overview.html',
                controller: 'OverviewCtrl'
            }).
            when('/relation/new', {
                templateUrl: 'assets/templates/graph/relation/edit_relation.html',
                controller: 'NewRelationCtrl'
            }).
            when('/relation/:label', {
                templateUrl: 'assets/templates/graph/relation/show_relation.html',
                controller: 'ShowRelationCtrl'
            }).
            when('/relation/:label/edit', {
                templateUrl: 'assets/templates/graph/relation/edit_relation.html',
                controller: 'EditRelationCtrl'
            }).
            when('/node/new', {
                templateUrl: 'assets/templates/graph/node/edit_node.html',
                controller: 'NewNodeCtrl'
            }).
            when('/node/:label', {
                templateUrl: 'assets/templates/graph/node/show_node.html',
                controller: 'ShowNodeCtrl'
            }).
            when('/node/:label/edit', {
                templateUrl: 'assets/templates/graph/node/edit_node.html',
                controller: 'EditNodeCtrl'
            }).
            when('/node/:label/:display', {
                templateUrl: 'assets/templates/graph/node/show_node.html',
                controller: 'ShowNodeCtrl'
            }).
            otherwise({
                redirectTo: '/overview'
            });
    }])
    .factory('Scopes', Scopes)
    .factory('NodesFactory', NodesFactory)
    .directive('property', PropertyDirective)
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
    .controller('ViewerController', ViewerController)
    .controller('SearchController', SearchController)
    .controller('OverviewCtrl', OverviewCtrl)
    .controller('ShowRelationCtrl', ShowRelationCtrl)
    .factory('EditRelationFactory', EditRelationFactory)
    .controller('NewRelationCtrl', NewRelationCtrl)
    .controller('EditRelationCtrl', EditRelationCtrl)
    .controller('ShowNodeCtrl', ShowNodeCtrl)
    .factory('EditNodeFactory', EditNodeFactory)
    .controller('NewNodeCtrl', NewNodeCtrl)
    .controller('EditNodeCtrl', EditNodeCtrl);