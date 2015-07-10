<%@ page import="org.dbnp.gdt.AssayModule" %>
<div id="wrapper" xmlns="http://www.w3.org/1999/html">
    <div id="header">
        <div class="container">
            <g:link class="logo" controller="home"><img src="${resource(dir: 'images/default_style', file: 'phenotypedb-logo.png')}" height="65%" width="65%"/></g:link>
                <ul class="loginBlock">
                    <li>Hello
                    <sec:ifLoggedIn>
                        <g:if test="${session.gscfUser.shibbolethUser && session.gscfUser.displayName}">
                            ${session.gscfUser.displayName}
                        </g:if><g:else>
                            <sec:username/>
                        </g:else>
                    </sec:ifLoggedIn>
                    <sec:ifNotLoggedIn>
                        Guest
                    </sec:ifNotLoggedIn>
                    !</li>
                    <sec:ifLoggedIn>
                        <li><g:link controller="userRegistration" action="profile"><img src="${icon.userIcon(user:session.gscfUser, size: 20, transparent: false)}"></g:link></li>
                        <li><g:link controller="userRegistration" action="profile"><input class="button-1 pie" type="button" name="profile" value="Profile"/></g:link></li>
                        <li><g:link controller="logout" action="index"><input class="button-2 pie" type="button" name="logout" value="Logout"/></g:link></li>
                    </sec:ifLoggedIn>
                    <sec:ifNotLoggedIn>
                        <g:if test="${grailsApplication.config.authentication.shibboleth.toString().toBoolean()}">
                            <g:link class="open" controller="login">Log in</g:link>
                        </g:if>
                        <g:else>
                            <li><a class="signup pie" href="#" title="">Sign up</a></li>
                            <li><a class="login pie" href="#" title="">Log In</a></li>
                        </g:else>
                    </sec:ifNotLoggedIn>
                </ul>
        </div>
        <div class="topbar clearfix">
            <div class="container">
                <ul class="topnav">
                    %{--Should set current page on each and every page in order to use li class="active"--}%
                    %{--<li class="active"><a href="#" title="">Home</a></li>--}%
                    <li><g:link controller="home">Home</g:link></li>
                    <li>
                        <a href="#" title="">Create <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <li><g:link controller="studyEdit" action="add">Create a new study</g:link></li>
                            </ul>
                        </div>
                    </li>
                    <sec:ifLoggedIn>
                    <li>
                        <a href="#" title="">Import <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <li><g:link controller="studyImporter" action="chooseType">A part of the study design</g:link></li>
   			                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_TEMPLATEADMIN">
                                	<li><g:link controller="template" action="importTemplate">Templates</g:link></li>
                                </sec:ifAnyGranted>
                            </ul>
                        </div>
                    </li>
                    </sec:ifLoggedIn>
                    <li>
                        <a href="#" title="">Browse/Edit <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <sec:ifLoggedIn>
                                    <li><g:link controller="study" action="myStudies">My studies</g:link></li>
                                    <li><g:link controller="study" action="list">All studies</g:link></li>
                                </sec:ifLoggedIn>
                                <sec:ifNotLoggedIn>
                                    <li><g:link controller="study" action="list">View studies</g:link></li>
                                </sec:ifNotLoggedIn>
   			                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_TEMPLATEADMIN">
                                    <li class="has-child">
                                        <a href="#">Templates</a>
                                        <div class="subsubnav">
                                            <ul>
                                                <af:templateEditorMenu wrap="li" skipImport="true" skipExport="true" />
                                            </ul>
                                        </div>
                                    </li>
                                </sec:ifAnyGranted>
                                <sec:ifLoggedIn>
                                    <li class="has-child">
                                        <a href="#">Contacts</a>
                                        <div class="subsubnav">
                                            <ul>
                                                <li><g:link controller="person" action="list">View persons</g:link></li>
                                                <li><g:link controller="personAffiliation" action="list">View affiliations</g:link></li>
                                                <li><g:link controller="personRole" action="list">View roles</g:link></li>
                                            </ul>
                                        </div>
                                    </li>
                                </sec:ifLoggedIn>
                                <li class="has-child">
                                    <a href="#">Publications</a>
                                    <div class="subsubnav">
                                        <ul>
                                        <li><g:link controller="publication" action="list">View publications</g:link></li>
                                        <sec:ifLoggedIn>
                                        <li><g:link controller="publication" action="create">Add publication</g:link></li>
                                        </sec:ifLoggedIn>
                                        </ul>
                                    </div>
                                </li>
                            </ul>
                        </div>
                    </li>
                    <li>
                        <a href="#" title="">Analyze <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <li><g:link controller="advancedQuery">Search</g:link></li>
                                <li><g:link controller="visualize" action="index">Visualize</g:link></li>
                                <g:if env="development">
                                    <li><g:link controller="studyCompare" action="index">Compare</g:link></li>
                                </g:if>
                                <li><g:link controller="cookdata" action="index">Prepare Data</g:link></li>
                            </ul>
                        </div>
                    </li>
                   <sec:ifLoggedIn>
                    <li>
                        <a href="#" title="">Export <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <li><g:link controller="exporter" action="assays">Assay Data</g:link> </li>
                                <li><g:link controller="exporter" action="studies">Studies</g:link></li>
			                    <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_TEMPLATEADMIN">
                                	<li><g:link controller="template" action="export">Templates</g:link></li>
                                </sec:ifAnyGranted>
                            </ul>
                        </div>
                    </li>
                    </sec:ifLoggedIn>
                    <li>
                        <a href="#" title="">Modules <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                        <div class="subnav">
                            <ul>
                                <g:each in="${assayModules}" var="assayModule">
                                    <li><a href="${assayModule.url}">${assayModule.name}</a>
                                </g:each>
                            </ul>
                        </div>
                    </li>
                    <sec:ifAllGranted roles="ROLE_ADMIN">
                        <li>
                            <a href="#" title="">Admin <img class="subicon" src="${resource(dir: 'images/default_style', file: 'blank.gif')}" alt="" /></a>
                            <div class="subnav">
                                <ul>
                                    <li><g:link controller="user" action="userSearch"><img src="${fam.icon(name: 'user')}" alt="user administration"/> List Users</g:link></li>
                                    <g:if test="${!session.gscfUser.shibbolethUser}"><li><g:link controller="user" action="create"><img src="${fam.icon(name: 'user')}" alt="user administration"/> Create User</g:link></li></g:if>
                                    <li><g:link controller="userGroup" action="userGroupSearch"><img src="${fam.icon(name: 'group')}" alt="group administration"/> List Groups</g:link></li>
                                    <g:if test="${!session.gscfUser.shibbolethUser}"><li><g:link controller="userGroup" action="create"><img src="${fam.icon(name: 'group')}" alt="group administration"/> Create Group</g:link></li></g:if>
                                    <li><g:link controller="assayModule" action="index"><img src="${fam.icon(name: 'disconnect')}" alt="module administration"/> Manage Modules</g:link></li>
                                    <li><g:link controller="setup"><img src="${fam.icon(name: 'wand')}" alt="module administration"/> Setup wizard</g:link></li>
                                    <li><g:link controller="info"><img src="${fam.icon(name: 'lightning')}" alt="application information"/> Application information</g:link></li>
                                </ul>
                            </div>
                        </li>
                    </sec:ifAllGranted>
                </ul>
                <div class="search">
                    <g:form action="pages" name="simpleQueryForm" id="simpleQueryForm">
                        <g:if test="${search_term}"><g:set var="preterm" value="${search_term}"/></g:if>
                        <g:textField name="search_term" id="search_term" placeholder="Search term" value="${preterm}"/>
                        <img name="search_spinner" id="search_spinner" class="search_spinner" src="${resource(dir: 'images', file: 'spinner.gif')}" alt="" />
                        %{--<input class="pie" type="submit" value="Search" title="Search"  name="searchSubmit" id="searchSubmit" disabled="disabled"/>--}%
                        %{--Actual submit button is not needed, will use as label--}%
                        <input name="search_button" id="search_button" class="pie" type="button" value="Search" disabled="disabled"/>
                    </g:form>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="popup loginPopup">
    <a class="close" href="#" title="">x</a>
    <div class="content">
        <div class="fieldset signupForm">
            <h2>Not a member yet? Sign up!</h2>
            <g:form url="[action:'add',controller:'userRegistration']" class="clearfix registration">
                <ul class="form">
                    <li>
                        <input class="field" type="text" name="username" id="username" size="23" placeholder="Username"/>
                    </li>
                    <li>
                        <input class="field" type="text" name="email" id="email" size="23" placeholder="Email"/>
                    </li>
                    <li>A password will be emailed to you</li>
                    <li class="buttons">
                        <input class="button-2 pie" type="submit" value="Sign up" />
                    </li>
                </ul>
            </g:form>
        </div>
        <div class="fieldset loginForm">
            <h2>Login</h2>
            <g:form controller="." action="j_spring_security_check" method='POST' class="clearfix">
                <ul class="form">
                    <g:if test="${redirectUrl}">
                        <g:hiddenField name="spring-security-redirect" value="${redirectUrl}"/>
                    </g:if>

                    <li>
                        <input type="text" placeholder="Username" name="j_username" id="j_username" value="${username}" size="23"/>
                    </li>
                    <li>
                        <input type="password" placeholder="Password" name="j_password" id="password" size="23"/>
                    </li>
                    <li><input type="checkbox" class="icheckbox" name='_spring_security_remember_me' id='remember_me' <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                        <label for="remember_me">Remember me</label>

                        <a class="lost-pwd" href="<g:createLink url="[action:'forgotPassword',controller:'register']"/>">Lost your password?</a>
                    </li>
                    <li class="buttons">
                        <input class="button-1 pie" type="submit" name="submit" value="Login"/>
                    </li>
                </ul>
            </g:form>
        </div>
    </div>
</div>
