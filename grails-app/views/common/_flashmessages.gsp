		<g:if test="${flash.error}">
			<div class="message errormessage">
				${flash.error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${flash.message}">
			<div class="message info">
				${flash.message.toString().encodeAsHTML()}
			</div>
		</g:if>	
