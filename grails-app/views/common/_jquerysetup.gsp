<link rel="stylesheet" href="${createLinkTo(dir:'css/jquery-ui', file: 'jquery-ui-1.7.2.custom.css')}"/>
<!--
  we'd rather like to use this tag, as this tag uses the proper jquery version
  as maintained by the jquery plugin. However, nimble is conflicting at this
  moment and tries to fetch jquery from it's plugin resource location (which
  you can see in the HTML rendered version of this layout in the nimble views.
  So for now we will use the hardcoded javascript tag instead and comment out:
  <g:javascript library="jquery" />
  @see http://jira.codehaus.org/browse/GRAILSPLUGINS-1658
  @see http://github.com/intient/nimble/issues#issue/55
//-->
<script type="text/javascript" src="/gscf/js/jquery/jquery-1.3.2.js"></script>
