#!/bin/bash
WorkDir=`dirname "$0"`
export CLASSPATH=$CLASSPATH:"$WorkDir"
java -jar "$WorkDir"/JInvaders.jar &

