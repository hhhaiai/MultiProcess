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

push_submodule() {
  cd TestCaseBase/
  git remote set-url origin https://github.com/hhhaiai/TestCaseBase.git
#  git remote -v
  git push
  git remote set-url origin https://github.com.cnpmjs.org/hhhaiai/TestCaseBase.git
}
main() {
  mode_up
  source_common
  push_submodule
}

main
