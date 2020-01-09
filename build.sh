#!/bin/bash


dir=("app" "dev-sdk" "buildSrc" "dex")

# clean  cache
function clean()
{
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
}

# gradlew build
function build_gradlew()
{
    ./gradlew zip
}

# gradle build
function build_gradle()
{
    gradle zip
}


: '
    编译程序的入口
'
red='\033[31m'
green='\033[32m'
yellow='\033[33m'
end='\033[0m'

# 1. 清除缓存
clean

# 2. 编译并处理异常情况
build_gradlew

if  [ $# == 0 ]; then
    echo "${green}gradlew build success${end}"
    pwd=$(pwd)
    echo "${green}gradlew build jar and zip success. path: $(pwd)/release/${end}"
else
    echo "${red}gradlew build failed${end}"
    build_gradle
    if  [ $# == 0 ]; then
        echo "${green}gradle build success${end}"
        pwd=$(pwd)
        echo "${green}gradle build jar and zip success. path: $(pwd)/release/${end}"
    fi
fi
