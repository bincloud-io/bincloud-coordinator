#!/bin/bash

# AGGREGATE ALL INITIAL SQL SCRIPTS TO A SINGLE FILE
echo -n > mariadb/init_database.sql
cat $(echo "$(file $(find mariadb/init.sql) | grep -v -w directory)" | cut -d: -f1 | tr '[:upper:]' '[:lower:]' | sort) > mariadb/init_database.sql
rm -rf ./init.sql/*

# RUN MYSQL DAEMON
mysqld --user=bincloud --init_file=/srv/bincloud/mariadb/init_database.sql
