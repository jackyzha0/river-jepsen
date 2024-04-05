FROM jgoerzen/debian-base-standard:bookworm as debian-addons
FROM debian:bookworm-slim

COPY --from=debian-addons /usr/local/preinit/ /usr/local/preinit/
COPY --from=debian-addons /usr/local/bin/ /usr/local/bin/
COPY --from=debian-addons /usr/local/debian-base-setup/ /usr/local/debian-base-setup/

RUN run-parts --exit-on-error --verbose /usr/local/debian-base-setup

ENV container=docker
STOPSIGNAL SIGRTMIN+3

ENV LEIN_ROOT true

# JDK21 only in Debian testing
RUN echo "deb http://deb.debian.org/debian testing main" >> /etc/apt/sources.list
ADD ./control/apt-preferences /etc/apt/preferences

#
# Jepsen dependencies
#
RUN apt-get -qy update && \
    apt-get -qy install \
        curl dos2unix emacs git gnuplot graphviz htop iputils-ping libjna-java openjdk-21-jdk-headless pssh screen vim wget

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein && \
    mv lein /usr/bin && \
    chmod +x /usr/bin/lein && \
    lein self-install

COPY ./jepsen/project.clj /jepsen/project.clj
RUN cd /jepsen && lein install

ADD ./control/bashrc /root/.bashrc
ADD ./control/init.sh /init.sh
RUN dos2unix /init.sh /root/.bashrc \
    && chmod +x /init.sh

CMD /init.sh
