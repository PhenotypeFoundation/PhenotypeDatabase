<%@ page contentType="text/html;charset=UTF-8" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>User '${username}' registered at ${grailsApplication.config.application.title}</title>
</head>

<body>
<p>A new user has registed at <i>${grailsApplication.config.application.title}</i> with username <b>${username}</b> and email address <b>${email}</b>.</p>

<p>If you want to allow this person access to <i>${grailsApplication.config.application.title}</i>, please click <a
        href="${link}">here</a>".</p>

<p>If you unable to click the link, copy this url to the browser:</p>

<p>${link}</p>

<p>Kind regards</p>
</body>
</html>