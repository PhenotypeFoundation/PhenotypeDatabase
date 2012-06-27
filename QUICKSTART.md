# Three ways to get started

There are three ways of running GSCF:
* download a pre-compiled WAR into a Java servlet container such as Tomcat
* compile and run it from source using the command line
* compile and run it from source using a development environment

Also, you might want to
* [set up a dedicated server](https://trac.nbic.nl/gscf/wiki/QuickStartServer)

## Download a pre-compiled WAR into a Java servlet container

* Download the pre-compiled WAR from [Trac](https://trac.nbic.nl/gscf/downloads).
* Deploy the WAR into your Java servlet container (such as Apache Tomcat)
* By default, the gscf-[version]-test.war download will use an in-memory database, which means that the database is emptied and filled with some example data at each restart. You can specify a database by creating your own configuration: create a file .grails-config/test-gscf.properties in the home directory of the user that runs the servlet container process (probably 'tomcat'). See Configuration below.

See for more details the INSTALLATION file that comes with the downloaded version, or Database/ Production below.

## First time run from command line

If you run from source from command line, you don't need to set up a database as the default development configuration runs on an in-memory HSQLDB database.

The following list of commands is known to work under Ubuntu 8.04, provided you have put 
	
	grails-1.3.7
	 
in 
	
	/app
	
and installed 
	
	sun-java6-jdk
	
:
	
	export JAVA_HOME=/usr/lib/jvm/java-6-sun
	export GRAILS_HOME=/app/grails-1.3.7 # or change this to your grails directory
	export PATH=$PATH:$JAVA_HOME/bin:$GRAILS_HOME/bin
	svn co https://trac.nbic.nl/svn/gscf/trunk gscf
	cd gscf
	grails run-app # this fails with a plugin resolve error
	grails upgrade # and press Y at upgrade prompt
	grails run-app # now it should run fine, provided nothing else is running at port 8080
	grails war # to create a war file in /target
	

## Use a development environment

* The best free IDE out there for Grails seem to be Springsource Tool Suite and Netbeans at the moment. Make sure to use the latest version, as Grails support is still much under development. Blog post http://www.grailsblog.com/archive/show?id=15 shows how to get Netbeans and Grails working. Another editor with excellent (and paid) Grails support is IntelliJ.
* Checkout the gscf trunk from subversion in a new folder (https://trac.nbic.nl/svn/gscf/trunk)
* Open the folder in the Grails-enabled Netbeans, it should recognize the Grails project
* You might have to change your launch configuration to include the VM parameter -XX:MaxPermSize=256M if you run into Permgemspace errors
* Launch it from Netbeans (just press the green button ;-))
* If this is a first time checkout and launch, you might get dependency errors. This can be resolved by running 'grails run-app', 'grails upgrade --non-interactive', and then 'grails run-app' to run the application again.

# A little more background

## Configuration

By default, GSCF looks for configuration settings in the classpath directory, where a number of configuration files is supplied. Each Grails 'environment' has its own default configuration file named config-[environment name].properties. If you downloaded the compiled WAR, your environment is 'test' and your local settings can be retrieved from the file [config-test-properties](https://trac.nbic.nl/gscf/browser/trunk/grails-app/conf/config-test.properties).

You can override this by creating a file named [environment name]-gscf.properties in a directory named .grails-config, which should reside in the home folder of the user that is running Tomcat. For example, if you use Debian Lenny with a default Tomcat installation and the pre-compiled GSCF WAR file, the configuration file name would become /home/tomcat55/.grails-config/test-gscf.properties. To find the expected file name, you can also look into the 'catalina' logfile of Tomcat, where you will find a helpful message stating 'Unable to load specified config location file' and the expected location of the custom configuration file.

## Grails plugins

When you run 'grails run-app' (as Netbeans does behind the scenes), grails automatically installs any plugins on which the application is depending. To view which plugins are in your Grails cache, run:
	
	ls ~/.grails/1.1.1/plugins
	
* As of Grails 1.3.1, the plugin structure has changed, and this causes sometimes 'plugin not found' errors even on a fresh Grails download and project checkout. Try running 'grails upgrade', that should solve the problem.
* It seems that Netbeans sometimes has trouble installing the Nimble plugin, so if it's not there (grails-nimble-0.3-SNAPSHOT.zip), try running 'grails run-app' from the command line in the application directory.
* Also, if you get complaints about a certain plugin when building from scratch, it could be that some of the plugin versions were are using (see 'application.properties' in the project root) are outdated. Since Grails automatically fetches the newest version of any plugin when installing from scratch, this causes 'artefact not found' errors. You can check for plugin updates with the command 'grails list-plugin-updates'. One way of solving this problem is to upgrade the project plugin version to the newest available version (preferably only latest stable, no RC's), either by updating the version number in 'application.properties' or by using the install-plugin command, e.g. 'grails install-plugin webflow 1.2.2'.

## Database/ Production environment

In production environment, we use a real database as backend, whereas we use an in-memory database in the production environment. (All of this can be changed in grails-app/conf/Datasource.groovy). We tested it with PostgreSQL 8.3, and that works out of the box. The only thing you need to do is to create an empty database; Hibernate will automatically create the database schema in the first run. Also, the application will add test users if there are no users in the database.

To create the Postgres database (be sure to set authentication to md5 in pg_hba.conf):
	
	sudo -u postgres createuser -d -R -P gscf;
	sudo -u postgres createdb -O gscf gscf;
	(and enter 'dbnp' as password)
	

To create the empty database in MySQL (not tested for all versions of GSCF):
	
	$ mysql --user=root
	mysql> create database gscf;
	mysql> use gscf;
	mysql> grant all on gscf. * to gscf@localhost identified by 'dbnp';
	mysql> flush privileges;
	mysql> exit
	

Also, make sure the right connector JARs are in the application /lib folder (both MySQL and Postgres drivers are in SVN now).

If you set this up correctly, just run the application in production mode and you will see that the database is filled:

	
	$ mysql --user=gscf -p --database=gscf
	mysql> show tables;
	+-------------------------------+
	| Tables_in_gscf                |
	+-------------------------------+
	| _group                        | 
	| _group_roles                  | 
	| _group_users                  | 
	| _role                         | 
	| _role_users                   | 
	| _user                         | 
	| _user__user                   | 
	| _user_passwd_history          | 
	...
	

## Testing

You can run both the unit tests and integration tests with the following Grails command:
	
	grails test-app
	

## Webserver production environment

### Deploy in a Tomcat container

To generate a production WAR, use 
	
	grails prod war
	

You can deploy this on Tomcat 6.
Known issues:
* If you downloaded Tomcat for Ubuntu, do not forget to set executable file properties to the scripts in /bin: 
	
	chmod u+x *.sh
	
* You need to increase the Permgenspace, otherwise Tomcat will crash. This can be done by setting 
	
	export CATALINA_OPTS="-Xmx1024M -XX:MaxPermSize=256M"
	
* You need to upgrade to the latest version of JQuery plugin 1.4.1.1 (see http://jira.codehaus.org/browse/GRAILSPLUGINS-1864), otherwise you will get an error concerning JQueryTagLib: 
	
	org.springframework.beans.ConversionNotSupportedException: Failed to convert property value of type 'org.apache.catalina.loader.WebappClassLoader' to required type 'groovy.lang.GroovyClassLoader' for property 'classLoader'; nested exception is java.lang.IllegalStateException: Cannot convert value of type [org.apache.catalina.loader.WebappClassLoader] to required type [groovy.lang.GroovyClassLoader] for property 'classLoader': no matching editors or conversion strategy found
	
* The searchable plugin sometimes causes errors: http://n4.nabble.com/searchable-tomcat6-fail-td1339718.html - temporary fix: delete searchable plugin and searchable and plugin controllers+views
* we use scaffolding, so make sure all scaffolding files (like Controller.groovy) are present in /src/templates/scaffolding when you build the war. You can install them with
	
	grails install-templates
	

### Set up a virtual host in Apache

Of course, in the end you want users to be able to access your website without having to type :8080 or any other port Grails is running on. To do this, you can use mod_jk to redirect traffic from (a certain path on) port 80 to your Grails instance. Also, you can use virtual hosts to be able to run multiple instances/versions alongside each other and redirect the user to the right instance according to the URL (e.g. demo.dbnp.org points to the GSCF instance for test users, but the other URLs point to our production environments).
You will need to configure the following Apache configuration files (found in /etc/apache2 on most *nix systems), assuming you want to map :

=### worker.properties=

First, we have to define the AJP 1.3 connector to the Tomcat instance. Assuming it's running on port 8080 here:

	
	worker.list=gscf
	
	worker.gscf.type=ajp13
	worker.gscf.host=localhost
	worker.gscf.port=8080
	

=### jk-httpd.conf=

Next, we have to configure JK to mount the actual GSCF instance on the worker. Assuming it's running on <server url>:<tomcat port>/gscf-0.4.0 here: 

	
	LoadModule jk_module /usr/lib/apache2/modules/mod_jk.so
	
	JkWorkersFile /etc/apache2/workers.properties
	
	# Where to put jk logs
	JkLogFile     /var/log/apache2/mod_jk.log
	
	# Set the jk log level [debug/error/info]
	JkLogLevel    info
	
	# Select the log format
	JkLogStampFormat "[%a %b %d %H:%M:%S %Y] "
	
	# JkOptions indicate to send SSL KEY SIZE, 
	JkOptions     +ForwardKeySize +ForwardURICompat -ForwardDirectories
	
	# JkRequestLogFormat set the request format 
	JkRequestLogFormat     "%w %V %T"
	
	# mount gscf release 0.4.0
	# url: nbx14.nugo.org/gscf-0.4.0/
	#JkMount  /gscf-0.4.0/* gscf
	#JkMount  /gscf-0.4.0 gscf
	

=### Set up virtual host=

Then, we have set up a virtual host for each instance that we want to be reachable from outside on a different address. In this example, we configure a host called example.dbnp.org, and we restrict access to the instance by using basic HTTP username/password authentication.
The convention for apache2 on *nix systems is to put this configuration in (/etc/apache)/sites-available, and then create a symbolic link to it from the directory (/etc/apache2)/sites-enabled. This way, you can easily disable the site temporarily by removing the link, without destroying or having to backup the configuration.

So we create a file /sites-available/dbnp.org_example.conf:
	
	<VirtualHost *:80>
	        ServerName example.dbnp.org
	
	        ErrorLog /var/log/apache2/example.dbnp.org-error.log
	        CustomLog /var/log/apache2/example.dbnp.org-access.log combined
	
	        ServerAdmin your@email
	
	        DocumentRoot "/home/tomcat/apache-tomcat-6.0.26/webapps/" # fill in the path to your tomcat instance
	        <Location />
	                AuthType Basic
	                AuthName "GSCF Example instance"
	                AuthUserFile /home/tomcat/authentication/users # your authentication user file
	                AuthGroupFile /home/tomcat/authentication/groups # your authentication groups file
	                Require group exampleusers
	                Order allow,deny
	                #If you want to explicitly enable access from a certain IP (e.g. a trusted REST client):
	                #Allow from 1.2.3.4
	                Satisfy any
	        </Location>
	
	        <IfModule mod_jk.c>
	                JkMount /* gscf
	        </IfModule>
	
	        <IfModule mod_rewrite.c> # Enable access by version number
	                RewriteEngine on
	                RewriteLog "/var/log/apache2/domain-rewrite.log"
	
	                RewriteRule ^/gscf-([0-9\.]{1,})(.*)$ $2 [NC,NE]
	                RewriteRule ^/(.*)$ /gscf-0.4.0/$1 [L,PT,NC,NE]
	        </IfModule>
	
	</VirtualHost>
	

Then, we create a link to the file in /sites-enabled:
	
	kees@server:/etc/apache2/sites-enabled$ sudo ln ../sites-available/dbnp.org_example.conf
	

This will create a link to the configuration file we just created with the same name in the current directory, which should be sites-enabled. You can check this with ls -al.

=### Configure user access=

You probably noticed that in the previous step, we defined two authentication files, in this case /home/tomcat/authentication/users and /home/tomcat/authentication/groups, but you can place them wherever you want. Of course, make sure that you secure access and that the user that is running the apache process is allowed to read the file (as goes for all those files). You can create the files by using ht_passwd (see the [documentation](http://httpd.apache.org/docs/2.2/programs/htpasswd.html)). Also be sure to check the [Apache security tips](http://httpd.apache.org/docs/2.2/misc/security_tips.html).

