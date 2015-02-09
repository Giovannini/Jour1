/**
 * Created by vlynn on 26/01/15.
 */
const TAG = "GAMEMAP_";

var RestFactory = function() {
    var getUrl = function(url) {
        return function (success, error) {
            var concepts = [];

            var req = new XMLHttpRequest();
            req.open("GET", url, true);
            req.onreadystatechange = function (e) {
                if (req.readyState === 4) {
                    if (req.status === 200) {
                        success(req.responseText);
                    } else {
                        error(req.status, req.responseText);
                    }
                }
            };
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

var GraphFactory = function(Rest) {
    var concepts = [];

    var Relation = function(relation) {
        var _this = this;

        (function() {
            _this.label = relation.relation;
            _this.relatedConcept = relation.conceptId;
        })();
    };

    var Concept = function(concept, relations) {
        var _this = this;

        /**
         * Constructeur d'un concept - en javascript
         */
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
    
    Concept.prototype.getRelations = function(callback) {
        if(this.relations.length > 0) {
            callback(this.relations);
            return false;
        } else {
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
            )
            return true;
        }
    };

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

    return {
        getConcepts: getConcepts,
        addConcept: addConcept,
        initConcepts: initConcepts
    }
};

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

var DrawerFactory = function(tileWidth, tileHeight) {
    var renderer = null,
        stage = null,
        textures = [],
        sprites = [],
        map = [];

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

        renderer = new PIXI.autoDetectRenderer(width * tileWidth, height * tileHeight);
        stage = new PIXI.Stage(backgroundColor, true);

        // Add the drawing to the page
        var element = document.getElementById("pixi");
        element.appendChild(renderer.view);
    };

    var render = function() {
        console.log(stage);
        // Render the frame
        renderer.render(stage);
    };

    // Create a base texture for each concepts
    var createPixelTextures = function(concepts) {
        textures = [];
        var id,
            concept,
            canvas,
            context;

        for(id in concepts) {
            concept = concepts[id];

            // Init a new canvas
            canvas = document.createElement('canvas');
            context = canvas.getContext('2d');
            canvas.width  = tileWidth;
            canvas.height = tileHeight;

            // Draw the pixel
            context.fillStyle = concept.color;
            context.fillRect(0,0,tileWidth,tileHeight);

            // Create a new texture from the canvas
            textures[id] = PIXI.Texture.fromCanvas(canvas);
        }
        return textures;
    };

    // Draw multiple instances
    var drawInstances = function(instances) {
        for(var id in instances) {
            sprites.push(drawInstance(stage, textures, instances[id]));
        }
        return sprites;
    };

    // Draw an instance
    var drawInstance = function(stage, textures, instance) {
        var sprite = new PIXI.Sprite(textures[instance.conceptId]);

        // move the sprite to its position
        sprite.position.x = instance.coordinates.x * tileWidth;
        sprite.position.y = instance.coordinates.y * tileHeight;

        sprite.interactive = true;
        sprite.click = function(data) {
            var coord = data.target.position.clone();
            coord.x = parseInt(coord.x / tileWidth);
            coord.y = parseInt(coord.y / tileHeight);
            document.dispatchEvent(new CustomEvent(TAG+"selectTile", {'detail': {
                x: coord.x,
                y: coord.y,
                instances: map[coord.x][coord.y]
            }}));
        };

        // add it to the stage
        stage.addChild(sprite);

        // add it to the map
        map[instance.coordinates.x][instance.coordinates.y].push(instance.id);

        return sprite;
    };

    return {
        initDrawer: initDrawer,
        render: render,
        createTextures: createPixelTextures,
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