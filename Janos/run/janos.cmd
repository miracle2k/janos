REM Copyright David Wheeler 2008

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
REM Usage: Janos.cmd


echo Starting Janos


set CLASSPATH=Janos.jar
set CLASSPATH=%CLASSPATH%;lib/resources
set CLASSPATH=%CLASSPATH%;lib/commons-jxpath-1.1.jar
set CLASSPATH=%CLASSPATH%;lib/commons-lang-2.3.jar
set CLASSPATH=%CLASSPATH%;lib/commons-logging-api.jar
set CLASSPATH=%CLASSPATH%;lib/commons-logging.jar
set CLASSPATH=%CLASSPATH%;lib/joda-time-1.5.2.jar
set CLASSPATH=%CLASSPATH%;lib/sbbi-upnplib-1.0.4.jar
set CLASSPATH=%CLASSPATH%;lib/swt.jar
set CLASSPATH=%CLASSPATH%;lib/mx4j-impl.jar
set CLASSPATH=%CLASSPATH%;lib/mx4j-jmx.jar
set CLASSPATH=%CLASSPATH%;lib/mx4j-remote.jar
set CLASSPATH=%CLASSPATH%;lib/mx4j-tools.jar
set CLASSPATH=%CLASSPATH%;lib/sbbi-jmx-1.0.jar

set PATH=%PATH%;./lib

java -cp %CLASSPATH% net.sf.janos.Janos
