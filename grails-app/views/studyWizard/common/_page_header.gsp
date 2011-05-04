<%
/**
 * page header template
 *
 * This template is actually rendered by the AjaxflowTagLib using
 * the following tags:
 *
 * <af:pageHeader>
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
<g:hiddenField name="do" value="" />
<h1><g:if test="${(study && study.getFieldValue('title'))}">${study.title}</g:if><g:else>${pages[page - 1].title}</g:else> (page ${page} of ${pages.size()})</h1>
<g:render template="common/tabs"/>
<div class="content">
