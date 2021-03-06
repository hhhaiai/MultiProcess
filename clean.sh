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
  dir=("appdemo" "casedemo" "dexdemo" "kotlindemo" "dev-sdk" "buildSrc")
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

  rm -rf build/
  rm -rf release/
  rm -rf releasebak/
  rm -rf sh.exe.stackdump
  rm -rf classes.dex
  rm -rf .vs/
  rm -rf .vscode/
  rm -rf .DS_Store
  rm -rf __MACOSX

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
