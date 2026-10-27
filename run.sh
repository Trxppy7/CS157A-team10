#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
ROOT="$PWD"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 25)}"
export TOMCAT_HOME="${TOMCAT_HOME:-/opt/homebrew/opt/tomcat/libexec}"
export PAWMATCH_CONFIG="${PAWMATCH_CONFIG:-$ROOT/.env}"
PORT="${PORT:-8081}"
case "$PORT" in ''|*[!0-9]*) echo 'PORT must be a number.' >&2; exit 1;; esac
if [[ ! -f "$PAWMATCH_CONFIG" && -z "${DB_PASSWORD+x}" ]]; then
  echo 'Copy .env.example to .env and enter your local MySQL settings first.' >&2
  exit 1
fi
if command -v lsof >/dev/null && lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null; then
  echo "Port $PORT is already in use. Stop that server or choose another PORT before running again." >&2
  exit 1
fi
./build.sh
export CATALINA_HOME="$TOMCAT_HOME"
export CATALINA_BASE="$ROOT/.local/tomcat"
mkdir -p "$CATALINA_BASE"/{conf,logs,temp,webapps,work}
cp "$TOMCAT_HOME"/conf/{web.xml,catalina.properties,logging.properties} "$CATALINA_BASE/conf/"
cat > "$CATALINA_BASE/conf/server.xml" <<EOF
<Server port="-1">
  <Service name="Catalina">
    <Connector address="127.0.0.1" port="$PORT" protocol="HTTP/1.1" />
    <Engine name="Catalina" defaultHost="localhost">
      <Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true" />
    </Engine>
  </Service>
</Server>
EOF
cp build/pawmatch.war "$CATALINA_BASE/webapps/CS157A-team10.war"
echo "Starting http://localhost:$PORT/CS157A-team10/ (Ctrl+C stops it)"
exec "$TOMCAT_HOME/bin/catalina.sh" run
