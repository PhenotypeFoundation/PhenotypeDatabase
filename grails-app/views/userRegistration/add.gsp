<html>
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>User registration</title>
    </head>
    <body>
        <div class="body">
          <g:if test="${flash.message}">
              <div class='login_message'>${flash.message}</div>
          </g:if>
            <g:if test="${success}">
                <div class='fheader'>
                    You have succesfully signed up for an account.
                    You will receive an email with instructions how to complete this subscription within a few minutes.<br/>
                    N.B. An administrator must approve your account before you can use it.
                </div>
            </g:if>
            <g:else>
                Please try again
            </g:else>
        </div>
  </body>
</html>
