window.pl_edu_icm_unicore_portal_angio_ui_vis_ResultsVisualizer = function() {
	var v = new MedView();
	var resizeTimer;
	
	function animate()
	{
		requestAnimationFrame(animate);
		v.update();
	}
	
	$(window).resize(function () {
		clearTimeout(resizeTimer);
	    resizeTimer = setTimeout(v.onResize(), 100);
	});
	
    // Handle changes from the server-side
    this.onStateChange = function() {
		v.init(this.getElement(), this.getState().outputPath);
		animate();
    };
};