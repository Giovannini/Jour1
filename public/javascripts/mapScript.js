/**
 * Created by vlynn on 26/01/15.
 */
const TAG = "GAMEMAP_";

/*
 * Factory that provides the logic to communicate with the server
 * has methods for instances and concepts
 * and a general method if it's needed elsewhere 
 */
var RestFactory = function() {
    var baseUrl = "/";
    var getUrl = function(url) {
        return function (success, error) {
            // Init the AJAX object
            var req = new XMLHttpRequest();
            req.open("GET", url, true);
            req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
            req.onreadystatechange = function () {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        // if succeeded
                        success(req.responseText);
                    } else {
                        // if error
                        error(req.status, req.responseText);
                    }
                }
            };
            // Launch request
            req.send(null);
        };
    };
    
    var postUrl = function(url, data) {
        return function(success, error) {
            var req = new XMLHttpRequest();
            req.open("POST", url, true);
            req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
            req.onreadystatechange = function() {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        // if succeeded
                        success(req.responseText);
                    } else {
                        // if error
                        error(req.status, req.responseText);
                    }
                }
            };
            // Launch request
            req.send(JSON.stringify(data));
        }
    };

    return {
        concepts: {
            get: getUrl(baseUrl+"concepts"),
            getById: function(idConcept) { return getUrl(baseUrl+"concepts/"+idConcept); }
        },
        instances: {
            get: getUrl(baseUrl+"instances"),
            getById: function(idInstance) { return getUrl(baseUrl+"instances/"+idInstance); },
            getByConcept: function(idInstance, action, conceptId) { return getUrl(baseUrl+"instances/"+idInstance+"/"+action+"/"+conceptId); },
            deleteInstance: function(idInstance) { return postUrl(baseUrl+"instances/delete/"+idInstance, {}); }
        },
        action: {
            sendAction: function(initInstance, action, destInstance) {
                return postUrl(baseUrl+"map/action", {
                    action: action,
                    instances: [initInstance, destInstance]
                });
            }
        },
        all: {
            get: getUrl
        }
    };
};

/*
 * Factory for graph related items
 * has concepts and relations and functions to manipulate the arrays containing them
 */
var GraphFactory = function(Rest) {
    var concepts = {};

    // Object mapping the relation between concepts
    var Relation = function(relation) {
        var _this = this;

        // Object constructor
        (function() {
            _this.id = relation.relationID;
            _this.label = relation.relationLabel;
            _this.relatedConcept = relation.conceptId;
        })();
    };

    // Object mapping a concept
    var Concept = function(concept, relations) {
        var _this = this;

        // Object constructor
        (function() {
            _this.id = concept.id;
            _this.label = concept.label;
            
            if(typeof concept.properties !== "undefined")
                _this.properties = concept.properties;
            
            if(typeof concept.rules !== "undefined") {
                _this.defineRules(concept.rules);
            }

            if(typeof concept.display.color === "undefined") {
                _this.color = "#aaaaaa";
            } else {
                _this.color = concept.display.color;
            }

            if(typeof concept.display.zindex === "undefined") {
                _this.zindex = "1";
            } else {
                _this.zindex = concept.display.zindex;
            }

            _this.relations = null;
            if(typeof relations !== "undefined")
                _this.addRelation(relations);
        })();
    };
    
    Concept.prototype.defineRules = function(rules) {
        this.rules = {};
        for(var id in rules) {
            this.rules[rules[id].property.label] = rules[id].value;
        }
    };

    // add a single relation or an array of relations to the concept
    Concept.prototype.addRelation = function(relation) {
        if(!this.relations)
            this.relations = [];
        if(relation instanceof Array) {
            for(var id in relation) {
                this.addRelation(relation[id]);
            }
        } else if(relation !== "undefined") {
            if(typeof relation !== "Relation")
                relation = new Relation(relation);
    
            this.relations.push(relation);
        }
    };
    
    // Get the relation of the concept (from the server if needed)
    // the callback is a function with the relations as an argument
    // return true if it needs to fetch the relations from the server and false if not
    Concept.prototype.getRelations = function(callback) {
        if(this.relations) {
            // The relations are already there
            callback(this.relations);
            return false;
        } else {
            // We need to fetch the relations
            var _this = this;
            Rest.all.get("/map/relations/" + this.id)(
                function (response) {
                    var relations = JSON.parse(response);
                    _this.addRelation(relations);
                    callback(_this.relations);
                },
                function (status, response) {
                    console.log("ERROR"+status+": "+response);
                }
            );
            return true;
        }
    };

    // Add a concept (or an array of concepts) to the array of concepts
    var addConcept = function(concept) {
        if(typeof concept === "undefined")
            return;

        if(concept instanceof Array) {
            for(var id in concept) {
                addConcept(concept[id]);
            }
        } else {
            if (!(concept instanceof Concept))
                concept = new Concept(concept);

            concepts[concept.id] = concept;
        }
    };

    // Initialize the concepts from the server
    var initConcepts = function() {
        Rest.concepts.get(
            function(resConcepts) {
                addConcept(JSON.parse(resConcepts));
                document.dispatchEvent(new Event(TAG+"initialized"));
            },
            function(status, responseText) {
                console.log(status, responseText);
            }
        );
    };
    
    // Return concepts from the array of concept
    // if the idArray is defined, it only returns the concerned concepts
    // if not, it returns the whole array
    var getConcepts = function(idArray) {
        if(typeof idArray === "undefined") {
            return concepts;
        } else {
            var res = {};
            for(var id in idArray) {
                res[idArray[id]] = concepts[idArray[id]];
            }
            return res;
        }
    };

    // The public methods of the factory
    return {
        getConcepts: getConcepts,
        addConcept: addConcept,
        initConcepts: initConcepts
    }
};

