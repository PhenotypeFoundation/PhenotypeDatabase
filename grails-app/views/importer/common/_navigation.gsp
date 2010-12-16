<%
/**
 * Navigation template
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<g:set var="showPrevious" value="${page>1 && page<pages.size}"/>
<g:set var="showNext" value="${page<pages.size}"/>
<af:navigation events="[previous:[label:'&laquo; prev',show: showPrevious], next:[label:'next &raquo;', show:showNext]]" separator="&nbsp; | &nbsp;" class="prevnext" />
