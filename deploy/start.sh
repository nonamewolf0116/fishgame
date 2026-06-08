#!/bin/bash
cd /opt/fishgame
exec java -Xms64m -Xmx128m -Xss256k \
  -XX:+UseSerialGC \
  -XX:MaxMetaspaceSize=96m \
  -XX:CompressedClassSpaceSize=24m \
  -XX:+UseStringDeduplication \
  -XX:MaxHeapFreeRatio=20 \
  -XX:MinHeapFreeRatio=10 \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.awt.headless=true \
  -jar /opt/fishgame/app.jar
