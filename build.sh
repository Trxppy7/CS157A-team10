#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 25)}"
TOMCAT_HOME="${TOMCAT_HOME:-/opt/homebrew/opt/tomcat/libexec}"
if [[ ! -f "$TOMCAT_HOME/lib/servlet-api.jar" ]]; then
  echo 'Set TOMCAT_HOME to your Tomcat 11 installation.' >&2
  exit 1
fi
if [[ "$("$JAVA_HOME/bin/javac" -version)" != javac\ 25* ]]; then
  echo 'Set JAVA_HOME to a JDK 25 installation.' >&2
  exit 1
fi
if ! compgen -G 'src/main/webapp/WEB-INF/lib/mysql-connector-*.jar' > /dev/null; then
  echo 'Copy MySQL Connector/J into src/main/webapp/WEB-INF/lib first.' >&2
  exit 1
fi
for dependency in jakarta.mail-api angus-mail jakarta.activation-api angus-activation; do
  if ! compgen -G "src/main/webapp/WEB-INF/lib/$dependency-*.jar" > /dev/null; then
    echo "Copy $dependency into src/main/webapp/WEB-INF/lib; see README.md." >&2
    exit 1
  fi
done
rm -rf build/classes
mkdir -p build/classes
find src/main/java -name '*.java' > build/sources.txt
"$JAVA_HOME/bin/javac" --release 25 -encoding UTF-8 -cp "$TOMCAT_HOME/lib/servlet-api.jar:src/main/webapp/WEB-INF/lib/*" -d build/classes @build/sources.txt
rm -rf build/webapp
mkdir -p build/webapp/WEB-INF/classes/sql
cp -R src/main/webapp/. build/webapp/
cp -R build/classes/. build/webapp/WEB-INF/classes/
cp db/*.sql build/webapp/WEB-INF/classes/sql/
"$JAVA_HOME/bin/jar" --create --file build/pawmatch.war -C build/webapp .
echo 'Built build/pawmatch.war'
