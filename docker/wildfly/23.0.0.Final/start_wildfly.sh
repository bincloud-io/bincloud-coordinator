#!/bin/bash

# AGGREGATE ALL SERVER INIT SCRIPTS TO A SINGLE FILE
echo "embed-server --server-config=standalone-full.xml" > /srv/bincloud/.serverinit
printenv > /srv/bincloud/.serverenv
cat $(echo "$(file $(find /srv/bincloud/wildfly/init.server) | grep -v -w directory)" | cut -d: -f1 | tr '[:upper:]' '[:lower:]' | sort) >> /srv/bincloud/.serverinit
rm -rf /srv/bincloud/wildfly/init.server/*


# INITIALIZE SERVER BY SCRIPT
/bin/bash jboss-cli.sh --file=/srv/bincloud/.serverinit --properties=/srv/bincloud/.serverenv --resolve-parameter-values

# RUN WILDFLY SERVER IN STANDALONE MODE
standalone.sh --server-config=standalone-full.xml -b 0.0.0.0 -bmanagement 0.0.0.0