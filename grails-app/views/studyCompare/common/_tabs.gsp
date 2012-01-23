<%
/**
 * wizard tabs
 *
 * The 'pages' and 'page' variables are defined
 * in the flow scope. See WizardController:pagesFlow:onStart
 *
 * @author Jeroen Wesbeek
 * @since  20120123
 *
 * Revision information:
 * $Rev:  68236 $
 * $Author:  duh $
 * $Date:  2011-01-18 15:57:41 +0100 (Tue, 18 Jan 2011) $
 */
%>
<af:tabs pages="${pages}" page="${page}" clickable="${true}" />
