<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Template/Templatefield request</title>
  </head>
  <body>
      <h3>Hello admin</h3>
      <p>Category: ${requestcat}</p>
      <p>User: "${user}" (${user.email}) requested a ${requestnm} ${requestcat}</p>
      <p>Name: "${rname}" of the type: ${rtype}</p>
      <p>Specification of the needed ${requestnm} ${requestcat}: ${specification}</p>
      <p>Kind regards,</p>
      <p>The GSCF team</p>
  </body>
</html>
