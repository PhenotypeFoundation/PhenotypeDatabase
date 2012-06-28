<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Welcome to ${grailsApplication.config.application.title}</title>
</head>

<body>
<p>Hello <b>${username}</b></p>

<p>
    Welcome to ${grailsApplication.config.application.title}. Your account has been created, but before you will be
    able to log in you have to confirm your registration. Please click <a href="${link}">here</a> to confirm your
    registration.
</p>

<p>If you are unable to click the link, copy this url into your browser:</p>

<p>${link}</p>

<p>You should confirm your account before <g:formatDate format="dd-MM-yyyy hh:mm" date="${expiryDate}"/>, otherwise your account will not be created.</p>

<p>After you have confirmed your registration an administrator still has to approve your registration. When your registration is approved your will be able to login.</p>

<p>Your password is: <b>${password}</b></p>

<p>Kind regards</p>
</body>
</html>