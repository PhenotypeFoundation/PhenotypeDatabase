Generic Study Capture Framework
====

## WAR files
We have the most recent builds available for download as ```war``` files which can be deployed on an application container (e.g. Apache Tomcat). 

War file | Build Environment | Build Status | Source | Config Location
--- | --- | --- | --- | --- | ---
[GSCF](http://download.dbnp.org/production/gscf.war) | ```production``` | [![Build Status](http://jenkins.dbnp.org/job/production-gscf/badge/icon)](http://jenkins.dbnp.org/job/production-gscf/) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/GSCF) | ~/.gscf/production.properties [?](https://github.com/PhenotypeFoundation/GSCF/blob/master/grails-app/conf/default.properties)
_Note: each project / environment requires a specific configuration file._

# Installation
In this guide we will assume you use Linux as a hosting platform. While you will be able to run GSCF on Windows, using Linux is preferable. This guide is written with [Debian GNU/Linux](http://www.debian.org/) (or Ubuntu) as a hosting platform. As Linux distributions differ other distributions may require minor changes in setup, but following this guide you should be able to get things running on other distributions as well.

### Assumptions
The tutorial is based on a number of assumptions:
* you have root access to the server
* we will set up a gscf test instance on test.mysite.com on IP (1.2.3.4)
* a DNS record is available for test.mysite.com
* our database will be named 'mytestdb' with username 'mydbuser' and password 'mydbpassword'
* we will have one administrator user (user 'admin', password 'adminpw')
* we will have one default user (user 'user', password 'userpw')

### Requirements
Before we can set up the server, the following requirements should be met:
* Apache Tomcat ≥ 7.x.x
* Apache Webserver ≥ 2.x (+mod_proxy, +mod_rewrite)
* PostgreSQL database server ≥ 9.2
* Active internet connection (with change of code other options are to install a local instance of BioPortal or remove the link to BioPortal)

Installation is quick and easy:
	
	apt-get install tomcat7 postgresql-9.2 apache2 libapache2-mod-proxy-html libapache2-mod-jk
	

### Set Up the Database
su to user postgres and create the database:

Note: you may have to use double quotes (") rather than single quotes (').

	
	root@nmcdsp:~# su - postgres
	postgres@nmcdsp:~$ psql
	psql (9.2.4)
	Type "help" for help.
	
	postgres=# create database 'gscf-www';
	CREATE DATABASE
	postgres=# create user gscfuser password 'mydbpassword';
	CREATE ROLE
	postgres=# grant all privileges on database 'gscf-www' to gscfuser;
	GRANT
	postgres=# alter database 'gscf-www' owner to gscfuser;
	ALTER DATABASE
	postgres=# \l
	           List of databases
	    Name     |     Owner     | Encoding 
	-------------+---------------+----------
	 gscf-www    | gscfuser      | UTF8
	 postgres    | postgres      | UTF8
	 template0   | postgres      | UTF8
	 template1   | postgres      | UTF8
	(16 rows)
	
	postgres=# \q
	postgres@nmcdsp:~$ exit
	logout
	root@nmcdsp:~# 
	

### Set up the application configuration
As of GSCF 0.8.3 a setup wizard is included which will create a configuration file for you (/path/to/homedir/.gscf/environment.properties). However, to run this wizard, you need a working instance, so it might be more convenient to write the configuration yourself.
If you start the application, you will see exactly at which location it is looking for a configuration file. It is probably /usr/share/tomcat7/.gscf/production.properties. You can use it to specify the database connection.

The latest version of the configuration file can be found [HERE](https://github.com/PhenotypeFoundation/GSCF/blob/master/grails-app/conf/default.properties).

_Don't forget to change the default passwords to something more secure!_

_You will need to request an API key by creating an account on bioportal.org._

## Create a .grails directory
Grails uses a cache folder, which should be created if the tomcat user cannot create it

	
	root@nmcdsp:~# mkdir -p /usr/share/tomcat7/.grails;chown tomcat7.tomcat7 /usr/share/tomcat7/.grails;chmod -R gou+rwx /usr/share/tomcat7/.grails
	

### Install GSCF
Download and install the latest WAR from the section above, and deploy it on your application container (e.g. Apache Tomcat).
	
	
### Start GSCF
You should now be able to start tomcat and run the GSCF application:

	
	root@nmcdsp:~# /etc/init.d/tomcat7 start
	 * Starting Tomcat servlet engine tomcat7                                [ OK ]
	root@nmcdsp:~# 
	

### Set Up Apache to proxy / rewrite request
As tomcat is running (by default) on 8080, it is not very professional to have your application run on http://test.mysite.com:8080/gscf. Instead http://test.mysite.com is preferable. Also it is convenient to be able to add load balancing functionality in case you expect high load. Apache can solve these issues.

First, make sure Apache loads all modules we require:
	
	root@nmcdsp:~# cd /etc/apache2/mods-enabled/
	root@nmcdsp:/etc/apache2/mods-enabled# ln -s ../mods-available/proxy* .
	root@nmcdsp:/etc/apache2/mods-enabled# ln -s ../mods-available/rewrite.load .
	

Then create a new virtual host configuration for test.mysite.com and edit it:
	
	root@nmcdsp:/etc/apache2/mods-enabled# cd /etc/apache2/sites-available/
	root@nmcdsp:/etc/apache2/sites-available# nano mysite.com_gscf-test.conf
	

Paste the following content into the virtual host configuration:

	<VirtualHost *:80>
		ServerName test.mysite.com
		ServerAlias test.gscf.mysite.com
	
		ErrorLog /var/log/apache2/gscf-test-error.log
		CustomLog /var/log/apache2/gscf-test-access.log combined
	
		<IfModule mod_rewrite.c>
			RewriteEngine on
	
	                # keep listening for the serveralias, but redirect to
	                # servername instead to make sure only one user session
	                # is created (tomcat will create one user session per
	                # domain which may lead to two (or more) usersessions
	                # depending on the number of serveraliases)
	                # see gscf ticket #321
			RewriteCond %{HTTP_HOST} ^test.gscf.mysite.com$ [NC]
			RewriteRule ^(.*)$ http://test.mysite.com$1 [R=301,L]
	
			# rewrite the /gscf-a.b.c-environment/ part of the url                
			RewriteCond %{HTTP_HOST} ^test.mysite.com$ [NC]
			RewriteRule ^/gscf/(.*)$ /$1 [L,PT,NC,NE]
		</IfModule>
	
		<IfModule mod_proxy.c>
			<Proxy *>
				Order deny,allow
				Allow from all
			</Proxy>
	
			ProxyStatus On
			ProxyPass / balancer://gscf-cluster/gscf/ stickysession=JSESSIONID|jsessionid nofailover=On
			ProxyPassReverse / balancer://gscf-cluster/gscf/
			ProxyPassReverseCookiePath /gscf /
	
	                <Location />
	                        SetOutputFilter proxy-html
	                        ProxyHTMLDoctype XHTML Legacy
	                        ProxyHTMLURLMap /gscf/ /
	                </Location>
	
			<Proxy balancer://gscf-cluster>
				BalancerMember ajp://localhost:8009
			</Proxy>
		</IfModule>
	</VirtualHost>
		
and press CTRL-X (and Y) to save

Now enable this virtual host configuration:
	
	root@nmcdsp:/etc/apache2# cd /etc/apache2/sites-enabled
	root@nmcdsp:/etc/apache2/sites-enabled# ln -s ../sites-available/mysite.com_gscf-test.conf	
And reload apache to use the newly created virtual host configuration:
	
	root@nmcdsp:/etc/apache2/sites-enabled# /etc/init.d/apache2 reload
	
Your site should now be up and running and listening on http://test.mysite.com (and server alias test.gscf.mysite.com)

### Loadbalancing
If you, at some point in the future, require more nodes serving GSCF you can change the virtual host configuration above to include multiple BalancerMembers in the balancer configuration. For example, you could set up one Apache Webserver to act as a loadbalancer to a number of tomcat servers running in a DMZ:
	
			<Proxy balancer://gscf-cluster>
				BalancerMember ajp://10.0.0.10:8009
				BalancerMember ajp://10.0.0.11:8009
				BalancerMember ajp://10.0.0.12:8009
				BalancerMember ajp://10.0.0.13:8009
			</Proxy>

Caveats: GSCF has not yet been tested in such an environment. Other things to keep in mind when moving towards loadbalancing:
* the tomcat sessions need to be synchronized as well as shared storage (gfs seems best equiped), unless a client stays on the same node during the duration of his session
* probably one PostgreSQL database can manager all members, however when load becomes too high one might also introduce one or more PostgreSQL servers. Possible read/write and read only database servers. This however required changes in the codebase to support such features.

# Running the source
The project is developed using the [Grails](http://grails.org) Framework, so you either need an IDE that supports Grails (we use Intellij) or run it in your terminal. In either case you need to [download](http://grails.org/download) install Grails (version [2.2.0](https://github.com/PhenotypeFoundation/GSCF/blob/master/application.properties) at the time of writing).


### Running in your terminal
When you have successfully installed the Grails web application framework, you should be able to run Grails in your terminal:

	root@nmcdsp:~/projectRoot/ grails run-app 

When the application is running, you should be able to access it at http://localhost:8080/gscf


### Running in your IDE
Most of the developers on this project favor Intellij over Eclipse, as we feel it integrates best with Groovy & Grails. Running GSCF in Intellij is as easy as configuring Grails and running the Application.

[![Intellij](http://www.jetbrains.com/idea/opensource/img/all/banners/idea120x60_blue.gif)](http://www.jetbrains.com/idea/features/javascript.html)

### VM Options

```
-Xms1048m -Xmx1048m -XX:PermSize=1048m -XX:MaxPermSize=2048m -XX:MaxHeapFreeRatio=70 -XX:MaxGCPauseMillis=10 -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -javaagent:/Users/jeroen/libs/grails-2.2.0/lib/org.springsource.springloaded/springloaded-core/jars/springloaded-core-1.1.1.jar -Xverify:none
```

# License
   Copyright 2009 Phenotype Foundation & Netherlands Metabolomics Centre

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   

# Technologies
[![Grails](http://www.chip.de/ii/6/0/5/9/2/7/1/95f75d6b0f329cb3.jpg)](http://www.grails.org)
[![jQuery](http://3.bp.blogspot.com/-Pv6D2RbhMoY/UfklyE_3fkI/AAAAAAAAAo0/wftYaC95wQg/s1600/logo-jquery2.png)](http://jquery.org)
[![jQuery-UI](http://www.bits4beats.it/wp-content/uploads/2010/02/jquery_ui_logo.png)](http://jqueryui.com)
[![PostgreSQL](http://avatar3.status.net/i/identica/42228-96-20090305041844.png)](http://www.postgresql.org)
[![JenkinsCI](http://blog.finalist.nl/wp-content/uploads/2012/03/jenkins-headshot.png)](http://jenkins-ci.org)
[![Apache Tomcat](http://cdn.kemptechnologies.com/files/pages/kemp-loadmaster-remote-configuration-service/tomcat-logo.jpg)](http://tomcat.apache.org)
[![Apache](http://imagenes.es.sftcdn.net/es/scrn/8000/8653/apache-http-server-11.jpg)](http://httpd.apache.org)
[![Intellij](http://www.jetbrains.com/idea/opensource/img/all/banners/idea120x60_blue.gif)](http://www.jetbrains.com/idea/features/javascript.html)


