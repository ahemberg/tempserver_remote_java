#!/usr/bin/env bash

AmIRoot()
{
    ! (( ${EUID:-0} || $(id -u) ))
}

if !(AmIRoot)
then
  echo 'The installer must be run as root. Use sudo'
  exit 126
fi

echo 'Will remove all installed files, including database of measurements not sent to server'
read -p 'Continue? [yN] ' YN;

if [[ ${YN} != "Y" && ${YN} != "y" ]] ; then
  echo 'Exiting'
  exit 0
fi

systemctl list-units --full --all | grep 'tempserver-remote.service';
if [[ $? == 0 ]] ; then
  echo 'Found service. WIll remove'
  if systemctl is-active --quiet tempserver-remote.service ; then
    echo "Service is running. will stop"
    systemctl stop tempserver-remote.service
  fi
  systemctl disable tempserver-remote.service
  rm /etc/systemd/system/tempserver-remote.service
  systemctl daemon-reload
  systemctl reset-failed
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
rm /usr/local/bin/measure-temp
rm -rf ${DIR}


