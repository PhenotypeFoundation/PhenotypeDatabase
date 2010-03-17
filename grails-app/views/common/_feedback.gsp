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
	<img id="add_feedback" src="${resource(dir: 'images', file: 'icons/famfamfam/comment_add.png')}" style="cursor: pointer;" alt="submit feedback for this page" />
	<div class="feedback" id="feedback">
	  <form>
		<g:hiddenField name="feedbackUrl" />
		<g:hiddenField name="feedbackWindowWidth" />
		<g:hiddenField name="feedbackWindowHeight" />
		<g:hiddenField name="feedbackUserAgent" />
		<div class="element">
			<div class="name">Your name</div>
			<div class="input"><g:textField name="feedbackName" /></div>
		</div>
		<div class="element">
			<div class="name">Remark</div>
			<div class="input"><g:textArea name="feedbackBody" rows="5" cols="40">this does not work yet so don't use it :)</g:textArea></div>
		</div>
		<div class="element">
			<div class="name"></div>
			<div class="input"><g:submitToRemote url="[controller:'feedback',action:'add']" update="[success:'feedback']" value="submit feedback" /></div>
		</div>
	  </form>
	</div>