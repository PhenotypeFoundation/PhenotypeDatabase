



<!--- This is a table body. It contains exactly one Protocol Parameters per row. -->
<% tableRowId=0 %>
<g:each in ="${list}" >

    <g:if test="it.type==dbnp.studycapturing.ProtocolParameterType.STRINGLIST" >
        <% def listEntries=it.listEntries.collect { it } %>

        <script type="text/javascript">
	    var tmpList = [];
            <% listEntries.each{ print "tmpList.push( escape('${it}') ); tmpList.push( '${it.id}' );" } %>
            addRow('showProtocolParameters',"${list.id[0]}","${it.name}","${it.unit}","${it.type}","${it.reference}","${it.description}", tmpList );
	    delete tmpList;
        </script>
    </g:if>
    <g:else>
        <script type="text/javascript">
            addRow('showProtocolParameters',"${list.id[0]}","${it.name}","${it.unit}","${it.type}","${it.reference}","${it.description}", new Array() );
        </script>
    </g:else>
</g:each>