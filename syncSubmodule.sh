#!/usr/bin/env bash

mode_up() {
  chmod -R 777 *
  git config core.filemode false
}
source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh md
  #    bash $pwd/common.sh mdout
}

update_submodule() {
  git submodule update
}
main() {
  mode_up
  source_common
  update_submodule
}

main
