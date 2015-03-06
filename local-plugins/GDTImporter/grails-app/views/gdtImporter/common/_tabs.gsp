<%
/**
 * wizard tabs
 *
 * The 'pages' and 'page' variables are defined
 * in the flow scope. See WizardController:pagesFlow:onStart
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information: 
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
%>
<af:tabs pages="${pages}" page="${page}" clickable="${false}" />