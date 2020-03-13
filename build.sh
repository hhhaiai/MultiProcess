#!/usr/bin/env bash


source_common()
{
    pwd=$(cd `dirname $0`; pwd)
    source $pwd/common.sh
}

# clean cache
clean()
{
    dir=("app" "dev-sdk" "buildSrc" "dex")
    for element in ${dir[@]}
    do
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

mdout_build()
{
    logw "======================================="
    logw "========  will general doc ============"
    logw "======================================="
    $mdout init
    if  [ $# == 0 ]; then
        logi "general init success. "
        $mdout doc/流量审核SDK.md
    fi
}


build_check()
{
    logw "======================================="
    logw "========  will begin build============"
    logw "======================================="
    $gw zip
    if  [ $# == 0 ]; then
        logi "gradlew build jar and zip success. path: $ipwd/release/"
    else
        loge "gradlew build failed"
    fi
}


main()
{
    source_common

    # 1. 清除缓存
    clean
    # 2. 文档处理
    mdout_build
    # 3. 编译并处理异常情况
    build_check

}

: '
    编译程序的入口
'
main
