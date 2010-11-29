<g:if test="${studyList*.events?.flatten()?.size()==0 && studyInstance*.samplingEvents?.flatten()?.size()==0 }">
  No events in these studies
</g:if>
<g:else>
  <g:each in="${studyList}" var="study" status="i">
	<div style="margin: 10px; ">
	  <div class="eventtitles" id="eventtitles-${i}"></div>
	  <div class="eventstimeline" id="eventstimeline-${i}"></div>
	</div>
  </g:each>
  <noscript>
	Javascript is required to show the timeline. Please use the events table instead or enable javascript in your browser.
  </noscript>
</g:else>