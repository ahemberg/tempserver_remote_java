#!/bin/bash
chmod u+x remote.app
cp tempserver-remote.service /etc/systemd/system/

systemctl daemon-reload
systemctl enable tempserver-remote.service
systemctl start tempserver-remote
systemctl status tempserver-remote