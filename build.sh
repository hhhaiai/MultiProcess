#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh md
  #    bash $pwd/common.sh mdout
}

# clean cache
clean() {
  dir=("appdemo" "casedemo" "dexdemo" "kotlindemo" "dev-sdk" "buildSrc")
  for element in "${dir[@]}"; do
    # clean sub dir
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.externalNativeBuild
    rm -rf $element/.DS_Store
    rm -rf $element/__MACOSX
  done
  # clean root dir
  rm -rf build/
  rm -rf release/
  rm -rf releasebak/
  rm -rf classes.dex
  rm -rf .DS_Store
  rm -rf __MACOSX
  #  find . -name '.DS_Store' -delete
}

mdout_build() {
  logw "======================================="
  logw "========  will general doc ============"
  logw "======================================="
  #    $mdout init
  if [ $# == 0 ]; then
    logi "general init success. "
    ${mdout} doc/流量审核SDK.md
  fi
}

build_check() {
  logw "======================================="
  logw "========  will begin build============"
  logw "======================================="
  $gw zip
  if [ $# == 0 ]; then
    logi "gradlew build jar and zip success. path: $ipwd/release/"
  else
    loge "gradlew build failed"
  fi
}
remove_temp_file() {
  rm -rf doc/*.html
  rm -rf doc/*.pdf
  rm -rf doc/*.tmp
}

main() {
  source_common
  clean
  mdout_build
  build_check
  remove_temp_file
}

main
