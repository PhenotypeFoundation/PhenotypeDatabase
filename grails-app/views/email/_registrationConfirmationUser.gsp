<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Welcome to ${grailsApplication.config.application.title}</title>
  </head>
  <body>
      <h3>Hello ${username}</h3
      <p>Welcome to ${grailsApplication.config.application.title}. Your account has been created, but you have to confirm your registration. Please click <a href="${link}">here</a> to confirm your registration.</p>
      <p>If you can not click the link, copy this url to the browser:</p>
      <p>${link}</p>

      <p>You should confirm your account before <g:formatDate format="dd-MM-yyyy hh:mm" date="${expiryDate}" />, otherwise your account will not be created.</p>

      <p>After you have confirmed your registration and the administrator has approved your account, you can login. Your password is:</p>
      <p><b>${password}</b></p>
      <p>Kind regards,</p>
      <p>The ${grailsApplication.config.application.title} team</p>
  </body>
</html>
