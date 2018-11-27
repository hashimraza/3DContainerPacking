var scene;
var camera;
var renderer;
var controls;
var viewModel;
var itemMaterial;

async function PackContainers(request) {
    return $.ajax({
        url: 'http://localhost:8081/api/containerpacking',
        type: 'POST',
        data: request,
        contentType: 'application/json; charset=utf-8'
    });
};

function InitializeDrawing() {
    var container = $('#drawing-container');

    scene = new THREE.Scene();
    camera = new THREE.PerspectiveCamera(50, window.innerWidth / window.innerHeight, 0.1, 1000);
    camera.lookAt(scene.position);

    // var axisHelper = new THREE.AxisHelper( 5 );
    // scene.add( axisHelper );

    // LIGHT
    var light = new THREE.PointLight(0xffffff);
    light.position.set(0, 150, 100);
    scene.add(light);

    // Get the item stuff ready.
    itemMaterial = new THREE.MeshNormalMaterial({transparent: true, opacity: 0.6});

    renderer = new THREE.WebGLRenderer({antialias: true}); // WebGLRenderer CanvasRenderer
    renderer.setClearColor(0xf0f0f0);
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth / 2, window.innerHeight / 2);
    container.append(renderer.domElement);

    controls = new THREE.OrbitControls(camera, renderer.domElement);
    window.addEventListener('resize', onWindowResize, false);

    animate();
};

function onWindowResize() {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth / 2, window.innerHeight / 2);
}

//
function animate() {
    requestAnimationFrame(animate);
    controls.update();
    render();
}

function render() {
    renderer.render(scene, camera);
}

