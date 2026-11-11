#!/bin/sh
set -eu

if [ "${APP_STARTUP_DIAGNOSTICS_ENABLED:-false}" = "true" ]; then
  exec java -Xlog:gc*,safepoint:stdout:time,uptime,level,tags -jar /app/app.jar "$@"
fi

exec java -jar /app/app.jar "$@"
