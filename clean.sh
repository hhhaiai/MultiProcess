#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh
}

clean_task() {
  logw "[$filename]>>>>clean project<<<<"
  dir=("appdemo" "casedemo" "dexdemo" "kotlindemo" "dev-sdk" "buildSrc" "TestCaseBase/app" "TestCaseBase/TestCaseLib" "MultiProcess/app" "MultiProcess/MultiprocessLib" "MultiProcess/JavaGenreal")
  for element in "${dir[@]}"; do
    #clean task
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.externalNativeBuild
    rm -rf $element/.DS_Store
    rm -rf $element/__MACOSX
    logd "[$filename]clean $element over."
  done

  base_dir=("." "TestCaseBase" "MultiProcess")
  for element in "${base_dir[@]}"; do
    rm -rf $element/build/
    rm -rf $element/release/
    rm -rf $element/releasebak/
    rm -rf $element/sh.exe.stackdump
    rm -rf $element/classes.dex
    rm -rf $element/.vs/
    rm -rf $element/.vscode/
    rm -rf $element/.DS_Store
    rm -rf $element/__MACOSX
  done

  if [ $# == 0 ]; then
    logi "[$filename]clean project success. "
  else
    loge "[$filename]clean project Failed!"
  fi

}

main() {
  source_common
  clean_task
}

main
