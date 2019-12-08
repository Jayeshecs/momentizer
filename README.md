# momentizer
Organize, Categorize, Search and better Manager your media

# Build
This project make use of Maven 3.2.1+ as build tools and hence make sure that you have Maven 3.2.1+ is installed and setup properly

Make use of below command to build this application - 

```
mvn clean install
```
# Package
Two WAR files are created after build is successful
1. momentarize-0.1-SNAPSHOT.war - This WAR is deployable in application server
2. momentarize-0.1-SNAPSHOT-jetty-console.war - This WAR is self executable

# Running

## Running using maven command

Make use of below command to run this application - 

```
mvn jetty:run
```

## Running using jetty console

In UNIX
```
cp target/*-console.war ./momentarize.war && java -jar momentarize.war --headless --port 8080
```

In WINDOWS
```
copy target\*-console.war .\momentarize.war && java -jar momentarize.war --headless --port 8080
```

## Deploying in application server

## Tomcat 8.5+ installed on UNIX

```
sudo service tomcat8 stop
cp target/momentarize*.war $CATALINA_HOME/webapps/momentarize.war
rm $CATALINA_HOME/webapps/*-console.war
rm -Rf $CATALINA_HOME/webapps/momentarize/
sudo service tomcat8 start
```

