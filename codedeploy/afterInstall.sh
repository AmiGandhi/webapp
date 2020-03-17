#!/bin/bash
#Commands to run after after installation
echo "Entered after install hook"

sudo systemctl stop tomcat.service
echo "Application stopped successfully"

sudo rm -rf /opt/tomcat/webapps/docs  /opt/tomcat/webapps/examples /opt/tomcat/webapps/host-manager  /opt/tomcat/webapps/manager /opt/tomcat/webapps/ROOT
sudo chown tomcat:tomcat /opt/tomcat/webapps/ROOT.war

#Killing the application
kill -9 $(ps -ef|grep ROOT | grep -v grep | awk '{print $2}')

#Removing log files
sudo rm -rf /opt/tomcat/logs/catalina*
sudo rm -rf /opt/tomcat/logs/*.log
sudo rm -rf /opt/tomcat/logs/*.txt

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/AmazonCloudWatch-config.json \
    -s
echo "After install hook completed successfully"
