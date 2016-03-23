#!/bin/bash
# -*- coding:utf-8 -*-

#######################
# Template config
# DO NOT modify
PATH=".:$PATH" 
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)
TBASENAME=$(basename $TBASEDIR)

#######################
# programe name
# Fullpath is required
PROG="/usr/local/jdk1.8/bin/java"

PROGNAME=$(basename ${PROG})
PROGBASE=${PROGNAME%%.*}

#######################
#get all jars under lib
function get_all_jars()
{
    local path=$1
    local res=""
    for file in $(ls ${path}/*.jar)
    do
        if [ "${res}"x == ""x ]; then
            res=${file}
        else
            res="${res}:${file}"
        fi
    done
    echo ${res}
    return 0
}
#
# runtime options
LIB_DIR=${TBASEDIR}/lib
CONFIG_DIR=${TBASEDIR}/conf
LIB_DIR_JARS=$(get_all_jars ${LIB_DIR})
#JAVA_FLAGS="-Dhttp.proxyHost=proxy1.yidian.com -Dhttp.proxyPort=3128"
JAVA_FLAGS="\
-Dcom.sun.management.jmxremote=true \
-Dcom.sun.management.jmxremote.port=9090 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dfile.encoding=UTF-8 \
"
OPTS="${JAVA_FLAGS} -Xmx12G -Xms4G -server -XX:+UseCompressedOops -XX:+UseParNewGC -cp .:${LIB_DIR}:${CONFIG_DIR}:${LIB_DIR_JARS} com.yidian.push.client_pull.services.Service ${CONFIG_DIR}/prod_config.json"
KILLPROC_OPTS=""

#######################
# monit config
MONIROT_NAME=${TBASENAME//\//_}
MONIT_NAME=${MONIT_NAME-${TBASENAME}}

#######################
#
DAEMONIZE=yes

#######################
# pidfile absolute path
# Default: 
#	${TBASEDIR}/var/${PROGBASE}.pid
PIDFILE=${PIDFILE:-${TBASEDIR}/var/${TBASENAME}.pid}

#######################
# nohup stdout file
# set STDOUT=/dev/null if no need
# Default:
#	${TBASEDIR}/logs/${PROGBASE}.stdout.log
STDOUT=${STDOUT:-${TBASEDIR}/logs/${PROGBASE}.stdout.log.`date +%Y%m%d`}

#######################
# ulimit -c 
# Default:
#	0
DAEMON_COREFILE_LIMIT=${DAEMON_COREFILE_LIMIT:-0}

TIMEWAIT=${TIMEWAIT:-10}

#######################
# use nohup to daemonize
# if PROG daemonized by itself, set START_COMMAND="${PROG} ${OPTS} > /dev/null 2>&1"
if [ ${DAEMONIZE} == 'yes' -o ${DAEMONIZE} == 'YES' -o ${DAEMONIZE} == 1 ];then 
  START_COMMAND="${TBINDIR}/daemonize.sh"
else
  START_COMMAND="${PROG} ${OPTS}"
fi
