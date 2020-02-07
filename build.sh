#!/bin/bash


dir=("app" "dev-sdk" "buildSrc" "dex")

ecs="echo"
cygwnin="CYGWIN"
mingw="MINGW"
msys_nt="MSYS_NT"

# default use win
# 彩色输出，定义变量
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
endColor='\e[0m'
ecs="echo -e"

gw="./gradlew.bat"
# mac
#red='\033[31m'
#green='\033[32m'
#yellow='\033[33m'
#endColor='\033[0m'
#ecs="echo"

unames=$(uname -s)
#echo "us: $unames"
#win10 名字 MSYS_NT.  MSYS_NT-10.0-18362
if [[ ${unames} =~ $cygwnin ]]
then
    ecs="echo -e"
    gw="./gradlew.bat"
elif  [[ ${unames} =~ $mingw ]]
then
    red='\e[0;31m'
    green='\e[0;32m'
    yellow='\e[0;33m'
    endColor='\e[0m'
    ecs="echo -e"
    gw="./gradlew.bat"
elif  [[ ${unames} =~ $msys_nt ]]
then
    echo "win10......"
    red='\e[0;31m'
    green='\e[0;32m'
    yellow='\e[0;33m'
    endColor='\e[0m'
    ecs="echo -e"
    gw="./gradlew.bat"
else
    red='\033[31m'
    green='\033[32m'
    yellow='\033[33m'
    endColor='\033[0m'
    ecs="echo"
    dx="dx"
    gw="./gradlew"
fi



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
    $gw zip
}

# gradle build
function build_gradle()
{
    $gw zip
}


: '
    编译程序的入口
'


# 1. 清除缓存
clean

# 2. 编译并处理异常情况
build_gradlew

if  [ $# == 0 ]; then
    $ecs "${green}gradlew build success${endColor}"
    pwd=$(pwd)
    $ecs "${green}gradlew build jar and zip success. path: $(pwd)/release/${endColor}"
else
    $ecs "${red}gradlew build failed${endColor}"
    build_gradle
    if  [ $# == 0 ]; then
        $ecs "${green}gradle build success${endColor}"
        pwd=$(pwd)
        $ecs "${green}gradle build jar and zip success. path: $(pwd)/release/${end}"
    fi
fi
