<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Password reset at GSCF</title>
  </head>
  <body>
      <h3>Hello ${user.username}</h3
      <p>You (or someone pretending to be you) has requested a password reset at GSCF. I you want to reset your password, click <a href="${url}">here</a>.</p>
      <p>If you can not click the link, copy this url to the browser:</p>
      <p>${url}</p>
      <p>Kind regards,</p>
      <p>The GSCF team</p>
  </body>
</html>
