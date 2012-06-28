<af:page>
	<h1>Select study</h1>
	<p>
		<h2>This wizard builds a dataset using data from GSCF and assay modules. </br>
        Please select a study:</h2>
		<g:select name="selectStudy" from="${studies}" optionKey="id" value="title" onChange="getAssaylist();"/>
		
		<script type="text/javascript">
			function getAssaylist(){
				$.ajax({
					url: 'getAssays',
					data: {selectStudy: $("#selectStudy").val()},
					dataType: 'JSON',
					async: false,
					success: function(data) {
						$("#assayList").html("<h2>Select assays:</h2>");
						$.each(data.assays, function () {
							$("#assayList").append("<input type='checkbox' name='assay_"+this.id+"' CHECKED /> "+this.name+" ("+this.modulename+")<br />");
						});
					}
				});
			}
		</script>
		<p id="assayList">
		
		</p>
	<p>
</af:page>
