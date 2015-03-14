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

var NodesFactory = ['$resource', function($resource) {
    var currentNode = -1;
    var _displayedNodes = [];
    var _edges = [];
    var _properties = {};
    var options = {
        deepness: 2
    };

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
                'graph/property/:propertyId',
                { 'propertyId': "0" },
                { 'get': { method: "GET" } }
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

    var getRelation = function(label, success, failure) {
        var request = $resource(
            'graph/action/:label',
            { label: "" },
            { 'get': { method: "GET" } }
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

    return {
        getCurrentNode: getCurrentNode,
        setCurrentNode: function(nodeId) { currentNode = nodeId; },
        setEdges: function(edges) { _edges = edges; },
        getEdges: function() { return _edges; },
        setDisplayedNodes: function(displayedNodes) { _displayedNodes = displayedNodes; },
        getDisplayedNodes: function() { return _displayedNodes; },
        getProperty: getProperty,
        getRelation: getRelation,
        options: options
    }
}];

var ViewerController = ['$scope', 'Scopes', 'NodesFactory', function($scope, Scopes, NodesFactory) {
    Scopes.add('viewer', $scope);
    $scope.deepness = NodesFactory.options.deepness;

    $scope.updateDeepness = function() {
        NodesFactory.options.deepness = $scope.deepness;
    };

    $scope.select = function(nodeId) {
        NodesFactory.setCurrentNode(nodeId);
        Scopes.get('editNode').$emit('EditNode_updateNode');
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
        var id = node.getProperties().id;
        if(lastClick === id) {
            Scopes.get('search').$emit('Search_searchNode', node.getProperties().label);
            lastClick = 0;
        } else {
            $scope.select(id);
            lastClick = id;
        }
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
        templateUrl: 'assets/javascripts/templates/property.html',
        link: link
    }
}];

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
        $scope.nodeSelected = NodesFactory.getCurrentNode();
        $scope.$apply();
    };

    var updateRelation = function(relationLabel) {
        NodesFactory.getRelation(
            relationLabel,
            function(relation) {
                $scope.relationSelected = relation;
                $scope.$apply();
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

var SearchController = [
    '$scope',
    '$resource',
    'Scopes',
    'NodesFactory',
    function($scope, $resource, Scopes, NodesFactory) {
        Scopes.add('search', $scope);

        var search = $resource(
            'graph/:search/:deepness',
            { 'search': "", 'deepness': NodesFactory.options.deepness },
            { 'get': { method: "GET" } }
        );

        $scope.launchSearch = function() {
            search.get(
                {"search": $scope.search, "deepness": NodesFactory.options.deepness},
                function(res) {
                    $scope.error = "";

                    var nodes = [];
                    res.nodes.forEach(function(val) {
                        if(nodes.filter(function(node) { return node.id === val.id; }).length <= 0) {
                            if(val.label === $scope.search) {
                                val.root = true;
                            }
                            nodes.push(val);
                        }
                    });
                    NodesFactory.setDisplayedNodes(nodes);

                    var edges = [];
                    res.edges.forEach(function(val) {
                        edges.push(val);
                    });
                    NodesFactory.setEdges(edges);

                    Scopes.get('viewer').$emit('Viewer_displayNodes');
                    Scopes.get('editNode').$emit('EditNode_init');
                },
                function(res) {
                    if(res.status === 404) {
                        $scope.error = $scope.search +" est introuvable.";
                    } else {
                        $scope.error = "Erreur inconnue.";
                    }
                }
            );
        };

        Scopes.get('search').$on('Search_searchNode', function(event, search) {
            $scope.search = search;
            $scope.launchSearch();
        });
    }
];

angular.module('graphEditor', ["ngResource"])
    .factory('Scopes', Scopes)
    .factory('NodesFactory', NodesFactory)
    .directive('property', PropertyDirective)
    .controller('ViewerController', ViewerController)
    .controller('EditNodeController', EditNodeController)
    .controller('SearchController', SearchController);