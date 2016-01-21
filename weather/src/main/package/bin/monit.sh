#!/bin/bash
#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)
source ${TBINDIR}/functions
source ${TBINDIR}/config.sh

monit_file="$HOME/.monit/${MONIT_NAME}.conf"
eth0_ip=$(/sbin/ifconfig eth0 | /bin/grep 'inet addr:' | /bin/awk '{print $2}' | /bin/sed -e 's/addr://g')

function usage() {
    echo "Usage:"
    echo "    monit.sh on|off|gen|print|create"
}

function gen_monit() {
    echo "check process ${MONIT_NAME} with pidfile ${PIDFILE}"
    echo "    start program = \"${TBINDIR}/start.sh\" as uid $USER and gid services"
    echo "    stop  program = \"${TBINDIR}/stop.sh\"  as uid $USER and gid services"
    echo "    if not exist then restart"
}

function print_monit() {
    test -f ${monit_file}
    if [ $? -ne 0 ]; then
      echo "${monit_file} not existes!"
      return 1
    fi
    cat ${monit_file}
}

function create_monit() {
    test -f ${monit_file}
    if [ $? -eq 0 ]; then
        if [ "x$1" != 'force' ];then
        echo "${monit_file} already exists!"
        return 1
        fi
    fi
    mkdir $HOME/.monit > /dev/null 2>&1
    gen_monit > ${monit_file}
}

function status() {
    sudo /usr/bin/monit status | sed -rn "/^Process '${MONIT_NAME}'/,/^$/p"
}

function start() {
    echo "Trying to Turn monit on ..."
    if [ ! -f ${monit_file} ]; then
		create_monit
        sudo /usr/bin/monit reload
	fi
    sudo /usr/bin/monit monitor "${MONIT_NAME}"
    retval=$?
    [ "$retval" -eq 0 ] && success $"monit start" || failure $"monit start"
    status
}

function stop() {
    echo "Trying to Turn monit off ..."
    sudo /usr/bin/monit unmonitor "${MONIT_NAME}"
    retval=$?
    [ "$retval" -eq 0 ] && success $"monit stop" || failure $"monit stop"
    status
}

case $1 in
    on|ON|On|start|Start|START)
        start
        ;;
    off|OFF|Off|stop|Stop|STOP)
        stop
        ;;
    gen)
        gen_monit
        ;;
	reload|Reload|RELOAD)
		sudo /usr/bin/monit reload
		;;
    print|PRINT|Print)
        print_monit
        retval=$?
        ;;
    create|CREATE|Create)
        create_monit
        retval=$?
        ;;
    status)
        status
        ;;
    *)
        usage
        ;;
esac

echo
exit $retval
