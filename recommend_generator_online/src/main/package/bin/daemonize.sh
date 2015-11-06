#!/bin/bash
#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)

source ${TBINDIR}/config.sh

nohup ${PROG} ${OPTS} >> ${STDOUT} 2>&1 &
SUBPID=$!

[ -n ${PIDFILE} -a ! -f ${PIDFILE} ] && echo $SUBPID > ${PIDFILE}
