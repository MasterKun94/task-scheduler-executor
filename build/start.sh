#!/usr/bin/env bash
path=$(cd `dirname $0`; pwd)
echo ${path}
classpaths="${path}/lib/*"
cps=`echo $classpaths | sed 's/[ ][ ]*/:/g'`
echo "${cps}"
cd ${path}
java -javaagent:${path}/lib/sigar-loader-1.6.6-rev002.jar -cp ${cps}:${path}/conf/logback.xml com.oceanum.ClusterStarter --base-path="$path" $@
