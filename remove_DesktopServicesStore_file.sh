#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh
}

clean() {
  tempfiles=(".DS_Store" "__MACOSX")
  for element in "${tempfiles[@]}"; do
    # clean sub dir
    find . -name '$element' -delete
    logi "清除 $element 完毕"
  done
}

main() {
  source_common
  clean
}

main