/*
 * Factory for map related items
 * has the Instance object and stores the array of instances
 */
var MapFactory = function(Rest) {
    var width = 0,
        height = 0,
        instances = [];

    var Instance = function(id, label, x, y, conceptId, properties) {
        var _this = this;

        (function() {
            _this.id = id;
            _this.label = label;
            _this.coordinates = {x: x, y: y};
            _this.conceptId = conceptId;
            _this.properties = properties;
        })();
    };

    var addInstance = function(instance) {
        if(typeof instance === "undefined") {
            return;

        }
        if(instance instanceof Array) {
            for (var id in instance) {
                addInstance(instance[id]);
            }
        } else {
            if (!(instance instanceof Instance)) {
                instance = new Instance(
                    instance.id,
                    instance.label,
                    instance.coordinates.x,
                    instance.coordinates.y,
                    instance.concept,
                    instance.properties
                )
            }
            if(instance.label === "XXX") {
                console.log(instance);
            }
            instances[instance.id] = instance;
        }
    };

    var initInstances = function() {
        Rest.instances.get(function(map) {
            instances = [];
            map = JSON.parse(map);
            width = map.width;
            height = map.height;
            addInstance(map.instances);
            document.dispatchEvent(new Event(TAG+"initialized"));
        });
    };

    var getInstance = function(idInstance, callback) {
        Rest.instances.getById(idInstance)(
            function(responseText) {
                callback(JSON.parse(responseText));
            },
            function(status, responseText) {
                console.log(status, responseText);
            }
        );
    };
    
    var getInstances = function(idArray) {
        if(typeof idArray === "undefined") {
            return instances;
        } else {
            var res = [];
            for(var id in idArray) {
                res.push(instances[idArray[id]]);
            }
            return res;
        }
    };
    
    var getInstancesByConcept = function(idInstance, action, idConcept, callback) {
        Rest.instances.getByConcept(idInstance, action, idConcept)(
            function(responseText) {
                callback(JSON.parse(responseText));
            },
            function(status, responseText) {
                console.log(status, responseText);
            }
        );
        
    };

    return {
        getInstance: getInstance,
        getInstances: getInstances,
        getInstancesByConcept: getInstancesByConcept,
        initInstances: initInstances,
        addInstance: addInstance,
        getWidth: function() { return width; },
        getHeight: function() { return height; }
    };
};

