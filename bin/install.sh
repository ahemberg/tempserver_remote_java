#!/usr/bin/env bash
set -e

#TODO JSON to copy or create.

AmIRoot()
{
    ! (( ${EUID:-0} || $(id -u) ))
}

if !(AmIRoot)
then
  echo 'The installer must be run as root. Use sudo'
  exit 126
fi

DEFAULT_DIR=/opt/tempserver-remote
echo 'Default directory: ' ${DEFAULT_DIR}
read -p 'Will install to default directory. Specify another directory? [yN] ' YN;

if [[ ${YN} == "Y" || ${YN} == "y" ]] ; then
  echo 'Careful now, you are root...'
  read -p 'Directory for install:' INSTALL_DIR
else
  INSTALL_DIR=${DEFAULT_DIR}
fi

if [[ -d ${INSTALL_DIR} ]] ; then
  read -p 'The specified directory already exists, continue? [yN]? ' YN;
  if [[ ${YN} != "Y" && ${YN} != "y" ]] ; then
    echo 'Exiting'
    exit 0
  fi
else
  mkdir ${INSTALL_DIR}
fi

chmod a+rw ${INSTALL_DIR}

cp bin.jar ${INSTALL_DIR}/bin.jar
cp measure-temp.sh ${INSTALL_DIR}/measure-temp
cp uninstall.sh ${INSTALL_DIR}/uninstall
cp install_service.sh ${INSTALL_DIR}/install_service

chmod a+x ${INSTALL_DIR}/bin.jar
chmod a+x ${INSTALL_DIR}/measure-temp
chmod a+x ${INSTALL_DIR}/uninstall
chmod a+x ${INSTALL_DIR}/install_service

echo 'Program has been installed in' ${INSTALL_DIR} 'and can be run with' ${INSTALL_DIR} '/measure-temp'
read -p 'Create symink in /usr/local/bin? [Yn] ' YN;
if [[ ${YN} == "Y" || ${YN} == "y" || ${YN} = "" ]] ; then
  if ln -s ${INSTALL_DIR}/measure-temp /usr/local/bin/measure-temp; then
    echo 'Symlink created. Program can be run with "measure_temp"'
  else
    echo 'Failed to create symlink'
   fi
fi

#Todo -> testrun?

echo 'Program successfully installed. Would you like to create a service to run it automatically?'
echo 'If you answer no then you can create the service later by running' ${INSTALL_DIR} '/install_service'
read -p 'Create service? [yN] ' YN

if [[ ${YN} == "Y" || ${YN} == "y" ]] ; then
    source ${INSTALL_DIR}/install_service ${INSTALL_DIR}
fi