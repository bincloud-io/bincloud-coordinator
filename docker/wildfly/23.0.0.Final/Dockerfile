FROM bincloud/java:openjdk-8-jre
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

# INSTALL WILDFLY
RUN curl https://download.jboss.org/wildfly/23.0.0.Final/wildfly-23.0.0.Final.tar.gz -o wildfly-server.tar.gz
RUN tar -xvf wildfly-server.tar.gz
RUN rm wildfly-server.tar.gz
RUN mv wildfly-23.0.0.Final wildfly
RUN chown -R bincloud:bincloud ./wildfly
RUN chmod -R 777 ./wildfly
ENV JBOSS_HOME=/srv/bincloud/wildfly
ENV PATH=${JBOSS_HOME}/bin:${PATH}

USER bincloud

# ADD USER
RUN add-user.sh -u 'bincloud' -p 'bincloud'

VOLUME /srv/bincloud/wildfly
EXPOSE 8080
EXPOSE 9990

CMD ["/srv/bincloud/wildfly/bin/standalone.sh", "--server-config=standalone-full.xml", "-b", "0.0.0.0"]
