<%
/**
 * Navigation template
 *
 * @author Jeroen Wesbeek
 * @since  20101220
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<g:set var="showPrevious" value="${page>1 && page<pages.size}"/>
<g:set var="showNext" value="${page<pages.size}"/>
<g:set var="showQuickSave" value="${quickSave && page<pages.size}"/>
<af:navigation events="[previous:[label:'&laquo; prev',show: showPrevious], cancel:[label:'cancel',show: showCancel], quickSave:[label: 'quick save', show:showQuickSave], next:[label:'next &raquo;', show:showNext]]" separator="&nbsp; | &nbsp;" class="prevnext" />