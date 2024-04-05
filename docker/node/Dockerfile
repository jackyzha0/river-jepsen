FROM jgoerzen/debian-base-standard:bookworm as debian-addons

#### FIXTURES ####
FROM oven/bun AS fixtures
COPY jepsen/src/fixtures /fixtures

# Install deps
WORKDIR /fixtures/typescript
RUN ls
RUN bun install
RUN bun run dump --silent > ../schema.json
RUN bun build server.ts --compile --outfile bun-server

#### Actual image ####
FROM debian:bookworm-slim

ENV container=docker
STOPSIGNAL SIGRTMIN+3

COPY --from=debian-addons /usr/local/preinit/ /usr/local/preinit/
COPY --from=debian-addons /usr/local/bin/ /usr/local/bin/
COPY --from=debian-addons /usr/local/debian-base-setup/ /usr/local/debian-base-setup/

RUN run-parts --exit-on-error --verbose /usr/local/debian-base-setup

# Basic system stuff
RUN apt-get -qy update && \
    apt-get -qy install \
        apt-transport-https

# Install packages
RUN apt-get -qy update && \
    apt-get -qy install \
        dos2unix openssh-server pwgen

# When run, boot-debian-base will call this script, which does final
# per-db-node setup stuff.
ADD setup-jepsen.sh /usr/local/preinit/03-setup-jepsen
RUN chmod +x /usr/local/preinit/03-setup-jepsen

# Configure SSHD
RUN sed -i "s/#PermitRootLogin prohibit-password/PermitRootLogin yes/g" /etc/ssh/sshd_config

# Enable SSH server
ENV DEBBASE_SSH enabled

# Install Jepsen deps
RUN apt-get -qy update && \
    apt-get -qy install \
        build-essential bzip2 ca-certificates curl dirmngr dnsutils faketime iproute2 iptables iputils-ping libzip4 logrotate lsb-release man man-db netcat-openbsd net-tools ntpdate psmisc python3 rsyslog sudo tar tcpdump unzip vim wget

EXPOSE 22

WORKDIR /fixtures
COPY --from=fixtures /fixtures/typescript/bun-server /root/bun-server

CMD ["/usr/local/bin/boot-debian-base"]