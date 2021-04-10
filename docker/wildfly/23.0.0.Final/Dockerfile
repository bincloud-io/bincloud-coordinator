FROM bincloud/java:openjdk-8-jre
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

# INSTALL WILDFLY
RUN curl https://download.jboss.org/wildfly/23.0.0.Final/wildfly-23.0.0.Final.tar.gz -o wildfly-server.tar.gz
RUN tar -xvf wildfly-server.tar.gz
RUN rm wildfly-server.tar.gz
RUN mv wildfly-23.0.0.Final wildfly
RUN mkdir /srv/bincloud/wildfly/init.server
RUN chown -R bincloud:bincloud /srv/bincloud/wildfly
RUN chmod -R 777 /srv/bincloud/wildfly
ENV JBOSS_HOME=/srv/bincloud/wildfly
ENV PATH=${JBOSS_HOME}/bin:${PATH}

# ADD NECESSARY FILES AND SCRIPTS
ADD start_wildfly.sh /srv/bincloud/wildfly/start_wildfly.sh
RUN chown bincloud:bincloud /srv/bincloud/wildfly/start_wildfly.sh
RUN chmod 777 /srv/bincloud/wildfly/start_wildfly.sh

USER bincloud

# ADD USER
RUN add-user.sh -u 'bincloud' -p 'bincloud'

VOLUME /srv/bincloud/wildfly
EXPOSE 8080
EXPOSE 9990

CMD ["/srv/bincloud/wildfly/start_wildfly.sh"]
