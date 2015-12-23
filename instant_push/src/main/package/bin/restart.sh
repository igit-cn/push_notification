#!/bin/bash
#######################
# Template config
# DO NOT modify
PATH=".:$PATH"
TBINDIR=$(dirname $(which $0))
TBASEDIR=$(dirname $TBINDIR)

$TBINDIR/stop.sh
$TBINDIR/start.sh
