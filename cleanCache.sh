#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh
}

clean_caches() {

  logw "[$filename]clean android studio cache!"
  dir=("appdemo" "casedemo" "dexdemo" "kotlindemo" "dev-sdk" "buildSrc" "TestCaseBase/app" "TestCaseBase/TestCaseLib")

  for element in "${dir[@]}"; do
    #clean task
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.settings/
    rm -rf $element/.externalNativeBuild
    rm -rf $element/$element.iml
    rm -rf $element/.gradle
    rm -rf $element/.DS_Store
    rm -rf $element/__MACOSX
    logd "[$filename]clean $element over."
  done

  base_dir=("." "TestCaseBase")
  for element in "${base_dir[@]}"; do
    rm -rf $element/build/
    rm -rf $element/release/
    rm -rf $element/releasebak/
    rm -rf $element/*.iml
    rm -rf $element/.gradle/
    rm -rf $element/.idea/
    rm -rf $element/sh.exe.stackdump
    rm -rf $element/classes.dex
    rm -rf $element/local.properties
    rm -rf $element/.vs/
    rm -rf $element/.vscode/
    rm -rf $element/.DS_Store
    rm -rf $element/__MACOSX
  done

  if [ $# == 0 ]; then
    logw "[$filename]clean project success."
    loge "[$filename]>>>>you must close android studio<<<<"
  else
    loge "[$filename]>>clean project Failed!<<"
  fi

}

main() {
  source_common
  clean_caches
}

main
