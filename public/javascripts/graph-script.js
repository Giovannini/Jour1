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
            } }
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

    $scope.select = function(label) {
        $location.path('/node/'+Scopes.get('search').search+'/'+label);
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
        $scope.select(label);
    };

    var edgeClick = function(edge) {
        var label = edge.getProperties().label;
        Scopes.get('editNode').$emit('EditNode_searchEdge', label);
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

        Scopes.get('search').$on('Search_searchNode', function(event, search) {
            $scope.search = search;
            $location.path("/node/"+$scope.search);
            $rootScope.$apply();
        });
    }
];

var EditNodeController = ['$scope', 'Scopes', 'NodesFactory', function($scope, Scopes, NodesFactory) {
    Scopes.add('editNode', $scope);
    //$scope.NodesFactory = NodesFactory;
    $scope.displayedNodes = [];
    $scope.nodeSelected = null;
    $scope.relationSelected = null;

    $scope.isShowingNode = function() {
        return $scope.nodeSelected != null;
    };

    $scope.isShowingRelation = function() {
        return $scope.relationSelected != null;
    };

    var init = function() {
        $scope.nodeSelected = null;
        $scope.displayedNodes = NodesFactory.getDisplayedNodes();
    };

    var updateNode = function() {
        $scope.relationSelected = null;
        $scope.nodeSelected = NodesFactory.getCurrentNode();
        $scope.$apply();
    };

    var updateRelation = function(relationLabel) {
        NodesFactory.getRelation(
            relationLabel,
            function(relation) {
                $scope.nodeSelected = null;
                $scope.relationSelected = relation;
            },
            function(response) {

            }
        );
    };

    $scope.$on('EditNode_init', function(event) {
        init();
    });

    $scope.$on('EditNode_updateNode', function(event) {
        updateNode();
    });

    $scope.$on('EditNode_searchEdge', function(event, relationLabel) {
        updateRelation(relationLabel);
    });
}];

var OverviewCtrl = ['$scope', 'NodesFactory', function($scope, NodesFactory) {

}];

var NewRelationCtrl = ['$scope', 'NodesFactory', function($scope, NodesFactory) {

}];

var ShowRelationCtrl = ['$scope', '$routeParams', 'NodesFactory', function($scope, $routeParams, NodesFactory) {

}];

var EditRelationCtrl = ['$scope', '$routeParams', 'NodesFactory', function($scope, $routeParams, NodesFactory) {

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

var EditNodeCtrl = ['$scope', '$routeParams', 'Scopes', '$resource', 'NodesFactory', function($scope, $routeParams, Scopes, $resource, NodesFactory) {
    Scopes.get('search').search = $routeParams.label;

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
                console.log(response);
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
                templateUrl: 'assets/templates/graph/relation/new_relation.html',
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
    .controller('EditNodeController', EditNodeController)
    .controller('SearchController', SearchController)
    .controller('OverviewCtrl', OverviewCtrl)
    .controller('NewRelationCtrl', NewRelationCtrl)
    .controller('ShowRelationCtrl', ShowRelationCtrl)
    .controller('EditRelationCtrl', EditRelationCtrl)
    .controller('NewNodeCtrl', NewNodeCtrl)
    .controller('ShowNodeCtrl', ShowNodeCtrl)
    .controller('EditNodeCtrl', EditNodeCtrl);