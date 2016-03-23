#!/bin/bash
######################
# TODO
# clear logs

PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)
source ${TBINDIR}/config.sh

###
# Warning:
#   DELELE files in logs-dir last-modified 7 days ago
find ${TBASEDIR}/logs/ -type f -mtime 7 | while read logfile tail
do
  echo "clearing $logfile..."
  rm ${logfile}
done
