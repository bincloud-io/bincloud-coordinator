FROM ubuntu:20.04
MAINTAINER Dmitry Mikhaylenko<dmikhaylenko@hotmail.com>

RUN apt -y update
RUN apt -y upgrade 
RUN apt install tar -y
RUN apt install curl -y
RUN apt install iputils-ping -y
RUN apt install ca-certificates -y
RUN apt install software-properties-common -y
RUN groupadd -r bincloud -g 1000
RUN useradd -u 1000 -r -g bincloud -m -d /srv/bincloud -s /sbin/nologin -c "Bincloud user" bincloud
RUN chmod 755 /srv/bincloud

WORKDIR /srv/bincloud