#!/bin/sh

java -Dinternal.truststore.path="${TRUSTSTORE_PATH}" \
     -Dinternal.truststore.password="${TRUSTSTORE_PASSWORD}"   \
     -jar build/libs/ssl-diagnostic-tool.jar "$@"
