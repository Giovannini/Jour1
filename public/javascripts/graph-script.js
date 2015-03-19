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

var OverviewCtrl = ['$scope', 'NodesFactory', function($scope, NodesFactory) {

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

    _relation = {
        label: "",
        preconditions: [],
        parameters: [],
        actions: []
    };

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
            updateActions,
            relation
    ) {
        _notifyError = notifyError;
        _updateProperties = updateProperties;
        _updatePreconditions = updatePreconditions;
        _updateActions = updateActions;
        if(typeof relation !== "undefined" && relation !== null) {
            _relation = relation;
        }
    };

    return {
        relation: _relation,
        setRelation: function(relation) { _relation = relation; },
        properties: _properties,
        preconditions: _preconditions,
        actions: _actions,
        paramTypes: _paramTypes,
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

    $scope.relation = EditRelationFactory.relation;
    $scope.properties = [];
    $scope.preconditions = [];
    $scope.actions = [];
    $scope.paramTypes = EditRelationFactory.paramTypes;
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

var EditRelationCtrl = ['$scope', '$routeParams', 'NodesFactory', 'EditRelationFactory', function($scope, $routeParams, NodesFactory, EditRelationFactory) {
    $scope.submit_button = "Edit";
    $scope.back_url = "#/relation"+$routeParams.label;
    $scope.relation  = {};

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
            initRelation();
        }
    );

    function initRelation() {
        if($scope.properties != null
                && $scope.preconditions != null
                && $scope.actions != null) {
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
                        var arguments = [];
                        for (var parameterIndex in currentParameters) {
                            var parameterInitialized = false;
                            /* First look in the parameters of the relation */
                            for (var j = 0; j < relation.parameters.length && !parameterInitialized; j++) {
                                if (currentParameters[parameterIndex].reference == relation.parameters[j].reference) {
                                    arguments[parameterIndex] = {
                                        isParam: true,
                                        value: relation.parameters[j]
                                    };
                                    parameterInitialized = true;
                                }
                            }

                            /* if it hasn't been found, set the value directly */
                            if (!parameterInitialized) {
                                arguments[parameterIndex] = {
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
                    relation.actions = newActions;

                    $scope.relation = relation;
                    console.log(relation);
                    EditRelationFactory.setRelation(relation);
                },
                function(failure) {
                    console.log(failure);
                }
            );
        }
    }

    $scope.relation = EditRelationFactory.relation;
    $scope.properties = [];
    $scope.preconditions = [];
    $scope.actions = [];
    $scope.paramTypes = EditRelationFactory.paramTypes;
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
}];

var NewNodeCtrl = ['$scope', '$routeParams', '$resource', 'NodesFactory', function($scope, $routeParams, $resource, NodesFactory) {
    $scope.node = {
        label: "",
        properties: [],
        rules: [],
        displayProperty: {
            color: "",
            zindex: ""
        }
    };
    $scope.submit_button = "Edit";
    $scope.back_url = "#/";

    NodesFactory.getProperties(
        function(properties) {
            $scope.properties = properties;
        },
        function(failure) {

        }
    );

    var submit = $resource(
        baseUrl+'graph/node/new',
        {},
        {
            'save': {
                method: "POST",
                headers: [{'Content-Type': 'application/json'}]
            }
        }
    );

    $scope.addProperty = function() {
        $scope.node.properties.push({});
    };

    $scope.removeProperty = function(propertyId) {
        $scope.node.properties.splice(propertyId, 1);
    };

    $scope.addRule = function() {
        $scope.node.rules.push({
            property: $scope.properties[0],
            value: ""
        });
        $scope.newRuleType($scope.node.rules.length - 1);
    };

    $scope.removeRule = function(ruleId) {
        $scope.node.rules.splice(ruleId, 1);
    };

    $scope.newRuleType = function(ruleId) {
        $scope.node.rules[ruleId].value = $scope.node.rules[ruleId].property.defaultValue;
    };

    $scope.submitNode = function() {
        submit.save(
            {},
            $scope.node,
            function(response) {
                console.log(response);
            }, function(response) {
                console.log(response);
            }
        )
    };
}];

var ShowNodeCtrl = ['$scope', '$rootScope', '$location', '$routeParams', '$resource', 'Scopes', 'NodesFactory', function($scope, $rootScope, $location, $routeParams, $resource, Scopes, NodesFactory) {
    $scope.node = NodesFactory.getCurrentNode();
    $scope.message = "Loading...";

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

var EditNodeCtrl = ['$scope', '$location', '$rootScope', '$routeParams', 'Scopes', '$resource', 'NodesFactory', function($scope, $location, $rootScope, $routeParams, Scopes, $resource, NodesFactory) {
    Scopes.get('search').search = $routeParams.label;
    $scope.submit_button = "Edit";
    $scope.back_url = "#/node/"+$routeParams.label;

    NodesFactory.getProperties(
        function(properties) {
            $scope.properties = properties;

            NodesFactory.searchNodes(
                function(root, display) {
                    for(var ruleIndex in display.rules) {
                        for(var i = 0; i < $scope.properties.length; i++) {
                            if(display.rules[ruleIndex].property === $scope.properties[i].id) {
                                display.rules[ruleIndex].property = $scope.properties[i];
                            }
                        }
                    }

                    for(var propertyIndex in display.properties) {
                        for(var i = 0; i < $scope.properties.length; i++) {
                            if(display.properties[propertyIndex] === $scope.properties[i].id) {
                                display.properties[propertyIndex] = $scope.properties[i];
                            }
                        }
                    }

                    display.displayProperty = display.display;
                    delete display.display;

                    $scope.node = display;
                },
                function(error) {

                }
            )($routeParams.label, $routeParams.label, NodesFactory.options.deepness, true);
        },
        function(failure) {

        }
    );

    var submit = $resource(
        baseUrl+'graph/node/'+$routeParams.label+'/edit',
        {},
        {
            'save': {
                method: "POST",
                headers: [{'Content-Type': 'application/json'}]
            }
        }
    );

    $scope.addProperty = function() {
        $scope.node.properties.push({});
    };

    $scope.removeProperty = function(propertyId) {
        $scope.node.properties.splice(propertyId, 1);
    };

    $scope.addRule = function() {
        $scope.node.rules.push({
            property: $scope.properties[0],
            value: ""
        });
        $scope.newRuleType($scope.node.rules.length - 1);
    };

    $scope.removeRule = function(ruleId) {
        $scope.node.rules.splice(ruleId, 1);
    };

    $scope.newRuleType = function(ruleId) {
        $scope.node.rules[ruleId].value = $scope.node.rules[ruleId].property.defaultValue;
    };

    $scope.submitNode = function() {
        submit.save(
            {},
            $scope.node,
            function(response) {
                $location.path("#/node/"+response.label);
                $rootScope.$apply();
            }, function(response) {
                console.log(response);
            }
        )
    };
}];


angular.module('graphEditor', ["ngResource", "ngRoute"])
    .config(['$routeProvider', function($routeProvider) {
        $routeProvider.
            when(baseUrl+'', {
                templateUrl: 'assets/templates/graph/overview.html',
                controller: 'OverviewCtrl'
            }).
            when(baseUrl+'relation/new', {
                templateUrl: 'assets/templates/graph/relation/edit_relation.html',
                controller: 'NewRelationCtrl'
            }).
            when(baseUrl+'relation/:label', {
                templateUrl: 'assets/templates/graph/relation/show_relation.html',
                controller: 'ShowRelationCtrl'
            }).
            when(baseUrl+'relation/:label/edit', {
                templateUrl: 'assets/templates/graph/relation/edit_relation.html',
                controller: 'EditRelationCtrl'
            }).
            when(baseUrl+'node/new', {
                templateUrl: 'assets/templates/graph/node/edit_node.html',
                controller: 'NewNodeCtrl'
            }).
            when(baseUrl+'node/:label', {
                templateUrl: 'assets/templates/graph/node/show_node.html',
                controller: 'ShowNodeCtrl'
            }).
            when(baseUrl+'node/:label/edit', {
                templateUrl: 'assets/templates/graph/node/edit_node.html',
                controller: 'EditNodeCtrl'
            }).
            when(baseUrl+'node/:label/:display', {
                templateUrl: 'assets/templates/graph/node/show_node.html',
                controller: 'ShowNodeCtrl'
            }).
            otherwise({
                redirectTo: baseUrl+''
            });
    }])
    .factory('Scopes', Scopes)
    .factory('NodesFactory', NodesFactory)
    .directive('property', PropertyDirective)
    .controller('ViewerController', ViewerController)
    .controller('SearchController', SearchController)
    .controller('OverviewCtrl', OverviewCtrl)
    .controller('ShowRelationCtrl', ShowRelationCtrl)
    .factory('EditRelationFactory', EditRelationFactory)
    .controller('NewRelationCtrl', NewRelationCtrl)
    .controller('EditRelationCtrl', EditRelationCtrl)
    .controller('ShowNodeCtrl', ShowNodeCtrl)
    .controller('NewNodeCtrl', NewNodeCtrl)
    .controller('EditNodeCtrl', EditNodeCtrl);