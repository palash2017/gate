#!/bin/sh
#set -x
PRG="$0"
CURDIR="`pwd`"
# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
GATE_HOME=`dirname "$PRG"`/..
# make it fully qualified
# When CDPATH is set, the cd command prints out the dir name. Because of this
# wee need to execute the cd command separately, and only then get the value
# via `pwd`
cd "$GATE_HOME"
export GATE_HOME="`pwd`"
export ANT_HOME=$GATE_HOME
cd "$CURDIR"

## Process arguments: known arguments must come first, as soon as an 
## unknown argument is detected, processing ends and the rest of the
## arguments are passed on to ant run

config=
session=
initdir=
log4j=
while test "$1" != "";
do
  case "$1" in
  -h)
    cat <<EOF
Run GATE Developer
The following options can be passed immediately after the command name:
  -ld      ... create or use the GATE default configuration and session files 
               in the current directory
  -ln name ... create or use a config file name.xml and session file name.session 
               in the current directory
  -ll      ... if the current directory contains a file log4j.properties use
               this file to configure the logging
  -h       ... show this help
All other options will be passed on to the "ant run" command, for example:
  -propertyfile <file> ... use <file> instead of \$GATE_HOME/build.properties
  -Drun.java.io.tmpdir=<somedir>
Note that gate.sh does not use \$JAVA_OPTS. To adjust allocated memory use:
  -Druntime.start.memory=<memorysize>
  -Druntime.max.memory=<memorysize>
EOF
    #exec bin/ant -h
    exit 0
    ;;
  -ld)
    config="-Drun.gate.user.config=$CURDIR/.gate.xml"
    session="-Drun.gate.user.session=$CURDIR/.gate.session"
    initdir="-Drun.gate.user.filechooser.defaultdir=$CURDIR"
    shift
    ;;
  -ln)
    shift
    base=$1
    shift
    config="-Drun.gate.user.config=$CURDIR/$base.xml"
    session="-Drun.gate.user.session=$CURDIR/$base.session"
    initdir="-Drun.gate.user.filechooser.defaultdir=$CURDIR"
    ;;
  -ll)
    shift
    if [ -f "$CURDIR/log4j.properties" ]
    then
      log4j="-Drun.log4j.configuration=file://$CURDIR/log4j.properties"
    fi
    ;;
  *)
    break
    ;;
  esac
done


echo running: "$GATE_HOME/bin/ant" run -f "$GATE_HOME/build.xml"  "$config" "$sessioni" "$initdir" "$log4j" "$@"
exec "$GATE_HOME/bin/ant" run -f "$GATE_HOME/build.xml" "$config" "$session" "$initdir" "$log4j" "$@"

