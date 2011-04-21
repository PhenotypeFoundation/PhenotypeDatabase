<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <meta name="layout" content="main" />
    <title>Error</title>
  </head>
  <body>${flowExecutionException?.cause?.message ?: flash.errorMessage}</body>
</html>