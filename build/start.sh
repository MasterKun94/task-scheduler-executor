#!/usr/bin/env bash
path=$(cd `dirname $0`; pwd)
echo ${path}
classpaths="${path}/lib/*"
cps=`echo $classpaths | sed 's/[ ][ ]*/:/g'`
scala -cp ${cps} com.oceanum.ClusterStarter --port=3551 --topics=test