var ViewModel = function () {
    var self = this;

    self.ItemCounter = 0;
    self.ContainerCounter = 0;

    self.ItemsToRender = ko.observableArray([]);
    self.LastItemRenderedIndex = ko.observable(-1);

    self.ContainerOriginOffset = {
        x: 0,
        y: 0,
        z: 0
    };

    self.AlgorithmsToUse = ko.observableArray([]);
    self.ItemsToPack = ko.observableArray([]);
    self.containers = ko.observableArray([]);

    self.NewItemToPack = ko.mapping.fromJS(new ItemToPack());
    self.NewContainer = ko.mapping.fromJS(new Container());

    self.GenerateItemsToPack = function () {
        self.ItemsToPack([]);
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1000,
            name: 'Item1',
            length: 5,
            width: 4,
            height: 2,
            quantity: 1,
            weight: 5
        }));
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1001,
            name: 'Item2',
            length: 2,
            width: 1,
            height: 1,
            quantity: 3,
            weight: 5
        }));
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1002,
            name: 'Item3',
            length: 9,
            width: 7,
            height: 3,
            quantity: 4,
            weight: 5
        }));
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1003,
            name: 'Item4',
            length: 13,
            width: 6,
            height: 3,
            quantity: 8,
            weight: 5
        }));
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1004,
            name: 'Item5',
            length: 17,
            width: 8,
            height: 6,
            quantity: 1,
            weight: 5
        }));
        self.ItemsToPack.push(ko.mapping.fromJS({
            id: 1005,
            name: 'Item6',
            length: 3,
            width: 3,
            height: 2,
            quantity: 2,
            weight: 5
        }));
    };

    self.GenerateContainers = function () {
        self.containers([]);
        self.containers.push(ko.mapping.fromJS({
            id: 1000,
            name: 'Box1',
            length: 15,
            width: 13,
            height: 9,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1001,
            name: 'Box2',
            length: 23,
            width: 9,
            height: 4,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1002,
            name: 'Box3',
            length: 16,
            width: 16,
            height: 6,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1003,
            name: 'Box4',
            length: 10,
            width: 8,
            height: 5,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1004,
            name: 'Box5',
            length: 40,
            width: 28,
            height: 20,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1005,
            name: 'Box6',
            length: 29,
            width: 19,
            height: 4,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1006,
            name: 'Box7',
            length: 18,
            width: 13,
            height: 1,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1007,
            name: 'Box8',
            length: 6,
            width: 6,
            height: 6,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1008,
            name: 'Box9',
            length: 8,
            width: 5,
            height: 5,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1009,
            name: 'Box10',
            length: 18,
            width: 13,
            height: 8,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1010,
            name: 'Box11',
            length: 17,
            width: 16,
            height: 15,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1011,
            name: 'Box12',
            length: 32,
            width: 10,
            height: 9,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
        self.containers.push(ko.mapping.fromJS({
            id: 1012,
            name: 'Box13',
            length: 60,
            width: 60,
            height: 60,
            weight: 5,
            maxAllowedWeight: 100,
            algorithmPackingResults: []
        }));
    };

    self.AddAlgorithmToUse = function () {
        var algorithmID = $('#algorithm-select option:selected').val();
        var algorithmName = $('#algorithm-select option:selected').text();
        self.AlgorithmsToUse.push({AlgorithmID: algorithmID, AlgorithmName: algorithmName});
    };

    self.RemoveAlgorithmToUse = function (item) {
        self.AlgorithmsToUse.remove(item);
    };

    self.AddNewItemToPack = function () {
        self.NewItemToPack.id(self.ItemCounter++);
        self.ItemsToPack.push(ko.mapping.fromJS(ko.mapping.toJS(self.NewItemToPack)));
        self.NewItemToPack.name('');
        self.NewItemToPack.length('');
        self.NewItemToPack.width('');
        self.NewItemToPack.height('');
        self.NewItemToPack.quantity('');
        self.NewItemToPack.weight('');
    };

    self.RemoveItemToPack = function (item) {
        self.ItemsToPack.remove(item);
    };

    self.AddNewContainer = function () {
        self.NewContainer.id(self.ContainerCounter++);
        self.containers.push(ko.mapping.fromJS(ko.mapping.toJS(self.NewContainer)));
        self.NewContainer.name('');
        self.NewContainer.length('');
        self.NewContainer.width('');
        self.NewContainer.height('');
        self.NewContainer.weight('');
        self.NewContainer.maxAllowedWeight('');
    };

    self.RemoveContainer = function (item) {
        self.containers.remove(item);
    };

    self.PackContainers = function () {
        var algorithmsToUse = [];

        self.AlgorithmsToUse().forEach(algorithm => {
            algorithmsToUse.push(algorithm.AlgorithmID);
        });

        var itemsToPack = [];

        self.ItemsToPack().forEach(item => {
            var itemToPack = {
                id: item.id(),
                dim1: item.length(),
                dim2: item.width(),
                dim3: item.height(),
                quantity: item.quantity(),
                weight: item.weight()
            };

            itemsToPack.push(itemToPack);
        });

        var containers = [];

        // Send a packing request for each container in the list.
        self.containers().forEach(container => {
            var containerToUse = {
                id: container.id(),
                length: container.length(),
                width: container.width(),
                height: container.height(),
                weight: container.weight(),
                maxAllowedWeight: container.maxAllowedWeight()
            };

            containers.push(containerToUse);
        });

        // Build container packing request.
        var request = {
            containers: containers,
            itemsToPack: itemsToPack,
            algorithmTypeIDs: algorithmsToUse
        };

        PackContainers(JSON.stringify(request))
            .then(response => {
                // Tie this response back to the correct containers.
                response.forEach(containerPackingResult => {
                    self.containers().forEach(container => {
                        if (container.id() == containerPackingResult.containerId) {
                            container.algorithmPackingResults(containerPackingResult.algorithmPackingResults);
                        }
                    });
                });
            });
    };

    self.ShowPackingView = function (algorithmPackingResult) {
        var container = this;
        var selectedObject = scene.getObjectByName('container');
        scene.remove(selectedObject);

        for (var i = 0; i < 1000; i++) {
            var selectedObject = scene.getObjectByName('cube' + i);
            scene.remove(selectedObject);
        }

        camera.position.set(container.length(), container.length(), container.length());

        self.ItemsToRender(algorithmPackingResult.packedItems);
        self.LastItemRenderedIndex(-1);

        self.ContainerOriginOffset.x = -1 * container.length() / 2;
        self.ContainerOriginOffset.y = -1 * container.height() / 2;
        self.ContainerOriginOffset.z = -1 * container.width() / 2;

        var geometry = new THREE.BoxGeometry(container.length(), container.height(), container.width());
        var geo = new THREE.EdgesGeometry(geometry); // or WireframeGeometry( geometry )
        var mat = new THREE.LineBasicMaterial({color: 0x000000, linewidth: 2});
        var wireframe = new THREE.LineSegments(geo, mat);
        wireframe.position.set(0, 0, 0);
        wireframe.name = 'container';
        scene.add(wireframe);
    };

    self.AreItemsPacked = function () {
        if (self.LastItemRenderedIndex() > -1) {
            return true;
        }

        return false;
    };

    self.AreAllItemsPacked = function () {
        if (self.ItemsToRender().length === self.LastItemRenderedIndex() + 1) {
            return true;
        }

        return false;
    };

    self.PackItemInRender = function () {
        var itemIndex = self.LastItemRenderedIndex() + 1;

        var itemOriginOffset = {
            x: self.ItemsToRender()[itemIndex].packDimX / 2,
            y: self.ItemsToRender()[itemIndex].packDimY / 2,
            z: self.ItemsToRender()[itemIndex].packDimZ / 2
        };

        var itemGeometry = new THREE.BoxGeometry(self.ItemsToRender()[itemIndex].packDimX, self.ItemsToRender()[itemIndex].packDimY, self.ItemsToRender()[itemIndex].packDimZ);
        var cube = new THREE.Mesh(itemGeometry, itemMaterial);
        cube.position.set(self.ContainerOriginOffset.x + itemOriginOffset.x + self.ItemsToRender()[itemIndex].coordX, self.ContainerOriginOffset.y + itemOriginOffset.y + self.ItemsToRender()[itemIndex].coordY, self.ContainerOriginOffset.z + itemOriginOffset.z + self.ItemsToRender()[itemIndex].coordZ);
        cube.name = 'cube' + itemIndex;
        scene.add(cube);

        self.LastItemRenderedIndex(itemIndex);
    };

    self.UnpackItemInRender = function () {
        var selectedObject = scene.getObjectByName('cube' + self.LastItemRenderedIndex());
        scene.remove(selectedObject);
        self.LastItemRenderedIndex(self.LastItemRenderedIndex() - 1);
    };
};

var ItemToPack = function () {
    this.id = '';
    this.name = '';
    this.length = '';
    this.width = '';
    this.height = '';
    this.quantity = '';
    this.weight = '';
};

var Container = function () {
    this.id = '';
    this.name = '';
    this.length = '';
    this.width = '';
    this.height = '';
    this.weight = '';
    this.maxAllowedWeight = '';
    this.algorithmPackingResults = [];
};

$(document).ready(() => {
    $('[data-toggle="tooltip"]').tooltip();
    InitializeDrawing();

    viewModel = new ViewModel();
    ko.applyBindings(viewModel);
});