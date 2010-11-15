<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />

        <style type='text/css' media='screen'>
        #register {
                margin:15px 0px; padding:0px;
                text-align:center;
        }
        #register .inner {
                width:260px;
                margin:0px auto;
                text-align:left;
                padding:10px;
                border-top:1px dashed #499ede;
                border-bottom:1px dashed #499ede;
                background-color:#EEF;
        }
        #register .inner .fheader {
                padding:4px;margin:3px 0px 3px 0;color:#2e3741;font-size:14px;font-weight:bold;
        }
        #register .inner .cssform p {
                clear: left;
                margin: 0;
                padding: 5px 0 8px 0;
                padding-left: 105px;
                border-top: 1px dashed gray;
                margin-bottom: 10px;
                height: 1%;
        }
        #register .inner .cssform input[type='text'] {
                width: 120px;
        }
        #register .inner .cssform label {
                font-weight: bold;
                float: left;
                margin-left: -105px;
                width: 100px;
        }
        #register .inner .login_message {color:red;}
        #register .inner .text_ {width:120px;}
        #register .inner .chk {height:12px;}
        </style>


        <title>User registration</title>
    </head>
    <body>
        <div class="body" id="register">
          <div class="inner">
            <g:if test="${flash.message}"> <div class='login_message'>${flash.message}</div></g:if>

              <div class='fheader'>Please enter username and email address. </div>
              <form action='/gscf/userRegistration/add' method='POST' id='loginForm' class='cssform' autocomplete='off'>
                      <p>
                              <label for='username'>Username</label>
                              <g:textField name="username" value="${username}" />
                      </p>
                      <p>
                              <label for='password'>Email address</label>
                              <g:textField name="email" value="${email}" />
                      </p>
                      <p>
                              <input type='submit' value='Register' />
                      </p>
              </form>
          </div>
        </div>
  </body>
</html>
