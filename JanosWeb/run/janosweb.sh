#!/bin/sh
# Linux script for starting Janos 0.1
#
# Copyright David Wheeler 2008
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#
# Usage: ./Janos.sh

echo Starting Janos...

ROOTDIR=`dirname "$0"`
echo ROOTDIR=$ROOTDIR

CLASSPATH=$ROOTDIR/lib/SonosJ.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/JanosController.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/resources
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/commons-jxpath-1.1.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/commons-lang-2.3.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/commons-logging-api.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/commons-logging.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/joda-time-1.5.2.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/sbbi-upnplib-1.0.4.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/swt.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/lib/mx4j-impl.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/mx4j-jmx.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/mx4j-remote.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/mx4j-tools.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/mx4j-impl.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/lib/mx4j-tools.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/sbbi-jmx-1.0.jar
CLASSPATH=$CLASSPATH:$ROOTDIR/lib/log4j-1.2.15.jar
PATH=$PATH:$ROOTDIR/lib
echo PATH=$PATH

ARGS=2000
JVMARGS=
java -cp $CLASSPATH -Djava.library.path=$PATH $JVMARGS net.sf.janos.Janos $ARGS
