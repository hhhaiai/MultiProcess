#!/usr/bin/env bash

source_common() {
    pwd=$(
        cd $(dirname $0)
        pwd
    )
    source $pwd/common.sh
    ${mdout} install
}

# clean cache
clean() {
    dir=("app" "dev-sdk" "buildSrc" "dex")
    for element in ${dir[@]}; do
        # clean sub dir
        rm -rf $element/build/
        rm -rf $element/bin/
        rm -rf $element/gen/
        rm -rf $element/.externalNativeBuild
    done
    # clean root dir
    rm -rf build/
    rm -rf release/
    rm -rf releasebak/
    rm -rf classes.dex
}

mdout_build() {
    logw "======================================="
    logw "========  will general doc ============"
    logw "======================================="
    $mdout init
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
mode_up()
{
    chmod -R 777 *
    git config core.filemode false
}

main() {
    mode_up
    source_common
    clean
    mdout_build
    build_check
}

main
