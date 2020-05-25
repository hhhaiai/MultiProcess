#!/usr/bin/env bash

source_common() {
    pwd=$(
        cd $(dirname $0)
        pwd
    )
    source $pwd/common.sh md
}


buildAndRunUT(){
  $gw :dev-sdk:assembleAndroidTest
  $gw :app:assembleRelease
  java -jar tools/spoon/spoon.jar --test-apk ./dev-sdk/build/outputs/apk/androidTest/debug/dev-sdk-debug-androidTest.apk --apk ./app/build/outputs/apk/release/app-release.apk --output ./spoon/
}

mode_up()
{
    chmod -R 777 *
    git config core.filemode false
}

main() {
    mode_up
    source_common
    buildAndRunUT
}

main