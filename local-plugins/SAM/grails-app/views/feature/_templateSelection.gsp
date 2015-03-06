<%@ page import="org.dbxp.sam.Feature" %>
<af:templateElement name="template" rel="template" description="" value="${template}" error="template" entity="${Feature}" ontologies="" addDummy="true" onChange="handleTemplateChange( \$( 'option:selected', \$(this) ) );">
</af:templateElement>