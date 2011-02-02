<ul id="criteria">
	<g:each in="${criteria}" var="criterion">
		<li>
			<span class="entityfield">${criterion.entityField()}</span>
			<span class="operator">${criterion.operator}</span>
			<span class="value">${criterion.value}</span>
		</li>
	</g:each>
</ul>
