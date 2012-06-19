In this guide we will assume you use Linux as a hosting platform. While you will be able to run GSCF on Windows, using Linux is preferable. This guide is written with [Debian GNU/Linux](http://www.debian.org/) (or Ubuntu) as a hosting platform. As Linux distributions differ other distributions may require minor changes in setup, but following this guide you should be able to get things running on other distributions as well.

# Assumptions
The tutorial is based on a number of assumptions:
* you have root access to the server
* we will set up a gscf test instance on test.mysite.com on IP (1.2.3.4)
* a DNS record is available for test.mysite.com
* our database will be named 'mytestdb' with username 'mydbuser' and password 'mydbpassword'
* we will have one administrator user (user 'admin', password 'adminpw')
* we will have one default user (user 'user', password 'userpw')

# Requirements
Before we can set up the server, the following requirements should be met:
* Apache Tomcat ≥ 6.x.x
* Apache Webserver ≥ 2.x (+mod_proxy, +mod_rewrite)
* PostgreSQL database server ≥ 8.4

Installation is quick and easy:
	
	apt-get install tomcat6 postgresql-8.4 apache2 libapache2-mod-proxy-html libapache2-mod-jk
	

# Set Up the Database
su to user postgres and create the database:

Note: you may have to use double quotes (") rather than single quotes (').

	
	root@nmcdsp:~# su - postgres
	postgres@nmcdsp:~$ psql
	Welcome to psql 8.3.14, the PostgreSQL interactive terminal.
	
	Type:  \copyright for distribution terms
	       \h for help with SQL commands
	       \? for help with psql commands
	       \g or terminate with semicolon to execute query
	       \q to quit
	
	postgres=# create database 'mytestdb';
	CREATE DATABASE
	postgres=# create user mydbuser password 'mydbpassword';
	CREATE ROLE
	postgres=# grant all privileges on database mytestdb to mydbuser;
	GRANT
	postgres=# alter database mytestdb owner to mydbuser;
	ALTER DATABASE
	postgres=# \l
	           List of databases
	    Name     |     Owner     | Encoding 
	-------------+---------------+----------
	 mytestdb    | mydbuser      | UTF8
	 postgres    | postgres      | UTF8
	 template0   | postgres      | UTF8
	 template1   | postgres      | UTF8
	(16 rows)
	
	postgres=# \q
	postgres@nmcdsp:~$ exit
	logout
	root@nmcdsp:~# 
	

# Set up the application configuration
As of GSCF 0.8.3 a setup wizard is included which will create a configuration file for you (/path/to/homedir/.gscf/environment.properties)

# Create a .grails directory
Grails uses a cache folder, which should be created if the tomcat user cannot create it

	
	root@nmcdsp:~# mkdir -p /usr/share/tomcat6/.grails;chown tomcat6.tomcat6 /usr/share/tomcat6/.grails;chmod -R gou+rwx /usr/share/tomcat6/.grails
	

# Install GSCF
Download and install the latest WAR from [https://trac.nbic.nl/gscf/downloads](https://trac.nbic.nl/gscf/downloads) (make sure to replace the URL and WAR names with the most recent versions).

	
	root@nmcdsp:~# curl "https://trac.nbic.nl/gscf/downloads/8" > /tmp/gscf-0.6.6-nmcdsptest.war
	  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
	                                 Dload  Upload   Total   Spent    Left  Speed
	100 47.8M  100 47.8M    0     0  6075k      0  0:00:08  0:00:08 --:--:-- 6602k
	root@nmcdsp:~# cd /var/lib/tomcat6/webapps/
	root@nmcdsp:/var/lib/tomcat6/webapps# cp /tmp/gscf-0.6.6-nmcdsptest.war .;chown tomcat6.tomcat6 *.war;chmod gou+rx *.war
	root@nmcdsp:/var/lib/tomcat6/webapps# 
	

# Run GSCF
You should now be able to start tomcat and run the GSCF application:

	
	root@nmcdsp:~# /etc/init.d/tomcat6 start
	Starting Tomcat servlet engine: tomcat6.
	root@nmcdsp:~# 
	

And check if it is running properly:

	
	root@nmcdsp:~# lynx localhost http://localhost:8080/gscf-0.6.6-nmcdsptest
	

# Set Up Apache to proxy / rewrite request
As tomcat is running (by default) on 8080, it is not very professional to have your application run on http://test.mysite.com:8080/gscf-0.6.6-nmcdsptest. Instead http://test.mysite.com is preferable. Also it is convenient to be able to add load balancing functionality in case you expect high load. Apache can solve these issues.

First, make sure Apache loads all modules we require:
	
	root@nmcdsp:~# cd /etc/apache2/mods-enabled/
	root@nmcdsp:/etc/apache2/mods-enabled# ln -s ../mods-available/proxy* .
	root@nmcdsp:/etc/apache2/mods-enabled# ln -s ../mods-available/rewrite.load .
	

Then create a new virtual host configuration for test.mysite.com and edit it:
	
	root@nmcdsp:/etc/apache2/mods-enabled# cd /etc/apache2/sites-available/
	root@nmcdsp:/etc/apache2/sites-available# nano mysite.com_gscf-test.conf
	

Paste the following content into the virtual host configuration:
	
	# Apache Virtual Host for GSCF Test Build
	#
	# Author  Jeroen Wesbeek <J****n.W******@gmail.com>
	# Since		20100825
	#
	# Revision Information:
	# $Author$
	# $Date$
	# $Rev$
	#
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
			RewriteRule ^/gscf-0.6.6-nmcdsptest/(.*)$ /$1 [L,PT,NC,NE]
		</IfModule>
	
		<IfModule mod_proxy.c>
			<Proxy *>
				Order deny,allow
				Allow from all
			</Proxy>
	
			ProxyStatus On
			ProxyPass / balancer://gscf-cluster/gscf-0.6.6-nmcdsptest/ stickysession=JSESSIONID|jsessionid nofailover=On
			ProxyPassReverse / balancer://gscf-cluster/gscf-0.6.6-nmcdsptest/
			ProxyPassReverseCookiePath /gscf-0.6.6-nmcdsptest /
	
	                <Location />
	                        SetOutputFilter proxy-html
	                        ProxyHTMLDoctype XHTML Legacy
	                        ProxyHTMLURLMap /gscf-0.6.6-nmcdsptest/ /
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
	

# Done
Your site should now be up and running and listening on http://test.mysite.com (and server alias test.gscf.mysite.com)

# Loadbalancing
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
