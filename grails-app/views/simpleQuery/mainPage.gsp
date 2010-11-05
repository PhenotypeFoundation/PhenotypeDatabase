<%--
  Created by IntelliJ IDEA.
  User: luddenv
  Date: 26-mei-2010
  Time: 13:17:50
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<meta name="layout" content="main"/>
<g:if env="production">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'simpleQuery.min.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'simpleQuery.min.js')}"></script>
</g:if><g:else>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'simpleQuery.css')}"/>
	<script type="text/javascript" src="${resource(dir: 'js', file: 'simpleQuery.js')}"></script>
</g:else>
 <style type="text/css">
  #spotlight { display: block; align: center; padding: 0px; }
  #spotlight #begin { display: inline-block; background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-begin.png')}); height: 30px; width: 140px; }
  #spotlight #middle { display: inline-block; background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-middle.png')}); height: 30px; repeat-x top left; }
  #spotlight #end { display: inline-block; background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-end.png')}); height: 30px; width: 28px; }
  #spotlight #submit { display: inline-block; height: 30px; }

	#simpleQuery {
	}

	#simpleQuery .search {
		display: block;
		height: 30px;
		margin-bottom: 10px;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}
	#simpleQuery .search .begin {
		margin: 0px;
		padding: 0px;
		display: inline-block;
		background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-begin.png')});
		height: 30px;
		width: 140px;
		vertical-align: top;
		text-align: right;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
	}
  #simpleQuery .search .begin .label {
	  color: #fff;
	  font-face: Arial;
	  line-height: 30px;
	  text-shadow: 0px 0px 1px #006DBA;
	  font-size: 12px;
    margin-right: 23px;
  }
  #simpleQuery .search .middle {
		margin: 0px 0px -20px 0px;
	  padding: 0;
	  display: inline-block;
	  background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-middle.png')});
	  height: 30px;
	  width: 200px;
	  vertical-align: top;
	  zoom: 1; /* IE 6 & 7 hack */
	  *display: inline; /* IE 6 & 7 hack */
  }
  #simpleQuery .search .searchfield {
	  vertical-align: middle;
		width: 100%;
	  height: 100%;
	  color: #006DBA;
	  border-width:0px;
	  border: none;
	  background-color:Transparent;
	  zoom: 1; /* IE 6 & 7 hack */
	  *display: inline; /* IE 6 & 7 hack */
  }
  #simpleQuery .search .end {
	  margin: 0px;
	  padding: 0px;
	  display: inline-block;
	  background-image: url(${resource(dir: 'images', file: 'simpleQuery/spotlight-end.png')});
	  height: 30px;
	  width: 28px;
	  zoom: 1; /* IE 6 & 7 hack */
	  *display: inline; /* IE 6 & 7 hack */
  }
	#simpleQuery .search .submit {
		vertical-align: top;
	  margin: 0px;
	  padding: 0px;
	  display: inline-block;
	  height: 30px;
	  width: 100px;
		border: 0;
		zoom: 1; /* IE 6 & 7 hack */
		*display: inline; /* IE 6 & 7 hack */
  }
	#simpleQuery .search .submit input {
		width:30px;
		height:30px;
		border: 0px;
		background-image: url(${resource(dir: 'images', file: 'simpleQuery/button.png')});
	}
 </style>
</head>
<body>
<g:render template="common/query"/>
</body>
</html>