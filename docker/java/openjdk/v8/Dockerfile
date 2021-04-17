FROM bincloud/linux:ubuntu-20.04
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

RUN apt install openjdk-8-jre -y
RUN ln -s $(readlink -f /usr/bin/java | sed "s:/bin/java::") ./java
ENV JAVA_HOME=/srv/bincloud/java
ENV PATH=${JAVA_HOME}/bin:${PATH}
