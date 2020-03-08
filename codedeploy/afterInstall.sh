#!/bin/bash
#Commands to run after after installation
echo "Entered after install hook"

sudo rm -rf /opt/tomcat/webapps/docs  /opt/tomcat/webapps/examples /opt/tomcat/webapps/host-manager  /opt/tomcat/webapps/manager /opt/tomcat/webapps/cloud-0.0.1-SNAPSHOT

sudo chown tomcat:tomcat /opt/tomcat/webapps/cloud-0.0.1-SNAPSHOT.war

#Killing the application
kill -9 $(ps -ef|grep cloud-0.0.1 | grep -v grep | awk '{print $2}')

#Removing log files
sudo rm -rf /opt/tomcat/logs/catalina*
sudo rm -rf /opt/tomcat/logs/*.log
sudo rm -rf /opt/tomcat/logs/*.txt

echo "After install hook completed successfully"
