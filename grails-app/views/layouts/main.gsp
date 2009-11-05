<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
 <head>
  <title><g:layoutTitle default="Grails" /></title>
  <link rel="stylesheet" href="${resource(dir:'css',file:session.style+'.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'login_panel.css')}" />
  <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
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
  <!-- /jquery //-->
  <!-- layouthead //-->
  <g:layoutHead />
  <!-- /layouthead //-->
  <script type="text/javascript" src="${resource(dir:'js', file:'login_panel.js')}"></script>
  <script type="text/javascript" src="${resource(dir:'js', file:'topnav.js')}"></script>
 </head>
 <body>
  <g:render template="/common/login_panel" />
  <div class="container">
   <div id="header">
    <g:render template="/common/topnav" />
   </div>
   <div id="content"><g:layoutBody /></div>
   <div id="footer">
     Copyright © 2008 - <g:formatDate format="yyyy" date="${new Date()}"/> NMC & NuGO. All rights reserved.
     ( style: <%=session.style%>,
     <a href="?showSource=true">show page source</a>)</div>
  </div>
 </body>
</html>