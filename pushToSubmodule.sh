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
  source $pwd/common.sh
  #    bash $pwd/common.sh mdout
}

push_submodule() {
  cd TestCaseBase/
  # git remote -v
  git push
}
main() {
  mode_up
  source_common
  push_submodule
}

main
