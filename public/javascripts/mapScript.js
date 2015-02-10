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
    var getUrl = function(url) {
        return function (success, error) {
            // Init the AJAX object
            var req = new XMLHttpRequest();
            req.open("GET", url, true);
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

    return {
        concepts: {
            get: getUrl("/concepts")
        },
        instances: {
            get: getUrl("/instances")
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
    var concepts = [];

    // Object mapping the relation between concepts
    var Relation = function(relation) {
        var _this = this;

        // Object constructor
        (function() {
            _this.label = relation.relation;
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

            if(typeof concept.color === "undefined") {
                _this.color = "#aaaaaa";
            } else {
                _this.color = concept.color;
            }

            _this.relations = [];
            if(typeof relations !== "undefined")
                _this.addRelation(relations);
        })();
    };

    // add a single relation or an array of relations to the concept
    Concept.prototype.addRelation = function(relation) {
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
        if(this.relations.length > 0) {
            // The relations are already there
            callback(this.relations);
            return false;
        } else {
            // We need to fetch the relations
            var _this = this;
            Rest.all.get("/relations/" + this.id)(
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
                console.log("status"+status);
                console.log(responseText);
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
            var res = [];
            for(var id in idArray) {
                res.push(concepts[idArray[id]]);
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
        if(typeof instance === "undefined")
            return;

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

    return {
        getInstances: getInstances,
        initInstances: initInstances,
        addInstance: addInstance,
        getWidth: function() { return width; },
        getHeight: function() { return height; }
    };
};

var DrawerFactory = function() {
    var renderer = null,
        stage = null,
        textures = [],
        conceptContainers = [],
        sprites = [],
        map = [],
        tileWidth,
        tileHeight;

    // Init the drawer
    var initDrawer = function(width, height, backgroundColor) {
        if(typeof backgroundColor === "undefined")
            backgroundColor = 0xcccccc;

        for(var i = 0; i < width; i++) {
            map[i] = [];
            for(var j = 0; j < height; j++) {
                map[i][j] = [];
            }
        }
        
//        tileWidth = Math.max(parseInt(document.getElementById('pixi').offsetWidth / width));
//        tileHeight = Math.max(1, parseInt(window.innerHeight / height));

        tileWidth = 8;
        tileHeight = 8;
        
        renderer = new PIXI.autoDetectRenderer(width * tileWidth, height * tileHeight);
        stage = new PIXI.Stage(backgroundColor, true);

        // Add the drawing to the page
        var element = document.getElementById("pixi");
        // Remove all the content of pixi
        while (element.firstChild) {
            element.removeChild(element.firstChild);
        }
        // Append the view
        element.appendChild(renderer.view);
        
        // Set the overlay
        initOverlay(stage);
        
        // Listen to click events
        listenClicks(stage);
    };
    
    var initOverlay = function(stage) {
        var overlaySprite = new PIXI.Sprite(canvasTexture(tileWidth, tileHeight, "#ffffff", 0.3));
        overlaySprite.alpha = 0.3;

        stage.mousemove = function(data) {
            var coord = data.global.clone();
            coord.x = parseInt(coord.x / tileWidth);
            coord.y = parseInt(coord.y / tileHeight);
            overlaySprite.position.x = coord.x * tileWidth;
            overlaySprite.position.y = coord.y * tileHeight;
            stage.removeChild(overlaySprite);
            stage.addChild(overlaySprite);
        };
        
        stage.mouseout = function() {
            stage.removeChild(overlaySprite);
        }
    };
    
    var listenClicks = function(stage) {
        stage.click = function(data) {
            var coord = data.global.clone();
            coord.x = parseInt(coord.x / tileWidth);
            coord.y = parseInt(coord.y / tileHeight);
            document.dispatchEvent(new CustomEvent(TAG+"selectTile", {'detail': {
                x: coord.x,
                y: coord.y,
                instances: map[coord.x][coord.y]
            }}));
        };
    };

    // init render cycle
    var render = function() {
        // Render the frame
        renderer.render(stage);
        
        requestAnimationFrame(render);
    };
    
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

    // Create a base texture for each concepts
    var createPixelTextures = function(concepts) {
        textures = [];
        var id;
        for(id in concepts) {
            // Create a new texture from the canvas
            textures[id] = canvasTexture(tileWidth, tileHeight, concepts[id].color);
        }
        return textures;
    };
    
    var createSpriteBatches = function(concepts) {
        conceptContainers = [];
        var id;
        for(id in concepts) {
            // create a new sprite batch which will contain all the instances of this concept
            var spriteBatch = new PIXI.SpriteBatch();
            conceptContainers[id] = spriteBatch
            stage.addChild(spriteBatch);
        }
        
        return conceptContainers;
    };

    // Draw multiple instances
    var drawInstances = function(instances) {
        for(var id in instances) {
            sprites.push(drawInstance(stage, textures, instances[id]));
        }
        console.log(sprites.length);
        return sprites;
    };

    // Draw an instance
    var drawInstance = function(stage, textures, instance) {
        var sprite = new PIXI.Sprite(textures[instance.conceptId]);

        // move the sprite to its position
        sprite.position.x = instance.coordinates.x * tileWidth;
        sprite.position.y = instance.coordinates.y * tileHeight;

        // add it to the stage
        if(typeof conceptContainers[instance.conceptId] !== "undefined") {
            conceptContainers[instance.conceptId].addChild(sprite);
        } else {
            stage.addChild(sprite);
        }

        // add it to the map
        map[instance.coordinates.x][instance.coordinates.y].push(instance.id);

        return sprite;
    };

    return {
        initDrawer: initDrawer,
        render: render,
        createTextures: createPixelTextures,
        createSpriteBatches: createSpriteBatches,
        drawInstances: drawInstances,
        drawInstance: drawInstance
    }
};

var MapController = function(Graph, Map, Drawer) {
    Graph.initConcepts();
    Map.initInstances();
    var concepts, instances, width, height;

    function initMap() {
        // init main objects
        Drawer.initDrawer(width, height);

        // Create textures
        Drawer.createTextures(concepts);
        
        // Create the batches
        Drawer.createSpriteBatches(concepts);

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

        if(concepts.length > 0 && instances.length > 0)
            initMap();
    });

    document.addEventListener(TAG+"selectTile", function(event) {
        var selectedInstances = event.detail.instances;
        selectedInstances.map(function(id) {
            return instances[id];
        });
        console.log(selectedInstances);
    })
};

// Init the objects needed in the controller
window.Rest = RestFactory();
window.Graph = GraphFactory(Rest);
window.Map = MapFactory(Rest);
window.Drawer = DrawerFactory(10, 10);

MapController(Graph, Map, Drawer);