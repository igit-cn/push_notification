#!/bin/bash
#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)

source ${TBINDIR}/functions
source ${TBINDIR}/config.sh

####### main ######

echo -n "Starting ${PROGNAME} ..."
if [ -n ${PIDFILE} -a -s ${PIDFILE} ]; then
   checkpid `cat ${PIDFILE}`
   RETVAL=$?
   [ "$RETVAL" -ne 0 ] && success $"$base startup" || failure $"$base startup"
   echo
   if [ $RETVAL -eq 0 ];then
     echo -e "$PROGNAME is already running (pid=`cat $PIDFILE`, pidfile=\"$PIDFILE\")" 
     exit 1
   else
     rm ${PIDFILE}
   fi
fi

cd ${TBASEDIR}
DAEMON_OPTS=""
[ -n ${PIDFILE} ] && DAEMON_OPTS="${DAEMON_OPTS} --pidfile=${PIDFILE}"
export PIDFILE=$PIDFILE
daemon ${DAEMON_OPTS} ${START_COMMAND} 
sleep 1
if [ -n ${PIDFILE} ]; then
  waitcnt=0
  while [ $waitcnt -lt $TIMEWAIT ]
  do
    ((waitcnt=$waitcnt+1))
    if [ -f ${PIDFILE} -a -s ${PIDFILE} ]; then
      checkpid `cat ${PIDFILE}`
      RETVAL=$?
      [ $RETVAL -eq 0 ] && break
    fi
    sleep 1
  done
else
  ps -ef | grep "$PROG" | grep -qv 'grep'
  RETVAL=$?
fi

[ "$RETVAL" -eq 0 ] && success $"$base startup" || failure $"$base startup"
echo
[ "$RETVAL" -eq 0 ] && echo -e "RETVAL\tpid:$(cat ${PIDFILE})"
RETVAL=$?
exit $RETVAL
