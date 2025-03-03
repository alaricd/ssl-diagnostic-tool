#!/bin/sh

java -Dexpd.internal.truststore.path="${TRUSTSTORE_PATH}" \
     -Dexpd.internal.truststore.password="${TRUSTSTORE_PASSWORD}"   \
     -jar build/libs/ssl-diagnostic-tool.jar "$@"
