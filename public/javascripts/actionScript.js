var ActionController = ['$scope', function($scope) {
    $scope.isTileSelected = false;
    $scope.isInstanceSelected = false;
    var needToApply;
    
    // Selected tile
    $scope.tile = {
        x: -1,
        y: -1
    };

    // Event listener that says a tile has been clicked
    document.addEventListener(TAG+'selectTile', function(event) {
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
        $scope.isInstanceSelected = false;
        $scope.$apply();
    }
    
    // Show the actions of the selected instance 
    function applyActions(relations) {
        $scope.actions = [];
        for(var id in relations) {
            $scope.actions.push({
                label: relations[id].label + "(" + Graph.getConcepts([relations[id].relatedConcept])[0].label + ")"
            });
        }
        $scope.isInstanceSelected = true;
        if(needToApply)
            $scope.$apply();
    }
    
    // Select an instance and get its relations
    $scope.selectInstance = function(id) {
        var concept = Graph.getConcepts([$scope.instances[id].conceptId])[0];
        needToApply = false;
        needToApply = concept.getRelations(applyActions);
    }
}];

angular.module('actionManagerApp', [])
    .controller('ActionController', ActionController);