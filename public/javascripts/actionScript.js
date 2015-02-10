/**
 * Gestion du DOM pour l'affichage des actions
 * Created by vlynn on 27/01/15.
 */
var ActionController = ['$scope', function($scope) {
    $scope.isTileSelected = false;
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
        var actions = [];
        for(var id in relations) {
            actions.push({
                label: relations[id].label + "(" + Graph.getConcepts([relations[id].relatedConcept])[0].label + ")"
            });
        }
        $scope.actions[$scope.selectedInstance] = actions;
        $scope.loadingActions = -1;
        if(needToApply)
            $scope.$apply();
    }
    
    // Select an instance and get its relations
    $scope.selectInstance = function(id) {
        $scope.loadingActions = id;
        $scope.selectedInstance = id;
        var concept = Graph.getConcepts([$scope.instances[id].conceptId])[0];
        needToApply = false;
        needToApply = concept.getRelations(applyActions);
    }
}];

angular.module('actionManagerApp', [])
    .controller('ActionController', ActionController);