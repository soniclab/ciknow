C-IKNOW is a web application for data collection, analysis, recommendation, and
visualization.


*********************************************************************************
I, System Requirements							
*********************************************************************************
C-IKNOW requires Java 1.6.x, Apache Tomcat 6.x, MySQL 5.x database in 
the server side, and requires Flash Player 10.x in the client browser.



*********************************************************************************
II, Installation								
*********************************************************************************	
1, Database Preparation
(a) Install mysql (v.5+) (skip this step if already available)
http://www.mysql.com/downloads/mysql/
It is recommended to install mysql as a service.

(b) Create Database
(the command below assumming installation in Linux system)

login as root: 
# mysql -u root -p

create database:
mysql> create database _ciknow;
mysql> grant all privileges on _ciknow.* to sonic@localhost identified by 'sonic';


2, Java (skip this step if already available)
(a) Install Java Runtime Environment (JRE) or JDK version 1.6.x
http://www.oracle.com/technetwork/java/javase/downloads/index.html

(b) Set JAVA_HOME
# export JAVA_HOME=/path/to/java/installation/dir

(c) Set java in PATH
# export PATH=$JAVA_HOME/bin:$PATH


3, Tomcat (skip this step if already available)
(a) Install Tomcat (v.6)
http://tomcat.apache.org/download-60.cgi

(b) Set environment variable
# export CATALINA_HOME=/path/to/tomcat/installation/dir
# export PATH=%CATALINA_hOME/bin:$PATH


4, Startup tomcat, open browser, point to http://localhost:8080 to make sure 
tomcat is running correctly

	
5, Download _ciknow.war and drop it into tomcat "webapps" directory
Wait for the web application to load (about 10 seconds, depend on your system).
The .war file will be extracted automatically into a directory "_ciknow".
Make sure the folder owner has "rwx" permission.

Locate the default/base dataset in webapps/_ciknow/WEB-INF/sql/ciknow.sql 
Load base data:
# mysql -u sonic -p sonic _ciknow < path_to_sql_file

test in browser: http://localhost:8080/_ciknow/index.html

6, Login with default credentials: 
username: admin
password: admin

Please change your admin password after the first login.
(via Administration::Password::Change Your Password)


*********************************************************************************
III, Documentation							
*********************************************************************************
http://ciknow.northwestern.edu/documentation/



*********************************************************************************
IV, Inquery, Bug Report, and Feature Request				
*********************************************************************************
Email: ciknow@northwestern.edu
http://ciknow.northwestern.edu/contact/


*********************************************************************************
V, FAQ									
*********************************************************************************
1, My invitation failed (Got an "mailsendexception")?!
This is because the mail server address is not setup correctly. 
By default C-IKNOW use our "localhost" as your mail server, which may not exist in your server.

In order for the invitation to work for you, you need to edit the file
tomcat/webapps/_ciknow/WEB-INF/classes/applicationContext-mail.xml, search for
"mailSender", and update the "host" to your own mail server (SMTP host). Finally,
restart tomcat and test it again.