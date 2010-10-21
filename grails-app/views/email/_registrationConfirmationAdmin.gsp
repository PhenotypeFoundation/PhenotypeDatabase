<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>User ${username} registered at GSCF</title>
  </head>
  <body>
      <p>A new user has registed at GSCF. His username is ${username}, and his email address is ${email}.</p>
      <p>If you want to allow this person access to GSCF, please click <a href="${link}">here</a>".</p>
      <p>If you can not click the link, copy this url to the browser:</p>
      <p>${link}</p>
      <p>Kind regards,</p>
      <p>The GSCF team</p>
  </body>
</html>