/*
 * Factory for the drawer
 * Abstracts the layer of pixijs to adapt it to the map problem 
 */
var DrawerFactory = function() {
    var renderer = null,
        stage = null,
        tiledMap = null,
        conceptContainers = {},
        map = [],
        tileWidth = 20,
        tileHeight = 20;
    
    ConceptContainer.prototype = new PIXI.DisplayObjectContainer();
    ConceptContainer.prototype.constructor = ConceptContainer;
    
    function ConceptContainer(concept, _tileWidth, _tileHeight) {
        var _this = this;
        (function() {
            PIXI.DisplayObjectContainer.call(_this);
            
            // Set the information needed to create a new sprite
            _this.concept = concept;
            _this.texture = canvasTexture(tileWidth, tileHeight, concept.color);
            _this.tileWidth = (typeof _tileWidth === "undefined") ? tileWidth : _tileWidth;
            _this.tileHeight = (typeof _tileHeight === "undefined") ? tileHeight : _tileHeight;
        })();
    }
    
    ConceptContainer.prototype.addInstance = function(instance) {
        // Check that the instance is part of the concept
        if(instance.conceptId !== this.concept.id)
            return false;
        
        // Create a sprite for the instance
        var sprite = new PIXI.Sprite(this.texture);

        // move the sprite to its position
        sprite.position.x = instance.coordinates.x * this.tileWidth;
        sprite.position.y = instance.coordinates.y * this.tileHeight;

        // add the sprite to the container
        this.addChild(sprite);
    };
    
    TiledMap.prototype = new PIXI.DisplayObjectContainer();
    TiledMap.prototype.constructor = TiledMap;

    /**
     * Change the place where we want the overlay
     * To display it, we need to refresh the view with overlayRefresh
     * @param coord new place of the overlay
     */
    TiledMap.prototype.overlayPosition = function(coord) {
        this.overlaySprite.position.x = coord.x * this.tileWidth;
        this.overlaySprite.position.y = coord.y * this.tileHeight;
    };

    /**
     * Destroy the old overlay on the map and create a new overlay at the position that we know
     * This position can be changed with overlayPosition
     */
    TiledMap.prototype.overlayRefresh = function() {
        this.removeChild(this.overlaySprite);
        this.addChild(this.overlaySprite);
    };

    /**
     * Destroy the overlay on the map
     */
    TiledMap.prototype.overlayStop = function() {
        this.removeChild(this.overlaySprite);
    };

    function TiledMap(tileWidth, tileHeight, nbTileX, nbTileY, width, height) {
        var _this = this;
        (function() {
            PIXI.DisplayObjectContainer.call(_this);
            
            // Define properties
            _this.tileWidth = tileWidth;
            _this.tileHeight = tileHeight;
            _this.nbTileX = nbTileX;
            _this.nbTileY = nbTileY;
            _this.maxWidth = width;
            _this.maxHeight = height;
            
            // Actual display size of the object
            _this.width = width;
            _this.height = height;

            // Drag & drop vars
            _this.dragging = false;
            _this.initPosition = null;
            
            // Overlay vars
            _this.overlaySprite = new PIXI.Sprite(canvasTexture(tileWidth, tileHeight, "#ffffff", 0.3));
            _this.overlaySprite.alpha = 0.3;
            
            // Define interactivity
            initInteractive();
        })();
        
        function initInteractive() {
            _this.interactive = true;

            _this.click = selectTile;

            if (_this.tileWidth * _this.nbTileX > _this.width ||
                    _this.tileHeight * _this.nbTileY > _this.height) {
                _this.mousedown = dragStart;
                _this.mouseup = _this.mouseupoutside = dragStop;
                _this.mousemove = function(data) {
                    dragMove(data);
                    overlayMove(data);
                }
            } else {
                _this.mousemove = overlayMove;
            }
            _this.mouseout = overlayStop;
        }

        function setInitPosition(x, y) {
            _this.initPosition = {
                x: x,
                y: y
            };
        }
        
        function move(transition) {
            var newPositionX = _this.position.x + transition.x;
            var newPositionY = _this.position.y + transition.y;
            
            if((newPositionX > 0 || newPositionX + _this.nbTileX * _this.tileWidth < _this.maxWidth)) {
                newPositionX = _this.position.x;
            }
            if((newPositionY > 0 || newPositionY + _this.nbTileY * _this.tileHeight < _this.maxHeight)) {
                newPositionY = _this.position.y;
            }

            _this.position.set(
                    newPositionX,
                    newPositionY
            );
        }

        function selectTile(data) {
            var coord = data.global.clone();
            coord.x = parseInt((coord.x - _this.position.x) / _this.tileWidth);
            coord.y = parseInt((coord.y - _this.position.y) / _this.tileHeight);
            document.dispatchEvent(new CustomEvent(TAG+"selectTile", {'detail': {
                x: coord.x,
                y: coord.y,
                instances: map[coord.x][coord.y]
            }}));
        }

        function dragStart(data) {
            _this.dragging = true;
            setInitPosition(data.global.x, data.global.y);
        }

        function dragMove(data) {
            if(_this.dragging) {
                var transition = {
                    x: data.global.x - _this.initPosition.x,
                    y: data.global.y - _this.initPosition.y
                };
                move(transition);
                setInitPosition(data.global.x, data.global.y);
            }
        }

        function dragStop(data) {
            _this.dragging = false;
            
            // Set all the instances not in the frame to invisibile
//            var i, j, id, visibility;
//            for(i = 0; i < map.length; i++) {
//                for(j = 0; j < map[0].length; j++) {
//                    visibility = isVisible(i, j)
//                    for(id in map[i][j]) {
//                        _this.removeChild(map[i][j][id]);
//                        map[i][j][id].visible = visibility;
//                        _this.addChild(map[i][j][id]);
//                    }
//                }
//            }
        }
            
        function isVisible(tileX, tileY) {
            return tileX * _this.tileWidth + _this.position.x < 0 ||
                    tileX * _this.tileWidth + _this.position.x > _this.maxWidth ||
                    tileY * _this.tileHeight + _this.position.y < 0 ||
                    tileY * _this.tileHeight + _this.position.y > _this.maxHeight
        }

        /**
         * Move the overlay when user's mouse is over the map
         * @param data instance which needs the overlay
         */
        function overlayMove (data) {
            var coord = data.global.clone();
            coord.x = parseInt((coord.x - _this.position.x) / _this.tileWidth);
            coord.y = parseInt((coord.y - _this.position.y) / _this.tileHeight);
            _this.overlayPosition(coord);
            _this.overlayRefresh();
        }
    }

    // Init the drawer
    var initDrawer = function(width, height, backgroundColor) {
        if(typeof backgroundColor === "undefined")
            backgroundColor = 0xcccccc;

        // Initialize the map
        map = [];
        for(var i = 0; i < width; i++) {
            map[i] = [];
            for(var j = 0; j < height; j++) {
                map[i][j] = [];
            }
        }
        
        // Store the number of tiles needed
        var nbTileX = width;
        var nbTileY = height;

        // Actual size of the drawing window
        width = Math.min(document.getElementById('pixi').offsetWidth, tileWidth * width);
        height = Math.min(window.innerHeight, tileHeight * height);
        
        // Init main objects of pixi
        renderer = new PIXI.autoDetectRenderer(width, height);
        stage = new PIXI.Stage(backgroundColor, true);
        // The tiledMap allows to show different tiles and drag&drop
        tiledMap = new TiledMap(tileWidth, tileHeight, nbTileX, nbTileY, width, height);
        stage.addChild(tiledMap);

        // Add the drawing to the page
        var element = document.getElementById("pixi");
        // Remove all the content of the pixi div
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
        // Append the view
        element.appendChild(renderer.view);
    };

    // init render cycle
    var render = function() {
        // Render the frame
        renderer.render(stage);

        requestAnimationFrame(render);
    };
    
    // Create a pixi texture for a tile given a color
    var canvasTexture = function(width, height, color) {
        var canvas, context;

        // Init a new canvas
        canvas = document.createElement('canvas');
        context = canvas.getContext('2d');
        
        canvas.width  = width;
        canvas.height = height;

        // Draw the pixel
        context.fillStyle = color;
        context.fillRect(0,0,width,height);
        
        return PIXI.Texture.fromCanvas(canvas);
    };

    // Create a container for each concept
    var createConceptContainers = function(concepts) {
        var id;
        conceptContainers = {};

        var sortedConcepts = [];
        for(id in concepts) {
            if(typeof concepts[id].zindex !== "undefined")
                sortedConcepts.push({id: concepts[id].id, zindex: concepts[id].zindex});
        }
        sortedConcepts = sortedConcepts.sort(function(conceptA, conceptB) {
            if (conceptA.zindex < conceptB.zindex)
                return -1;
            if (conceptA.zindex > conceptB.zindex)
                return 1;
            return 0;
        });

        for(id in sortedConcepts) {
            // create a new ConceptContainer which will contain all the instances of this concept
            var conceptContainer = new ConceptContainer(concepts[sortedConcepts[id].id]);
            conceptContainers[sortedConcepts[id].id] = conceptContainer;
            tiledMap.addChild(conceptContainer);
        }
        
        return conceptContainers;
    };

    // Draw multiple instances
    var drawInstances = function(instances) {
        for(var id in instances) {
            drawInstance(instances[id]);
        }
    };

    // Draw an instance
    var drawInstance = function(instance) {
        if(typeof conceptContainers[instance.conceptId] !== "undefined") {
            // add the instance to the container
            conceptContainers[instance.conceptId].addInstance(instance);

            // add it to the map
            map[instance.coordinates.x][instance.coordinates.y].push(instance.id);
        } else {
            console.log("raté")
        }
    };

    var overlayMove = function(coord) {
        tiledMap.overlayPosition(coord);
        tiledMap.overlayRefresh();
    };

    var overlayStop = function() {
        tiledMap.overlayStop();
    };

    return {
        initDrawer: initDrawer,
        render: render,
        createConceptContainers: createConceptContainers,
        drawInstances: drawInstances,
        drawInstance: drawInstance,
        overlayMove: overlayMove,
        overlayStop: overlayStop
    }
};

