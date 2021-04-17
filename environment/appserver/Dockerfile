FROM bincloud/wildfly:23.0.0.Final
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

COPY ./init.server /srv/bincloud/wildfly/init.server

ENV DEPLOYMENT_MARIADB_JDBC_CLIENT_URL https://repo1.maven.org/maven2/org/mariadb/jdbc/mariadb-java-client/2.7.2/mariadb-java-client-2.7.2.jar

ENV DB_CENTRAL_JNDI_NAME  java:/jdbc/BC_CENTRAL
ENV DB_CENTRAL_CONNECTION_URL jdbc:mariadb://central.bincloud.internal/bc_central
ENV DB_CENTRAL_USERNAME bincloud
ENV DB_CENTRAL_PASSWORD bincloud