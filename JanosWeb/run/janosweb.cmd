REM Copyright David Wheeler 2008 adapted by Chris Christiansen 2010

REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM
REM       http://www.apache.org/licenses/LICENSE-2.0
REM
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.
REM
REM Usage: janosweb.cmd


echo Starting JanosWeb - Webserver and Sonos controller


set CLASSPATH=lib/JanosWeb.jar
set CLASSPATH=%CLASSPATH%;lib/resources
set CLASSPATH=%CLASSPATH%;lib/JanosController.jar
set CLASSPATH=%CLASSPATH%;lib/commons-jxpath-1.1.jar
set CLASSPATH=%CLASSPATH%;lib/commons-lang-2.3.jar
set CLASSPATH=%CLASSPATH%;lib/commons-logging-api.jar
set CLASSPATH=%CLASSPATH%;lib/commons-logging.jar
set CLASSPATH=%CLASSPATH%;lib/joda-time-1.5.2.jar
set CLASSPATH=%CLASSPATH%;lib/sbbi-upnplib-1.0.4.jar
set CLASSPATH=%CLASSPATH%;lib/jsp.jar
set CLASSPATH=%CLASSPATH%;lib/jspengine.jar
set CLASSPATH=%CLASSPATH%;lib/log4j-1.2.15.jar
set CLASSPATH=%CLASSPATH%;lib/servlet-2-3.jar
set CLASSPATH=%CLASSPATH%;lib/war.jar
set CLASSPATH=%CLASSPATH%;lib/webserver.jar

set PATH=%PATH%;./lib
set ARGS=
set JVMARGS=

echo To terminate JanosWeb press CTRL+C
java -cp %CLASSPATH% %JVMARGS% net.sf.janos.web.JanosWeb %ARGS%