var MapController = function(Graph, Map, Drawer) {
    Graph.initConcepts();
    Map.initInstances();
    var concepts, instances, width, height;

    function initMap() {
        // init main objects
        Drawer.initDrawer(width, height);

        // Create the containers for each concept
        Drawer.createConceptContainers(concepts);

        // Add each instances
        Drawer.drawInstances(instances);

        // Render the map
        Drawer.render();
    }

    // Wait for concepts and instances to be initialized
    document.addEventListener(TAG+"initialized", function() {
        concepts = Graph.getConcepts();
        instances = Map.getInstances();
        width = Map.getWidth();
        height = Map.getHeight();

        if(Object.keys(concepts).length > 0 && instances.length > 0) {
            initMap();
        }
    });

    document.addEventListener(TAG+"selectTile", function(event) {
        var selectedInstances = event.detail.instances;
        selectedInstances.map(function(id) {
            return instances[id];
        });
    });

    /**
     * Create an overlay on an instance
     * @param instanceId id of the instance
     * @param highlight true to create the overlay, false to destroy it
     */
    var highlightInstance = function(instanceId, highlight) {
        var instance = instances[instanceId];
        if (highlight) {
            Drawer.overlayMove(instance.coordinates);
        }
        else {
            Drawer.overlayStop();
        }
    };

    return {
        highlightInstance: highlightInstance
    }
};

// Init the objects needed in the controller
window.Rest = RestFactory();
window.Graph = GraphFactory(Rest);
window.Map = MapFactory(Rest);
window.Drawer = DrawerFactory();

window.MapController = MapController(Graph, Map, Drawer);