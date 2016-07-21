#!/bin/bash
# ==============================================================================
# Run PdfMetaModifier in a command line.
# ==============================================================================

JAR_FILE=pmm.jar
JVM_OPT="-Xms64m -Xmx512m"

SCRIPT_PATH=`readlink -f $0`
DIR_PATH=`dirname $SCRIPT_PATH`
SCRIPT_PARAMS=$@

java $JVM_OPT -jar "$DIR_PATH/$JAR_FILE" $SCRIPT_PARAMS

