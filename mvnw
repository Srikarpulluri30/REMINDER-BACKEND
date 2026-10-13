#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper Startup Batch script for UNIX/Linux/macOS
# ----------------------------------------------------------------------------

set -e

# OS specific support.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true ;;
  MINGW*) mingw=true ;;
esac

## Resolve links - $0 may be a link
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done

saveddir=`pwd`

MAVEN_BASEDIR=`dirname "$PRG"`

# cd to the directory containing this script
cd "$MAVEN_BASEDIR"

# Set default values for wrapper properties
WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
WRAPPER_JAR=".mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROP=".mvn/wrapper/maven-wrapper.properties"

# Locate java
if [ -n "$JAVA_HOME" ] ; then
  if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
    JAVACMD="$JAVA_HOME/jre/sh/java"
  else
    JAVACMD="$JAVA_HOME/bin/java"
  fi
  if [ ! -x "$JAVACMD" ] ; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD="java"
  which java >/dev/null 2>&1 || {
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
  }
fi

# Download wrapper jar if not present
if [ ! -e "$WRAPPER_JAR" ]; then
    mkdir -p ".mvn/wrapper"
    if [ -x "$(which curl 2>/dev/null)" ]; then
        curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
    elif [ -x "$(which wget 2>/dev/null)" ]; then
        wget -q "$WRAPPER_URL" -O "$WRAPPER_JAR"
    else
        echo "ERROR: Neither curl nor wget was found to download maven wrapper jar." >&2
        exit 1
    fi
fi

# Execute maven wrapper
exec "$JAVACMD" \
  -jar "$WRAPPER_JAR" \
  "$@"
