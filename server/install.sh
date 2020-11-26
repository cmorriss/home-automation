CWD=`pwd`
if [ `whoami` -ne "root" ]
then
	echo "This script must be run as root."
	exit 1
fi
cp $CWD/etc/init.d/home-automation-server /etc/init.d/home-automation-server
$CWD/update.sh
systemctl daemon-reload
systemctl enable home-automation-server
