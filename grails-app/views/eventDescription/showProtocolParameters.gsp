



<!--- This is a table body. It contains exactly one Protocol Parameters per row. -->
<% tableRowId=0 %>
<g:each in ="${list}" >
    <script type="text/javascript">
    addRow('showProtocolParameters',"${list.id}","${it.name}","${it.unit}","${it.type}","${it.reference}","${it.description}");
    </script>
</g:each>