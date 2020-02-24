#!/usr/bin/env bash
# define  global variable
# file name
filename=$(basename $0)
# 定义各自平台的adb
iadb="adb"

ipwd=$(cd `dirname $0`; pwd)

#readonly winadb="..\\tools\\adbs\\windows\\adb.exe"
#readonly macadb="../tools/adbs/mac/adb"
#readonly linuxadb="../tools/adbs/linux/adb"
readonly winadb="adb"
readonly macadb="adb"
readonly linuxadb="adb"

checkEnvArgs() {
    unames=$(uname -s)
    local cygwin="CYGWIN"
    local mingw="MINGW"
    local msys_nt="MSYS_NT"
    local macos="Darwin"
    local linux="Linux"
    if [[ ${unames} =~ $cygwin ]]; then
        echo "[$filename]your platform is win . cygwin"
        red='\e[0;31m'
        green='\e[0;32m'
        yellow='\e[0;33m'
        blue='\e[0;34m'
        endColor='\e[0m'
        ecs="echo -e"
        dx="dx.bat"
        gw="./gradlew.bat"
        if [ -z $ANDROID_HOME ]; then
            iadb=$winadb
        else
            iadb="$ANDROID_HOME\\platform-tools\\adb.exe"
        fi
    elif [[ ${unames} =~ $mingw ]]; then
        echo "[$filename]your platform is win . mingw"
        red='\033[31m'
        green='\033[32m'
        yellow='\033[33m'
        blue='\033[34m'
        endColor='\033[0m'
        ecs="echo -e"
        dx="dx.bat"
        gw="./gradlew.bat"
        if [ -z $ANDROID_HOME ]; then
            iadb=$winadb
        else
            iadb="$ANDROID_HOME\\platform-tools\\adb.exe"
        fi
    elif [[ ${unames} =~ $msys_nt ]]; then
        echo "[$filename]your platform is win10 . mingw"
        red='\e[0;31m'
        green='\e[0;32m'
        yellow='\e[0;33m'
        blue='\e[0;34m'
        endColor='\e[0m'
        ecs="echo -e"
        dx="dx.bat"
        gw="./gradlew.bat"
        if [ -z $ANDROID_HOME ]; then
            iadb=$winadb
        else
            iadb="$ANDROID_HOME\\platform-tools\\adb.exe"
        fi
    elif [[ ${unames} =~ $macos ]]; then
        echo "[$filename]your platform is macos"
        red='\033[31m'
        green='\033[32m'
        yellow='\033[33m'
        blue='\033[34m'
        endColor='\033[0m'
        # bash下需要设置echo -e参数才支持彩色打印
        ecs="echo -e"
        dx="dx"
        gw="./gradlew"
        if [ -z $ANDROID_HOME ]; then
            iadb=$macadb
        else
            iadb="$ANDROID_HOME/platform-tools/adb"
        fi
    elif [[ ${unames} =~ $linux ]]; then
        echo "[$filename]your platform is $linux"
        red='\033[31m'
        green='\033[32m'
        yellow='\033[33m'
        blue='\033[34m'
        endColor='\033[0m'
        ecs="echo -e"
        dx="dx"
        gw="./gradlew"
        if [ -z $ANDROID_HOME ]; then
            iadb=$linuxadb
        else
            iadb="$ANDROID_HOME/platform-tools/adb"
        fi
    else
        echo "[$filename]your platform is $unames"
        red='\033[31m'
        green='\033[32m'
        yellow='\033[33m'
        endColor='\033[0m'
        ecs="echo"
        dx="dx"
        gw="./gradlew"
        if [ -z $ANDROID_HOME ]; then
            iadb=$macadb
        else
            iadb="$ANDROID_HOME/platform-tools/adb"
        fi
    fi
    curtime=$(date "+%Y-%m-%d %H:%M:%S")
}
# make sure env
makesureEnv() {
    if [ "$curtime" = "" ]; then
        checkEnvArgs
    fi
}
logd() {
    makesureEnv
    if [ "$1" ]; then
        $ecs "${blue}$1${endColor}"
    fi
}
logi() {
    makesureEnv
    if [ "$1" ]; then
        $ecs "${green}$1${endColor}"
    fi
}
loge() {
    makesureEnv
    if [ "$1" ]; then
        $ecs "${red}$1${endColor}"
    fi
}
logw() {
    makesureEnv
    if [ "$1" ]; then
        $ecs "${yellow}$1${endColor}"
    fi
}

# 统一方式所有调用方式均放该方法
main() {
    makesureEnv
}
#test()
#{
#    makesureEnv
#    logd "打印一下调试信息"
#    logi "打印一下调试信息"
#    loge "打印一下调试信息"
#    logw "打印一下调试信息"
#}
# call method
main
#test
