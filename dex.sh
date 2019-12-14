#!/bin/sh

./gradlew build
# need delay .wait for build over
cd ./app/build/intermediates/classes/debug/

 dx --dex --output=temp.dex com/device/impls/Test.class
jar cvf temp.jar temp.dex
mv temp.jar ~/Desktop/temp_003.jar