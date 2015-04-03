<%@page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
<head>
	<title><g:layoutTitle default="${grailsApplication.config.application.title}"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta property="og:title" content="${grailsApplication.config.application.title}"/>
	<meta property="og:description" content="A generic tool for planning scientific studies, and capturing study meta-data, integrating with analysis platform(s) / LIMS systems and searching relevant studies."/>
	<meta property="og:type" content="non_profit"/>
	<meta property="og:image" content="${resource(dir: 'images', file: 'facebookLike.png', absolute: true)}"/>
	<meta property="fb:admins" content="721482421"/>
	<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
    <r:require modules="gscfmain"/>
    <r:layoutResources />

    <script type="text/javascript">var baseUrl = '${resource(dir: '')}';</script>

    <g:layoutHead/>
</head>
<body>
<g:render template="/common/header"/>
<div id="content">
    <div class="inner">
        <div class="container">
            <g:layoutBody/>
        </div>
    </div>
</div>
<div id="footer">
    <div class="container"><p class="copyright">Copyright © 2008 - <g:formatDate format="yyyy" date="${new Date()}"/> NuGO, NMC and NBIC. All rights reserved. For more information go to <a href="//dbnp.org">http://dbnp.org</a>.</p></div>
</div>
<trackr:track reference="${session?.gscfUser ? session.gscfUser : '-'}"/>

<div id="dialog-creative-commons" title="License agreement" style="display:none">
    <p>
        By publishing this study you agree that:<br/>
    <li>the study will be available under the <a target="_ccl" rel="license" href="//creativecommons.org/licenses/by-sa/3.0/deed.en_US">Creative Commons Attribution-ShareAlike 3.0 Unported License</a></li>
    <li>you are legally entitled to accept this agreement</li>
</p>
    <p>
        Do you agree to these terms?
    </p>
    <p>
        <a target="_ccl" rel="license" href="//creativecommons.org/licenses/by-sa/3.0/deed.en_US"><img alt="Creative Commons License" style="border-width:0" src="//i.creativecommons.org/l/by-sa/3.0/88x31.png" /></a>
    </p>
</div>
<r:layoutResources />
</body>
</html>