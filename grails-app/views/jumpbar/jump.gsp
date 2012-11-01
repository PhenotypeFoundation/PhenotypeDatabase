<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>

      <title>${pageTitle}</title>

      <meta name="layout" content="main" />
      <style type="text/css">

        #contentIFrame {
          border: 0;
          height: 0;
          width: 100%;
        }

      </style>

      <script type="text/javascript">

       (function($) {

         var oldIFrameHeight = 0;

         var fnSetIFrameHeight = function() {

           var iFrameHeight = Math.max(
                   200,
                   $(window).height() - $('body').height() + oldIFrameHeight - 4); // TODO: figure out what's the 4 for

           // Set content frame height to fill all space left, but at least 200px
           $('#contentIFrame').height( iFrameHeight );

           oldIFrameHeight = iFrameHeight;

         }

         $(document).ready(fnSetIFrameHeight);

         $(window).resize(fnSetIFrameHeight);

       })(jQuery);

      </script>

  </head>
  <body>
  <iframe id="contentIFrame" src=${frameSource}>Your browser does not support iFrames.</iframe>
  </body>
</html>