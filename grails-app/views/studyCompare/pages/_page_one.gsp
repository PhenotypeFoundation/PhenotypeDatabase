<%
/**
 * first wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20120123
 *
 * Revision information:
 * $Rev:  67319 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:45:42 +0100 (Wed, 22 Dec 2010) $
 *

 <meta http-equiv="Content-type" content="text/html; charset=utf-8">
 <title>Select test</title>
 <script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
 <script type="text/javascript" charset="utf-8">
 $(function(){
 $("select#ctlJob").change(function(){
 $.getJSON("select.php",{id: $(this).val()}, function(j){
 var options = '';
 for (var i = 0; i < j.length; i++) {
 options += '<option value="' + j[i].optionValue + '">' + j[i].optionDisplay + '</option>';
 }
 $("#ctlPerson").html(options);
 $('#ctlPerson option:first').attr('selected', 'selected');
 })
 })			
 })
 </script>
 </head>
 <body>
 <select id="ctlJob">
 <option value="1">Manager</option>
 <option value="2">Lead Dev</option>
 <option value="3">Developer</option>
 </select>
 <select id="ctlPerson">
 <option value="1">Mark</option>
 <option value="2">Andy</option>
 <option value="3">Richard</option>
 </select>
 </body> 
 
 */
%>
<af:page>
<script type="text/javascript">
// get unique species
$.getJSON("${createLink(controller:'ajax', action:'uniqueSpecies')}",{},function(j) {
	var options = '';
	for (var i=0;i<j.length;i++) {
		options += '<option value="'+j[i].id+'">'+j[i].name+'</option>';
	}
	$("#species").html(options);
	$("#species option:first").attr('selected', 'selected');
});
</script>

species: <select id="species"/><br/>



</af:page>
