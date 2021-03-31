#!/bin/bash

WILDFLY_HOME=/opt/jboss/wildfly
JBOSS_CLI=$WILDFLY_HOME/bin/jboss-cli.sh

echo $(date -u) "=> Reading messages on \"java:/jms/queue/BanQ\" queue"
$JBOSS_CLI -c "jms-queue list-messages --queue-address=BanQ"
