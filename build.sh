#!/bin/sh
set -e

mkdir -p build/libs

javac -d build src/Main.java

jar cfe build/libs/ssl-diagnostic-tool.jar Main -C build Main.class

echo "Build complete: build/libs/ssl-diagnostic-tool.jar"
