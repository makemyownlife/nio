#!/bin/sh
##
# Copyright 1999-2012 Alibaba Group.
#  
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#  
#      http://www.apache.org/licenses/LICENSE-2.0
#  
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##

#set JAVA_HOME
#JAVA_HOME=/usr/alibaba/java

#check JAVA_HOME & java
noJavaHome=false
if [ -z "$JAVA_HOME" ] ; then
    noJavaHome=true
fi
if [ ! -e "$JAVA_HOME/bin/java" ] ; then
    noJavaHome=true
fi
if $noJavaHome ; then
    echo
    echo "Error: JAVA_HOME environment variable is not set."
    echo
    exit 1
fi
#==============================================================================

#set JAVA_OPTS
JAVA_OPTS="-Xss128k"
#==============================================================================

#stop Server
$JAVA_HOME/bin/jps |grep CobarStartup|awk -F ' ' '{print $1}'|while read line
do
  eval "kill -9 $line"
done
#==============================================================================

#set HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..
COBAR_HOME=`pwd`
cd $CURR_DIR
if [ -z "$COBAR_HOME" ] ; then
    echo
    echo "Error: COBAR_HOME environment variable is not defined correctly."
    echo
    exit 1
fi
#==============================================================================

#set CLASSPATH
COBAR_CLASSPATH="$COBAR_HOME/conf:$COBAR_HOME/lib/classes"
for i in "$COBAR_HOME"/lib/*.jar
do
    COBAR_CLASSPATH="$COBAR_CLASSPATH:$i"
done
#==============================================================================

#shutdown Server
RUN_CMD="\"$JAVA_HOME/bin/java\""
RUN_CMD="$RUN_CMD -Dcobar.home=\"$COBAR_HOME\""
RUN_CMD="$RUN_CMD -classpath \"$COBAR_CLASSPATH\""
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD com.alibaba.cobar.CobarShutdown $@"
RUN_CMD="$RUN_CMD >> \"$COBAR_HOME/logs/console.log\" 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
#==============================================================================