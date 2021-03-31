#!/bin/bash

WILDFLY_HOME=/opt/jboss/wildfly
JBOSS_CLI=$WILDFLY_HOME/bin/jboss-cli.sh

echo $(date -u) "=> Creating a new JMS queue named \"java:/jms/queue/BanQ\""
$JBOSS_CLI -c "jms-queue add --queue-address=BanQ --entries=java:/jms/queue/BanQ"

echo $(date -u) "=> Deploy application"
$JBOSS_CLI -c "deploy wildfly/customization/target/bank-batch.war"

echo $(date -u) "=> Create user"
$WILDFLY_HOME/bin/add-user.sh admin admin
