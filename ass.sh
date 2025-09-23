#!/bin/sh

cd /home/thomas/dev/alephium || exit

sbt app/assembly

cd /home/thomas/dev/alephium/app/target/scala-2.13/ || exit

echo "Uziping"
unzip -l alephium-app.jar | grep akka

echo "Building Native image"
docker run --rm \
  -v "$PWD":/app -w /app \
  ghcr.io/graalvm/native-image-community:24 \
  native-image \
    --no-fallback \
    -H:IncludeResources='.*\.conf$,.*\.conf\.tmpl$' \
    -H:+ReportExceptionStackTraces \
    --initialize-at-build-time=org.apache.logging.log4j,org.apache.logging.slf4j \
    --initialize-at-run-time=org.apache.logging.log4j.core.util.internal.UnsafeUtil,io.netty,io.vertx.core.http,io.vertx.core.net \
    -jar  alephium-app.jar


sudo chown thomas:users alephium-app
rm /home/thomas/.alephium/alephium-app
sudo mv alephium-app /home/thomas/.alephium

