#!/usr/bin/env bash
set -e

AmIRoot() {
  ! ((${EUID:-0} || $(id -u)))
}

if !(AmIRoot); then
  echo 'The installer must be run as root. Use sudo'
  exit 126
fi

PROGRAM_DIR=$1

if [[ !(-d ${PROGRAM_DIR}) ]]; then
  echo 'Could not find' ${PROGRAM_DIR} '. Exiting.'
  exit 1
fi

SERVICE_FILE=tempserver-remote.service
touch ${SERVICE_FILE}

echo "[Unit]" >>${SERVICE_FILE}
echo "Description=Tempremote Daemon" >>${SERVICE_FILE}
echo "After=network.target" >>${SERVICE_FILE}
echo "[Service]" >>${SERVICE_FILE}
echo "User=$USER" >>${SERVICE_FILE}
echo "ExecStart=/usr/bin/java -jar bin.jar -v" >>${SERVICE_FILE}
echo "WorkingDirectory=$PROGRAM_DIR" >>${SERVICE_FILE}
echo "Restart=always" >>${SERVICE_FILE}
echo "[Install]" >>${SERVICE_FILE}
echo "WantedBy=multi-user.target" >>${SERVICE_FILE}

cp ${SERVICE_FILE} /etc/systemd/system/
rm ${SERVICE_FILE}

systemctl daemon-reload
systemctl enable tempserver-remote.service
systemctl start tempserver-remote
