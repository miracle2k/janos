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

CLASSPATH=Janos.jar
CLASSPATH=$CLASSPATH:lib/resources
CLASSPATH=$CLASSPATH:lib/commons-jxpath-1.1.jar
CLASSPATH=$CLASSPATH:lib/commons-lang-2.3.jar
CLASSPATH=$CLASSPATH:lib/commons-logging-api.jar
CLASSPATH=$CLASSPATH:lib/commons-logging.jar
CLASSPATH=$CLASSPATH:lib/joda-time-1.5.2.jar
CLASSPATH=$CLASSPATH:lib/sbbi-upnplib-1.0.4.jar
CLASSPATH=$CLASSPATH:lib/swt.jar
CLASSPATH=$CLASSPATH:lib/lib/mx4j-impl.jar
CLASSPATH=$CLASSPATH:lib/mx4j-jmx.jar
CLASSPATH=$CLASSPATH:lib/mx4j-remote.jar
CLASSPATH=$CLASSPATH:lib/lib/mx4j-tools.jar
CLASSPATH=$CLASSPATH:lib/sbbi-jmx-1.0.jar
PATH=$PATH:./lib
echo PATH=$PATH

java -cp $CLASSPATH net.sf.janos.Janos -Djava.library.path=$PATH
