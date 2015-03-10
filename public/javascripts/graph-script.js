var Scopes = [function() {
    var scopes = {};

    return {
        add: function(key, scope) {
            scopes[key] = scope;
        },
        get: function(key) {
            return scopes[key];
        }
    }
}];

var NodesFactory = [function() {
    var currentNode = -1;
    var _displayedNodes = [];
    var options = {
        profondeur: 1
    };

    var setCurrentNode = function() {

    };

    var getCurrentNode = function() {
        if(typeof _displayedNodes[currentNode] !== "undefined") {
            return _displayedNodes[currentNode];
        }
        return null;
    };

    return {
        getCurrentNode: getCurrentNode,
        setDisplayedNodes: function(displayedNodes) { _displayedNodes = displayedNodes; },
        getDisplayedNodes: function() { return _displayedNodes; },
        options: options
    }
}];

var ViewerController = ['$scope', 'Scopes', 'NodesFactory', function($scope, Scopes, NodesFactory) {
    Scopes.add('viewer', $scope);
    $scope.profondeur = NodesFactory.options.profondeur;

    var toAlchemy = function(resource) {
        var nodes = resource;
        return {
            nodes: nodes,
            edges: []
        }
    };

    var displayNodes = function() {
        console.log("displayNodes");

        var config = {
            dataSource: toAlchemy(NodesFactory.getDisplayedNodes()),
            forceLocked: true,
            graphHeight: function(){
                return document.getElementById("alchemy").offsetHeight;
            },
            graphWidth: function(){ return document.getElementById("alchemy").offsetWidth; },
            linkDistance: function(){ return 40; },

            nodeTypes: {"type":["instanciable"]},
            nodeStyle: {
                "all": {
                    radius: 15,
                    color: "rgba(200,200,200)",
                    borderWidth: 0
                },
                "instanciable": {
                    "radius": 18
                }
            },

            caption: "label",
            nodeCaptionsOnByDefault: true
        };

        alchemy = new Alchemy(config)
    };

    $scope.$on('Viewer_displayNodes', function(event) {
        displayNodes();
    });
}];

var EditNodeController = ['$scope', 'Scopes', 'NodesFactory', function($scope, Scopes, NodesFactory) {
    Scopes.add('editNode', $scope);
    $scope.displayedNodes = [];
    $scope.nodeSelected = null;

    $scope.isShowingNode = function() {
        return $scope.nodeSelected != null;
    };

    var init = function() {
        $scope.nodeSelected = null;
        $scope.displayedNodes = NodesFactory.getDisplayedNodes();
        console.log($scope.displayedNodes);
    };

    var updateNode = function() {
        $scope.nodeSelected = NodesFactory.getCurrentNode();
    };

    $scope.$on('EditNode_init', function(event) {
        init();
    });

    $scope.$on('EditNode_updateNode', function(event) {
        updateNode();
    });
}];

var SearchController = [
    '$scope',
    'Scopes',
    'NodesFactory',
    function($scope, Scopes, NodesFactory) {
        Scopes.add('search', $scope);

        $scope.launchSearch = function() {
            NodesFactory.setDisplayedNodes([
                {
                    id:21853,
                    label: "noeud 21853"
                }
            ]);

            Scopes.get('viewer').$emit('Viewer_displayNodes');
            Scopes.get('editNode').$emit('EditNode_init');
        }
    }
];

angular.module('graphEditor', [])
    .factory('Scopes', Scopes)
    .factory('NodesFactory', NodesFactory)
    .controller('ViewerController', ViewerController)
    .controller('EditNodeController', EditNodeController)
    .controller('SearchController', SearchController);