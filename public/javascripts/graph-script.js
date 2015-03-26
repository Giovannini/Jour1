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
        baseUrl+'graph/node/:search/:deepness',
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
                success(response);
            },
            function(response) {
                failure(response);
            }
        )
    };

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
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

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
                _preconditions = response;
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

    var getActionsOfConcept = function getActionsOfConcept(conceptLabel, success, failure) {
        if(conceptLabel != "") {
            var request = $resource(
                baseUrl + 'graph/node/' + conceptLabel + '/actions',
                {},
                {
                    'get': {
                        method: "GET",
                        isArray: true,
                        Accept: "application/json"
                    }
                }
            );
            request.get(
                {},
                function (response) {
                    success(response);
                },
                function (response) {
                    failure(response);
                }
            );
        } else {
            failure("");
        }
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
        getActionsOfConcept: getActionsOfConcept,
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
    $scope.search_node = function() {
        $location.path("/node/"+$scope.node_search);
    };
}];

var EditNodeFactory = ['$routeParams', '$resource', 'NodesFactory', function($routeParams, $resource, NodesFactory) {
    var _node,
        _properties,
        _actions,
        _effects,
        _notifyError,
        _updateProperties,
        _updateActions,
        _updateEffects,
        _submitUrl;

    var init = function(notifyError, updateProperties, updateActions, updateEffects, nodeLabel) {
        _node = {
            label: "",
            properties: [],
            rules: [],
            needs: [],
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

        _updateActions = updateActions;
        if(_actions != null) {
            _updateActions(_actions);
        }

        _updateEffects = updateEffects;
        if(_effects != null) {
            _updateEffects(_effects);
        }

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

        /* Get all actions */
        _actions = null;
        if(typeof nodeLabel === "undefined") {
            nodeLabel = "";
        }
        NodesFactory.getActionsOfConcept(
            nodeLabel,
            function(actions) {
                _actions = actions;
                _updateActions(_actions);
            },
            function(failure) {
                _notifyError("Impossible to load actions");
            }
        );

        _effects = null;
        NodesFactory.getActions(
            function(effects) {
                _effects = effects;
                _updateEffects(_effects);
            },
            function(failure) {
                _notifyError("Impossible to load effects");
            }
        );
        _updateEffects(_effects);
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

    var addNeed = function() {
        _node.needs.push({
            label: "",
            affectedProperty: _node.properties[0],
            priority: 0,
            consequenceSteps: [],
            meansOfSatisfaction: []
        });
    };

    var removeNeed = function(needId) {
        _node.needs.splice(needId, 1);
    };

    var addMeans = function(needId) {
        _node.needs[needId].meansOfSatisfaction.push(_actions[0]);
    };

    var removeMeans = function(needId, meansId) {
        _node.needs[needId].meansOfSatisfaction.splice(meansId, 1);
    };

    var addConsequence = function(needId) {
        _node.needs[needId].consequenceSteps.push({
            consequence: {
                effects: [$scope.effects[0]],
                severity: ""
            },
            value: ""
        });
    };

    var removeConsequence = function(needId, consequenceId) {
        _node.needs[needId].consequenceSteps.splice(consequenceId, 1);
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
        actions: function() { return _actions; },
        effects: function() { return _effects; },
        init: init,
        setNode: setNode,
        addProperty: addProperty,
        removeProperty: removeProperty,
        addRule: addRule,
        removeRule: removeRule,
        addNeed: addNeed,
        removeNeed: removeNeed,
        addMeans: addMeans,
        removeMeans: removeMeans,
        addConsequence: addConsequence,
        removeConsequence: removeConsequence,
        canAddNeeds: function() {
            return typeof _actions != "undefined"
                    && _actions != null
                    && _actions.length > 0;
        },
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
        },
        function(actions) {
            $scope.actions = actions;
        },
        function(effects) {
            $scope.effects = effects;
        }
    );

    $scope.node = EditNodeFactory.node();
    $scope.properties = EditNodeFactory.properties();

    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;
    $scope.addNeed = EditNodeFactory.addNeed;
    $scope.removeNeed = EditNodeFactory.removeNeed;

    $scope.addMeans = EditNodeFactory.addMeans;
    $scope.removeMeans = EditNodeFactory.removeMeans;
    $scope.canAddNeeds = EditNodeFactory.canAddNeeds;

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };

    $scope.submitNode = function() {
        if(!$scope.canAddNeeds) {
            $scope.node.needs = [];
        }
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
                console.log(display);


                for (var propertyIndex in display.properties) {
                    for (var i = 0; i < $scope.properties.length; i++) {
                        if (display.properties[propertyIndex].label === $scope.properties[i].label) {
                            display.properties[propertyIndex] = $scope.properties[i];
                        }
                    }
                }

                function matchProperty(property) {
                    for (var i = 0; i < $scope.properties.length; i++) {
                        if (property.label == $scope.properties[i].label) {
                            return $scope.properties[i];
                        }
                    }
                }

                function matchAction(actionId, actions) {
                    for(var i = 0; i < actions.length; i++) {
                        if(actionId == actions[i].id) {
                            return actions[i]
                        }
                    }
                }

                function matchMean(actionId) {
                    return matchAction(actionId, $scope.actions);
                }

                function matchEffect(effectId) {
                    return matchAction(effectId, $scope.effects);
                }

                for (var ruleIndex in display.rules) {
                    display.rules[ruleIndex].property = matchProperty(display.rules[ruleIndex].property);
                }

                for(var needIndex in display.needs) {
                    // Match the property
                    display.needs[needIndex].affectedProperty = matchProperty(display.needs[needIndex].affectedProperty)

                    // Match every action in the consequences
                    for(var consequenceIndex in display.needs[needIndex].consequenceSteps) {
                        for(var effectIndex in display.needs[needIndex].consequenceSteps[consequenceIndex].consequence.effects) {
                            display.needs[needIndex].consequenceSteps[consequenceIndex].consequence.effects[effectIndex] = matchEffect(display.needs[needIndex].consequenceSteps[consequenceIndex].consequence.effects[effectIndex])
                        }
                    }

                    // Match every action in the means of satisfaction
                    for(var actionIndex in display.needs[needIndex].meansOfSatisfaction) {
                        display.needs[needIndex].meansOfSatisfaction[actionIndex] = matchMean(display.needs[needIndex].meansOfSatisfaction[actionIndex]);
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

    var launchSearch = function() {
        if($scope.actions != null
            && $scope.properties != null
            && $scope.effects != null) {
            searchNode();
        }
    };

    EditNodeFactory.init(
        function(error) {
            $scope.error = error;
        },
        function(properties) {
            $scope.properties = properties;
            launchSearch();
        },
        function(actions) {
            $scope.actions = actions;
            launchSearch();
        },
        function(effects) {
            $scope.effects = effects;
            launchSearch();
        },
        $routeParams.label
    );

    $scope.properties = EditNodeFactory.properties();
    $scope.actions = EditNodeFactory.actions();
    $scope.effects = EditNodeFactory.effects();

    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;
    $scope.addNeed = EditNodeFactory.addNeed;
    $scope.removeNeed = EditNodeFactory.removeNeed;
    $scope.canAddNeeds = EditNodeFactory.canAddNeeds;

    $scope.addMeans = EditNodeFactory.addMeans;
    $scope.removeMeans = EditNodeFactory.removeMeans;

    $scope.addConsequence = EditNodeFactory.addConsequence;
    $scope.removeConsequence = EditNodeFactory.removeConsequence;

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };

    function refactorNodeToSubmit(node) {
        console.log(node);
        return node;
    }

    $scope.submitNode = function() {
        if(!$scope.canAddNeeds) {
            $scope.node.needs = [];
        }
        EditNodeFactory.setNode($scope.node);
        var nodeToSubmit = refactorNodeToSubmit($scope.node);
        //EditNodeFactory.submitNode(baseUrl+'graph/node/'+$routeParams.label+'/edit')(nodeToSubmit);
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
    .controller('ShowNodeCtrl', ShowNodeCtrl)
    .factory('EditNodeFactory', EditNodeFactory)
    .controller('NewNodeCtrl', NewNodeCtrl)
    .controller('EditNodeCtrl', EditNodeCtrl);