#!/usr/bin/env bash
path=$(cd `dirname $0`; pwd)
echo ${path}
classpaths="${path}/lib/*"
cps=`echo $classpaths | sed 's/[ ][ ]*/:/g'`
echo "${cps}"
java -cp ${cps} com.oceanum.ClusterStarter --base-path="$path" $@
