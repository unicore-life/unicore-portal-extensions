var MedView = function() {
	var gui = null;
	var guiLayerControls = {};
	var visParams = {
		fov: 20,
		len: 1,
		opacity: 1,
		time_shift: 0,
		auto_play: false,
		sync_auto_play: true
	};
	var angio = [];
	var container;
	var camera, scene, renderer;
	var width, height;
	var views = {};
	var time_start = 0;
	var deg2rad = Math.PI/180;
	var url_prefix = "";

	this.init = function(view_element, base_url) {	
		time_start = new Date().getTime();

		url_prefix = base_url;

		container = view_element;
		container.style.padding = 0;
		//container.style.overflow = "hidden";
		width = container.offsetWidth;
		height = container.offsetHeight;
		
		camera = new THREE.PerspectiveCamera( 40, width/height, 0.1, 50 );
		camera.position.z = 10;
		camera.target = new THREE.Vector3(0, 0, 0);

		scene = new THREE.Scene();

		{ // X axis 
			var g = new THREE.Geometry();
			addLine(g, 0, 0, 0, 1, 0, 0);
			var mat = new THREE.LineBasicMaterial({color: 0xff0000});
			var m = new THREE.Line(g, mat);
			m.rotation.x = -Math.PI/2;
			scene.add(m);
		}
		{ // Y axis
			var g = new THREE.Geometry();
			addLine(g, 0, 0, 0, 0, 1, 0);
			var mat = new THREE.LineBasicMaterial({color: 0x00ff00});
			var m = new THREE.Line(g, mat);
			m.rotation.x = -Math.PI/2;
			scene.add(m);
		}
		{ // Z axis
			var g = new THREE.Geometry();
			addLine(g, 0, 0, 0, 0, 0, 1);
			var mat = new THREE.LineBasicMaterial({color: 0x0000ff});
			var m = new THREE.Line(g, mat);
			m.rotation.x = -Math.PI/2;
			scene.add(m);
		}

		{
			var g = new THREE.PlaneGeometry(2, 5, 3, 7);
			var mat = new THREE.MeshBasicMaterial({color: 0x0000ff, wireframe: true});
			var m = new THREE.Mesh(g, mat);
			m.rotation.x = -Math.PI/2;
			scene.add(m);
		}
		renderer = new THREE.WebGLRenderer();
		renderer.setPixelRatio( window.devicePixelRatio );
		renderer.setClearColor(0x202020);
		$(container).append(renderer.domElement);	

		controls = new THREE.OrbitControls(camera, container);
		controls.minPolarAngle = 0;
		controls.maxPolarAngle = Math.PI;
		controls.minDistance = 1;
		controls.autoRotate = true;
		controls.autoRotateSpeed = 0.5;
		controls.userPan = false;
		controls.addEventListener('change', render);

		this.onResize();

		gui = new dat.GUI({autoPlace:false});
		$(container).append($(gui.domElement));
		gui.domElement.style.position = "absolute";
		gui.domElement.style.top = "0px";
		gui.domElement.style.right = "0px";

		$.getJSON(url_prefix + "/data.json", loadAngios);
	};

	this.onResize = function() {
		width = container.offsetWidth;
		height = container.offsetHeight;		

		camera.aspect = width/height;
		camera.updateProjectionMatrix();
		renderer.setSize(width, height);
	};

	this.update = function() {
		var time_now = new Date().getTime();

		for (var i in views) {		
			var o = views[i];
			var new_frame;

			if (visParams.auto_play) {
				if (visParams.sync_auto_play) {
					new_frame = Math.floor((time_now - time_start)/o.frame_duration) + o.angio.movement_locmini - 40;
				} else {
					new_frame = Math.floor((time_now - time_start)/o.frame_duration) % o.textures.length;
				}
			} else {
				new_frame = Math.floor(o.angio.movement_locmini + visParams.time_shift);
			}


			if (new_frame < 0) new_frame = 0;
			else if (new_frame > o.textures.length - 1) new_frame = o.textures.length - 1;
			if (new_frame != o.frame) {
				o.frame = new_frame;
				o.view_material.map = o.textures[o.frame];
				if (o.frame < o.angio.movement_roi_start || o.frame > o.angio.movement_roi_end) {
					o.frust.material.color = new THREE.Color(0x404040);
				} else {
					if (o.frame > o.angio.movement_locmini - 2 && o.frame < o.angio.movement_locmini + 2) {
						o.frust.material.color = new THREE.Color(0xffffff);
					} else {
						o.frust.material.color = new THREE.Color(0xa0a0a0);
					}
				}
			}
		}
		
		if (visParams.sync_auto_play && time_now - time_start > 5000) {
			time_start = time_now;
		}
		render();	
	};

	function render() {	
		renderer.render(scene, camera);
	}

	function addLine(g, x1, y1, z1, x2, y2, z2)
	{
		g.vertices.push(
			new THREE.Vector3(x1, y1, z1),
			new THREE.Vector3(x2, y2, z2)
		);
	}

	function loadAngios(data) {
		if (data !== undefined) {
			angio = data;
		}	
		for (var i in angio) {
			setupAngio(angio[i]);
		}
		var guiLayers = gui.addFolder("Angio studies");
		for (var v in guiLayerControls) {
			guiLayers.add(guiLayerControls, v).onChange(updateVisible);
		}
		//guiLayers.open();

		var guiControls = gui.addFolder("Controls");
		guiControls.add(visParams, "fov", 10.0, 100.0).onChange(regenAngioViews);
		guiControls.add(visParams, "len", 1.0, 3.0).onChange(regenAngioViews);
		guiControls.add(visParams, "opacity", 0, 1).onChange(regenOpacity);
		guiControls.add(visParams, "time_shift", -30, 30);
		guiControls.add(visParams, "auto_play");
		guiControls.add(visParams, "sync_auto_play");
		guiControls.open();
		render();	
	}

	function updateVisible()
	{
		for (var v in guiLayerControls) {
			var visible = guiLayerControls[v];
			views[v].plane.visible = visible;
		}
	}

	function setupAngioGeom(mesh1, mesh2, fov, len, angio)
	{
		fov = fov*deg2rad;
		var a = angio;
		var scale = 0.001; // mm to m
		var d = scale*len*a.dist_source_detector;
		var s = scale*a.dist_source_isocenter;
		var w = d*Math.tan(fov/2);
		var z = d - s;
		var g = mesh1.geometry;
		g.vertices = [];
		addLine(g, 0, 0, -s, -w, -w, z);
		addLine(g, 0, 0, -s, w, -w, z);
		addLine(g, 0, 0, -s, w, w, z);
		addLine(g, 0, 0, -s, -w, w, z);
		addLine(g, -w, -w, z, w, -w, z);
		addLine(g, w, -w, z, w, w, z);
		addLine(g, w, w, z, -w, w, z);
		addLine(g, -w, w, z, -w, -w, z);
		g.computeBoundingSphere();
		g.verticesNeedUpdate = true;
		mesh1.geometry = g;

		var g2 = new THREE.PlaneGeometry(2*w, 2*w);
		g2.verticesNeedUpdate = true;
		mesh2.geometry = g2;

		mesh1.matrixAutoUpdate = false;
		var m0 = new THREE.Matrix4();
		var m1 = new THREE.Matrix4();
		var m2 = new THREE.Matrix4();
		m0.makeRotationX(-Math.PI/2.0);
		var a1 = a.angle1*deg2rad;
		var a2 = a.angle2*deg2rad;
		m1.makeRotationZ(-a1);
		m2.makeRotationX(-a2);
		mesh1.matrix.identity();
		mesh1.matrix.multiply(m2);
		mesh1.matrix.multiply(m1);
		mesh1.matrix.multiply(m0);

		mesh2.matrixAutoUpdate = false;
		mesh2.matrix.identity();
		mesh2.matrix.multiply(mesh1.matrix);
		var m3 = new THREE.Matrix4();
		m3.makeTranslation(0, 0, z);	
		mesh2.matrix.multiply(m3);
		m3.makeRotationZ(-Math.PI/2.0);
		//mesh2.matrix.multiply(m3);

	}

	function regenAngioViews()
	{
		for (var i in views) {
			var o = views[i];
			setupAngioGeom(o.frust, o.plane, visParams.fov, visParams.len, o.angio);
		}
	}

	function regenOpacity()
	{
		for (var i in views) {
			var o = views[i];
			o.view_material.opacity = visParams.opacity;
		}
	}

	function setupAngio(a) {
		var g = new THREE.Geometry();
		var g2 = new THREE.Geometry();

		var mat = new THREE.LineBasicMaterial({color: 0xa0a0a0});
		mesh1 = new THREE.Line(g, mat);	

		var mat2 = new THREE.MeshBasicMaterial({color: 0xffffff, transparent: true, opacity: 1, side: THREE.DoubleSide});
		var mesh2 = new THREE.Mesh(g2, mat2);
		setupAngioGeom(mesh1, mesh2, 20, 2, a);
		scene.add(mesh1);
		scene.add(mesh2);

		texs = [];
		for (var i in a.images) {
			var tex = THREE.ImageUtils.loadTexture(url_prefix+"/"+a.images[i]);
			tex.minFilter = THREE.LinearFilter;
			tex.maxFilter = THREE.LinearFilter;
			texs.push(tex);
		}

		var obj = {
			textures: texs,
			view_material: mat2,
			plane: mesh2,
			frust: mesh1,
			frame: 0,
			frame_duration: a.frame_duration,
			angio: a
		};
		mat2.map = texs[0];

		var n = a.filename;
		var ni = n.lastIndexOf("/");
		if (ni >= 0) {
			n = n.slice(ni + 1);
		}
		views[n] = obj;
		guiLayerControls[n] = true;	
	}
};

