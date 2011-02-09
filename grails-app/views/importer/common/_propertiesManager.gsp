<%
/**
 * Import Wizard properties manager template
 *
 * @author Tjeerd Abma
 * @since 20110209
 * @package wizard
 * @see dbnp.studycapturing.ImporterTagLib
 * @see dbnp.studycapturing.ImporterController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>

<g:javascript library="jquery" plugin="jquery"/>

<script type="text/javascript">
  // get all properties
  //$(this).parent().('select[name^=columnproperty.index.]').each ( function() {
            //});
            //console.log($(this).data)
</script>

<div id="propertiesManager" class="" title="Properties manager">
  <table>
    <g:form name="propertiesmanagerform" url="[action:'propertiesManager']">
      <tr>
        <td>Name:</td>
        <td>
          <input type="text" name="mappingname">
        </td>
      </tr>
      <tr>
        <td>
          <input type="submit" value="Save">
        </td>
      </tr>
      <input type="hidden" name="savepropertymapping" value="savepropertymapping"/>
    </g:form>
  </table>
</div>