<script type="text/javascript">
	$(document).ready(function() {
		// hide feedback div
		$('div.feedback').hide();

		// attach action to icon
		$('#add_feedback').bind('click',function() {
			var f = $('div.feedback');

			f.find("input:[name='feedbackUrl']").val(window.location);
			f.find("input:[name='feedbackWindowWidth']").val(window.innerWidth);
			f.find("input:[name='feedbackWindowHeight']").val(window.innerHeight);
			f.find("input:[name='feedbackUserAgent']").val(navigator.userAgent);
			f.toggle('slow');
		});
	});
</script>
	<img id="add_feedback" src="${resource(dir: 'images', file: 'icons/famfamfam/bug_add.png')}" style="cursor: pointer;" alt="submit feedback for this page" />
	<div class="feedback" id="feedback">
	<iframe src ="https://trac.nbic.nl/gscf/newticket" width="100%" height="300">
	  <p>Your browser does not support iframes. Click <a href="https://trac.nbic.nl/gscf/newticket" target="_new">here</a> to report a bug.</p>
	</iframe>
	</div>