#!/bin/bash

apt-get -y update && apt-get install -y software-properties-common

# Repository for Java
add-apt-repository -y ppa:webupd8team/java

apt-get -y update && DEBIAN_FRONTEND=noninteractive apt-get install -qq -y \
    openssh-server \
    sssd \
    libpam-sss \
    libnss-sss \
    libnss-ldap \
    supervisor \
    gettext

# Installing Java
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
apt-get install -y oracle-java8-installer

# Disable message of the day by commenting out configuration lines refering to pam_motd.so
sed -e '/.*pam_motd\.so.*/ s/^#*/#/' -i /etc/pam.d/sshd
sed -e '/.*pam_motd\.so.*/ s/^#*/#/' -i /etc/pam.d/login
sed -e '/PrintMotd / s/^#*/#/' -i /etc/ssh/sshd_config
sed -e '/PrintLastLog / s/^#*/#/' -i /etc/ssh/sshd_config

# Get authorized keys from LDAP, disable message of the day and last log printout, disable options for speeding up access
printf '%s\n' \
    'AuthorizedKeysCommand /usr/bin/sss_ssh_authorizedkeys' \
    'AuthorizedKeysCommandUser root' \
    'PrintMotd no' \
    'PrintLastLog no' \
    'UseDNS no' \
    'GSSAPIAuthentication no' \
    >> /etc/ssh/sshd_config

# Create ssh deamon folder
mkdir /var/run/sshd