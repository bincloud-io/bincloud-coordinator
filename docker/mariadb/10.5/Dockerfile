FROM bincloud/linux:ubuntu-20.04
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

# INSTALL MARIADB
RUN apt-key adv --recv-keys --keyserver hkp://keyserver.ubuntu.com:80 0xF1656F24C74CD1D8
RUN add-apt-repository 'deb [arch=amd64,arm64,ppc64el] http://mirror.lstn.net/mariadb/repo/10.5/ubuntu focal main'
RUN apt update -y
RUN apt install mariadb-server -y

# OVERRIDE DEFAULTS
RUN mv /etc/mysql/my.cnf /etc/mysql/my.default.cnf
COPY mariadb-defaults.cnf /etc/mysql/my.cnf
RUN echo "!include /etc/mysql/my.default.cnf" >> my.cnf

# CLEANUP DIRECTORIES
RUN rm -rf /tmp/*
RUN rm -rf  /var/tmp/*
RUN rm -rf /var/lib/mysql
RUN rm -rf /var/lib/apt/lists/*
RUN mkdir -p ./mariadb/schema ./mariadb/init.sql /var/run/mysqld

# ADD SCRIPTS
COPY start_mariadb.sh ./mariadb/start_mariadb.sh

# SET PERMISSIONS
RUN chown -R bincloud:bincloud ./mariadb /var/run/mysqld
RUN chmod -R 777 ./mariadb /var/run/mysqld

# INSTALL DATABASE
RUN mariadb-install-db --user=bincloud

USER bincloud

VOLUME /srv/bincloud/mariadb
EXPOSE 3306

CMD ["/srv/bincloud/mariadb/start_mariadb.sh"]