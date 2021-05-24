#!/bin/bash

# AGGREGATE ALL INITIAL SQL SCRIPTS TO A SINGLE FILE
echo -n > .dbinit
cat $(echo "$(file $(find /srv/bincloud/mariadb/init.sql) | grep -v -w directory)" | cut -d: -f1 | tr '[:upper:]' '[:lower:]' | sort) > /srv/bincloud/.dbinit
rm -rf /srv/bincloud/mariadb/init.sql/*

# RUN MARIADB SERVER
mysqld --user=bincloud --init_file=/srv/bincloud/.dbinit
