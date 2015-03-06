<%
/**
 * last wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev: 1469 $
 * $Author: t.w.abma@umcutrecht.nl $
 * $Date: 2011-02-01 17:33:41 +0100 (Tue, 01 Feb 2011) $
 */
%>
<script type="text/javascript">
    // disable redirect warning
    var warnOnRedirect = false;
</script>

<af:page>
<h1>Final Page</h1>
<p>
This concludes the importer wizard. <br />
<g:if test="${parentEntityObject}">
    Continue to
    <g:link class="edit linktips" title="Edit this study" controller="studyEdit" action="${entityToImport?.name}s" id="${parentEntityObject.id}">
        edit ${parentEntityObject}
    </g:link>
    or just <g:link controller="study" action="show" id="${parentEntityObject.id}">view ${parentEntityObject}</g:link><br /></g:if>
    You can click <g:link action="index">here</g:link> to restart the wizard.
</p>

All rows were imported succesfully.
</af:page>