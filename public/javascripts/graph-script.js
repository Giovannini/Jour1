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
    var _concepts = [];
    var options = {
        deepness: 1
    };

    var search = $resource(
        baseUrl+'concepts/:search/:deepness',
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
                baseUrl+'properties/:propertyId',
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
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

    var getEffects = function getEffects(success, failure) {
        var request = $resource (
            baseUrl+'graph/effects',
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
                success(response);
            },
            function() {
                failure(response);
            }
        );
    };

    var getActionsOfConcept = function getActionsOfConcept(conceptLabel, success, failure) {
        if(conceptLabel != "") {
            var request = $resource(
                baseUrl + 'concepts/' + conceptLabel + '/actions',
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

    var getConcepts = function getConcepts(success, failure) {
        if(_concepts.length > 0) {
            success(_concepts);
        } else {
            var request = $resource(
                baseUrl + 'concepts/simple',
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
        }
    };

    var getAction = function(label, success, failure) {
        var request = $resource(
            baseUrl+'actions/:label',
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

    var getConceptsFromRelation = function(relationLabel, success, failure) {
        var request = $resource(
            baseUrl+'graph/relations/:label',
            { 'label': relationLabel },
            { 'get': { method: "GET", Accept: "application/json" } }
        );
        request.get(
            { 'label': relationLabel },
            function(response) {
                success(response);
            },
            function(response) {
                failure(response);
            }
        );
    };

    function matchNode(nodes, val) {
        for(var id in nodes) {
            if(nodes[id].id == val) {
                return nodes[id];
            }
        }
        return null;
    }

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
                    var relations = [];

                    res.edges.forEach(function (val) {
                        edges.push(val);
                        relations.push({
                            label: val.label,
                            target: matchNode(nodes, val.target)
                        })
                    });

                    if(updateGraph) {
                        _edges = edges;
                        _displayedNodes = nodes;
                        currentNode = root.id;
                        Scopes.get('viewer').$emit('Viewer_displayNodes');
                    }

                    success(root, display, relations);
                },
                function (res) {
                    var message;
                    if (res.status === 404) {
                        message = searchLabel + " not found";
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
        getEffects: getEffects,
        getActionsOfConcept: getActionsOfConcept,
        getConcepts: getConcepts,
        getAction: getAction,
        getConceptsFromRelation: getConceptsFromRelation,
        options: options,
        searchNodes: searchNodes
    }
}];

var ViewerController = ['$scope', '$location', 'Scopes', 'NodesFactory', function($scope, $location, Scopes, NodesFactory) {
    Scopes.add('viewer', $scope);
    $scope.deepness = NodesFactory.options.deepness;

    $scope.updateDeepness = function() {
        NodesFactory.options.deepness = $scope.deepness;
    };

    var selectNode = function(label) {
        $location.path('/node/'+Scopes.get('search').search+'/'+label);
    };

    var selectEdge = function(label) {
        $location.path('/relation/'+label);
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
    '$location',
    'Scopes',
    function($scope, $location, Scopes) {
        Scopes.add('search', $scope);

        $scope.launchSearch = function() {
            $location.path("/node/"+$scope.search);
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

    $scope.search_relation = function() {
        $location.path("/relation/"+$scope.relation_search);
    };
}];

var ShowNodeCtrl = ['$scope', '$location', '$routeParams', '$resource', 'Scopes', 'NodesFactory', function($scope, $location, $routeParams, $resource, Scopes, NodesFactory) {
    $scope.node = null;
    $scope.message = "Loading...";
    $scope.back_url = "#/";


    Scopes.get('search').search = $routeParams.label;

    $scope.isShowingNode = function() {
        return $scope.node != null;
    };

    $scope.deleteNode = function() {
        var request = $resource(
            baseUrl+"concepts/"+$scope.node.label,
            {},
            {
                'delete': {
                    method: "DELETE",
                    headers: [{'Content-Type': 'application/json'}]
                }
            }
        );
        request.delete(
            {},
            {},
            function(response) {
                $location.path('/overview');
            },
            function(response) {
                $scope.error = "Impossible to delete " + $scope.node.label;
            }
        );
    };

    var success = function(root, display, edges) {
        $scope.node = display;
        $scope.edges = edges.sort(function(a, b) {
            if(a.label < b.label) {
                return -1;
            } else if(a.label == b.label) {
                return 0;
            } else {
                return 1;
            }
        });
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

var EditNodeFactory = ['$routeParams', '$resource', 'NodesFactory', function($routeParams, $resource, NodesFactory) {
    var _node,
        _properties,
        _actions,
        _effects,
        _concepts,
        _notifyError,
        _updateProperties,
        _updateActions,
        _updateEffects,
        _updateConcepts;

    var init = function(notifyError, updateProperties, updateActions, updateEffects, updateConcepts, nodeLabel) {
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
        if(typeof _properties !== "undefined" && _properties != null) {
            _updateProperties(_properties);
        } else {
            _properties = null;
        }

        _updateActions = updateActions;
        if(typeof _actions !== "undefined" && _actions != null) {
            _updateActions(_actions);
        } else {
            _actions = null;
        }

        _updateEffects = updateEffects;
        if(typeof _effects !== "undefined" && _effects != null) {
            _updateEffects(_effects);
        } else {
            _effects = null;
        }

        _updateConcepts = updateConcepts;
        if(typeof _concepts !== "undefined" && _concepts != null) {
            _updateConcepts(_concepts);
        } else {
            _concepts = null;
        }

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

        NodesFactory.getEffects(
            function(effects) {
                _effects = effects;
                _updateEffects(_effects);
            },
            function(failure) {
                _notifyError("Impossible to load effects");
            }
        );
        _updateEffects(_effects);

        NodesFactory.getConcepts(
            function(concepts) {
                _concepts = concepts;
                _updateConcepts(_concepts);
            },
            function(failure) {
                _notifyError("Impossible to load concepts");
            }
        )
    };

    var setNode = function(node) {
        _node = node;
    };

    var newPropertyValue = function(propertyId) {
        for(var id in _properties) {
            if(_node.properties[propertyId].property == _properties[id].label) {
                _node.properties[propertyId].value = _properties[id].defaultValue;
                break;
            }
        }
    };

    var addProperty = function() {
        _node.properties.push({
            property: _properties[0].label,
            value: ""
        });
        newPropertyValue(_node.properties.length - 1);
    };

    var removeProperty = function(propertyId) {
        _node.properties.splice(propertyId, 1);
    };

    var newRuleValue = function(ruleId) {
        for(var id in _properties) {
            if(_node.rules[ruleId].property == _properties[id].label) {
                _node.rules[ruleId].value = _properties[id].defaultValue;
                break;
            }
        }
    };

    var addRule = function() {
        _node.rules.push({
            property: _properties[0].label,
            value: ""
        });
        newRuleValue(_node.rules.length - 1);
    };

    var removeRule = function(ruleId) {
        _node.rules.splice(ruleId, 1);
    };

    var addNeed = function() {
        _node.needs.push({
            label: "",
            affectedProperty: _node.properties[0].label,
            priority: 0,
            consequenceSteps: [],
            meansOfSatisfaction: []
        });
    };

    var removeNeed = function(needId) {
        _node.needs.splice(needId, 1);
    };

    var addMeans = function(needId) {
        _node.needs[needId].meansOfSatisfaction.push({
            action: _actions[0].id,
            concept: _concepts[0].id
        });
    };

    var removeMeans = function(needId, meansId) {
        _node.needs[needId].meansOfSatisfaction.splice(meansId, 1);
    };

    var addConsequence = function(needId) {
        _node.needs[needId].consequenceSteps.push({
            consequence: {
                effect: [_effects[0].id],
                severity: ""
            },
            value: ""
        });
    };

    var removeConsequence = function(needId, consequenceId) {
        _node.needs[needId].consequenceSteps.splice(consequenceId, 1);
    };

    var cleanNode = function(node) {
        return node;
    };

    var submitNode = function(node, url, method) {
        return function(success, failure) {
            var submit = $resource(
                url,
                {},
                {
                    'save': {
                        method: method,
                        headers: [{'Content-Type': 'application/json'}]
                    }
                }
            );
            submit.save(
                {},
                node,
                function (response) {
                    success(response);
                }, function (response) {
                    _notifyError("Impossible to save node");
                    failure(response);
                }
            )
        }
    };

    return {
        node: function() { return _node; },
        properties: function() { return _properties; },
        actions: function() { return _actions; },
        effects: function() { return _effects; },
        concepts: function() { return _concepts; },
        init: init,
        setNode: setNode,
        newPropertyValue: newPropertyValue,
        addProperty: addProperty,
        removeProperty: removeProperty,
        newRuleValue: newRuleValue,
        addRule: addRule,
        removeRule: removeRule,
        addNeed: addNeed,
        removeNeed: removeNeed,
        addMeans: addMeans,
        removeMeans: removeMeans,
        addConsequence: addConsequence,
        removeConsequence: removeConsequence,
        submitNode: submitNode,
        cleanNode: cleanNode
    };
}];

var NewNodeCtrl = ['$scope', '$routeParams', '$location', 'EditNodeFactory', function($scope, $routeParams, $location, EditNodeFactory) {
    $scope.submit_button = "Create";
    $scope.back_url = "#/";
    $scope.message = "Loading...";

    EditNodeFactory.init(
        function(error) {
            $scope.message = error;
        },
        function(properties) {
            $scope.properties = properties;
        },
        function(actions) {
            $scope.actions = actions;
        },
        function(effects) {
            $scope.effects = effects;
        },
        function(concepts) {
            $scope.concepts = concepts;
        }
    );

    $scope.node = EditNodeFactory.node();
    $scope.properties = EditNodeFactory.properties();

    $scope.newPropertyValue = EditNodeFactory.newPropertyValue;
    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.newRuleType = EditNodeFactory.newRuleType;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;
    $scope.addNeed = EditNodeFactory.addNeed;
    $scope.removeNeed = EditNodeFactory.removeNeed;

    $scope.addMeans = EditNodeFactory.addMeans;
    $scope.removeMeans = EditNodeFactory.removeMeans;
    $scope.canAddNeeds = function() {
        return typeof $scope.actions != "undefined"
            && $scope.actions != null
            && $scope.actions.length > 0;
    };

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };

    $scope.submitNode = function() {
        var nodeToSend = EditNodeFactory.cleanNode($scope.node);
        EditNodeFactory.submitNode(nodeToSend, baseUrl+'concepts/new', "POST")(
            function(response) {
                $location.path("/node/"+$scope.node.label);
            },
            function(errors) {
                $scope.message = "Failed to create node";
                $scope.errors = errors.data;
            }
        );
    }
}];

var EditNodeCtrl = ['$scope', '$routeParams', '$location', 'Scopes', 'NodesFactory', 'EditNodeFactory', function($scope, $routeParams, $location, Scopes, NodesFactory, EditNodeFactory) {
    Scopes.get('search').search = ""+$routeParams.label;
    $scope.submit_button = "Edit";
    $scope.back_url = "#/node/"+$routeParams.label;
    $scope.message = "Loading...";

    var searchNode = function() {
        NodesFactory.searchNodes(
            function (root, display) {
                for (var propertyIndex in display.properties) {
                    display.properties[propertyIndex].property = display.properties[propertyIndex].property.label;
                }

                for (var ruleIndex in display.rules) {
                    display.rules[ruleIndex].property = display.rules[ruleIndex].property.label;
                }

                for(var needIndex in display.needs) {
                    // Match the property
                    display.needs[needIndex].affectedProperty = display.needs[needIndex].affectedProperty.label;
                }

                display.displayProperty = display.display;
                delete display.display;

                $scope.node = display;
                EditNodeFactory.setNode(display);
            },
            function (error) {
                $scope.message = error;
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
            console.log(error);
            $scope.message = error;
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
        function(concepts) {
            $scope.concepts = concepts;
        },
        $routeParams.label
    );

    $scope.properties = EditNodeFactory.properties();
    $scope.actions = EditNodeFactory.actions();
    $scope.effects = EditNodeFactory.effects();
    $scope.concepts = EditNodeFactory.concepts();

    $scope.newPropertyValue = EditNodeFactory.newPropertyValue;
    $scope.addProperty = EditNodeFactory.addProperty;
    $scope.removeProperty = EditNodeFactory.removeProperty;
    $scope.newRuleType = EditNodeFactory.newRuleType;
    $scope.addRule = EditNodeFactory.addRule;
    $scope.removeRule = EditNodeFactory.removeRule;
    $scope.addNeed = EditNodeFactory.addNeed;
    $scope.removeNeed = EditNodeFactory.removeNeed;
    $scope.canAddNeeds = function() {
        console.log($scope.actions);
        return typeof $scope.actions != "undefined"
            && $scope.actions != null
            && $scope.actions.length > 0;
    };

    $scope.addMeans = EditNodeFactory.addMeans;
    $scope.removeMeans = EditNodeFactory.removeMeans;

    $scope.addConsequence = EditNodeFactory.addConsequence;
    $scope.removeConsequence = EditNodeFactory.removeConsequence;

    $scope.isShowingNode = function() {
        return typeof $scope.node !== "undefined" && $scope.node != null;
    };

    $scope.submitNode = function() {
        var nodeToSend = EditNodeFactory.cleanNode($scope.node);
        EditNodeFactory.submitNode(nodeToSend, baseUrl+'concepts/'+$routeParams.label, "PUT")(
            function(response) {
                console.log(response);
                $location.path("/node/"+$scope.node.label);
            },
            function(errors) {
                $scope.message = "Failed to create node";
                $scope.errors = errors.data;
            }
        );
    }
}];

var ShowRelationCtrl = ['$scope', '$routeParams', '$resource', 'Scopes', 'NodesFactory', function($scope, $routeParams, $resource, Scopes, NodesFactory) {
    $scope.relation = {
        label: $routeParams.label
    };
    var showRelation = false;
    $scope.message = "Loading...";

    $scope.isShowingRelation = function() {
        return showRelation;
    };

    $scope.canAddRelation = false;

    NodesFactory.getConcepts(
        function(concepts) {
            $scope.concepts = concepts;
            $scope.canAddRelation = true;
        },
        function(failure) {
            $scope.canAddRelation = false;
            $scope.errorAddRelation = "Can't get the list of concepts";
        }
    );

    NodesFactory.getConceptsFromRelation(
        $scope.relation.label,
        function(relations) {

            function getNodeById(id, nodes) {
                for(var nodeId in nodes) {
                    if(nodes[nodeId].id == id) {
                        return nodes[nodeId];
                    }
                }
                return null;
            }

            $scope.conceptRelations = [];
            for(var edgeId in relations.edges) {
                var edge = relations.edges[edgeId];
                var source = getNodeById(edge.source, relations.nodes);
                var target = getNodeById(edge.target, relations.nodes);
                $scope.conceptRelations.push({
                    source: source,
                    label: relations.edges[edgeId].label,
                    target: target
                })
            }

            NodesFactory.setDisplayedNodes(relations.nodes);
            NodesFactory.setEdges(relations.edges);
            Scopes.get('viewer').$emit('Viewer_displayNodes');

            showRelation = true;
        },
        function(failure) {
            $scope.message = $scope.relation.label + " is never used."
            showRelation = false;
        }
    );


    var request = $resource(
        baseUrl+'graph/relations/:label/:source/:target',
        {},
        {
            'create': {method: "POST"},
            'delete': {method: "DELETE"}
        }
    );

    $scope.submitNewRelation = function() {
        request.create(
            $scope.relation,
            {},
            function(result) {
                console.log(result);
            },
            function(failure) {
                console.log(failure);
            }
        );
    };

    $scope.deleteRelation = function(label, source, target) {
        request.delete(
            {
                label: label,
                source: source,
                target: target
            },
            {},
            function(result) {
                console.log(result);
            },
            function(failure) {
                console.log(failure);
            }
        );

    }

    $scope.isAction = function() {
        return $routeParams.label.startsWith("ACTION_");
    }
}];

var EditRelationFactory = ['NodesFactory', 'Scopes', function(NodesFactory, Scopes) {
    var relationTypes = ['ACTION_', 'EFFECT_', 'MOOD_'];

    var getRelation = function(label, success, failure) {
        NodesFactory.getConceptsFromRelation(
            label,
            function(relations) {
                NodesFactory.setDisplayedNodes(relations.nodes);
                NodesFactory.setEdges(relations.edges);
                Scopes.get('viewer').$emit('Viewer_displayNodes');

                success(label);
            },
            function(failure) {
                failure(failure);
            }
        );
    };

    return {
        getRelation: getRelation,
        relationTypes: relationTypes
    }
}];

var NewRelationCtrl = ['$scope', '$routeParams', 'EditRelationFactory', function($scope, $routeParams, EditRelationFactory) {
    $scope.submit_button = "Create";
    $scope.relationTypes = EditRelationFactory.relationTypes;
    $scope.back_url = "#/overview";

    $scope.relation = {
        type: $scope.relationTypes[0]
    };
}];

var EditRelationCtrl = ['$scope', '$routeParams', 'EditRelationFactory', function($scope, $routeParams, EditRelationFactory) {
    $scope.submit_button = "Edit";
    $scope.relationTypes = EditRelationFactory.relationTypes;
    $scope.relation = {};
    $scope.back_url = "#/relation/"+$routeParams.label;


    EditRelationFactory.getRelation(
        $routeParams.label,
        function(relation) {
            console.log(relation);
        },
        function(error) {
            console.log(error);
        }
    );

    for(var typeId in $scope.relationTypes) {
        if((new RegExp("^" + $scope.relationTypes[typeId])).test($routeParams.label)) {
            $scope.relation.type = $scope.relationTypes[typeId];
            break;
        }
    }

    $scope.relation.label = $routeParams.label.substr($scope.relation.type.length);


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
    .filter('formKeyFiltering', function() {
        function matchKey(key) {
            var trad = {
                "displayProperty": "Display",
                "properties": "Property",
                "label": "Name",
                "zindex": "ZIndex",
                "rules": "Rule",
                "needs": "Need",
                "affectedProperty": "Affected property",
                "priority": "Priority",
                "severity": "Severity",
                "effect": "Effect",
                "action": "Action",
                "consequenceSteps": "Consequence",
                "meansOfSatisfaction": "Mean of satisfaction"
            };
            if(trad.hasOwnProperty(key)) {
                return trad[key];
            } else {
                return key;
            }
        }

        return function(input) {
            var split = input.split(".").map(function(element) {
                var match_regex = element.match(/^([^\[]+)\[(\d+)\]$/i);
                if(match_regex) {
                    return matchKey(match_regex[1]) + " #"+match_regex[2];
                } else {
                    return matchKey(element);
                }
            });

            return split.join(": ");
        };
    })
    .filter('formErrorFiltering', function() {
        return function(input) {
            return input.map(function(input) {
                var message = input;
                switch (input) {
                    case "notFound":
                        message = "It does not exists";
                        break;
                    case "incorrectCase":
                        message = "The name must be UpperCamelCase";
                        break;
                }
                return message;
            }).join(" ");
        };
    })
    .controller('ViewerController', ViewerController)
    .controller('SearchController', SearchController)
    .controller('OverviewCtrl', OverviewCtrl)
    .controller('ShowNodeCtrl', ShowNodeCtrl)
    .factory('EditNodeFactory', EditNodeFactory)
    .controller('NewNodeCtrl', NewNodeCtrl)
    .controller('EditNodeCtrl', EditNodeCtrl)
    .controller('ShowRelationCtrl', ShowRelationCtrl)
    .factory('EditRelationFactory', EditRelationFactory)
    .controller('NewRelationCtrl', NewRelationCtrl)
    .controller('EditRelationCtrl', EditRelationCtrl);