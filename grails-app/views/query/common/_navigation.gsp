<%
/**
 * Wizard navigational buttons
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev: 299 $
 * $Author: duh $
 * $Date: 2010-03-22 14:40:35 +0100 (Mon, 22 Mar 2010) $
 */
%>
    <div class="navigation">
      <g:if test="${page>1 && page<pages.size}"><wizard:ajaxButton name="previous" value="&laquo; prev" url="[controller:'query',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /></g:if>
      <g:if test="${page>1 && page<pages.size}"> | </g:if>
      <g:if test="${page<pages.size}"><wizard:ajaxButton name="next" value="next &raquo;" url="[controller:'query',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /></g:if>
    </div>
