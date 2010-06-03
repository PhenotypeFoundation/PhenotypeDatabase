
<%@ page import="dbnp.studycapturing.Publication" %>
<%
    /**
     * Form to add publications based on PubMed articles
     *
     * @author Robert Horlings
     * @since 20100526
     * @see dbnp.studycapturing.PublicationController
     *
     * Revision information:
     * $Rev$
     * $Author$
     * $Date$
     */
%>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
 <head>
  <meta name="layout" content="dialog"/>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.js')}"></script>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.pubmed.js')}"></script>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'publication-chooser.programminglanguages.js')}"></script>
  <script type="text/javascript">
    PublicationChooser.prototype.availableDBs[ "pubmed" ].select = selectPubMedAdd;
    PublicationChooser.prototype.availableDBs[ "pubmed" ].close  = closePubMedAdd;
</script>
 </head>
 <body>
   <p>
     Search for a publication on pubmed. You can search on a part of the title or authors.
   </p>
   <g:form action="createFromPubmed">
    <g:textField name="publication" rel="publication-pubmed" style="width:500px;"/>
    <!-- <g:submitButton name="add" value="Add publication" /> -->
   </g:form>

 ${errors}
 ${message}

 <script type="text/javascript">
$(document).ready(function() {
  // initialize the ontology chooser
  PublicationChooser.init();
});
 </script>
 </body>
</html>