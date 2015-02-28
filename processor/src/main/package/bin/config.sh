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
PROG="/usr/local/jdk1.7/bin/java"

PROGNAME=$(basename ${PROG})
PROGBASE=${PROGNAME%%.*}

#######################
# runtime options
LIB_DIR=${TBASEDIR}/lib
CONFIG_DIR=${TBASEDIR}/conf
OPTS="-Xmx4G -Xms1G -server -XX:+UseCompressedOops -XX:+UseParNewGC -cp .:${LIB_DIR}:${CONFIG_DIR}:${LIB_DIR}/push-service-1.0-SNAPSHOT-jar-with-dependencies.jar com.yidian.push.server.Service ${CONFIG_DIR}/prod_config.json"
KILLPROC_OPTS=""

#######################
# monit config
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
