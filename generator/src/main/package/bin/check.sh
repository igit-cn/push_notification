#!/bin/bash
#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)

source ${TBINDIR}/functions
source ${TBINDIR}/config.sh

STATUS_OPTS=
[ -n ${PIDFILE} ] && STATUS_OPTS="${STATUS_OPTS} -p ${PIDFILE}"
status ${STATUS_OPTS} ${PROG}

RETVAL=$?
[ "$RETVAL" -eq 0 ] && success $"$base startup" || failure $"$base startup"
echo

exit $RETVAL
