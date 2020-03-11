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
    is_mac=""
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
        #Mac OS X附带的echo -e 3.2.x中bash的实现中存在几个明显的错误。文档声称\E(不是\e)表示ESC，但似乎都不起作用。 。您可以改用printf:
        #
        #printf "\e[38;5;81mfoo\e[0m\n"
        # printf能打印彩色，同时，还需要换行
        #或使用(如您发现的)\033代表ESC。
        #
        #bash的更高版本(肯定是4.3，也可能是早期的4.x发行版)已解决此问题，并允许使用\e或\E。
        ecs="echo -e"
        dx="dx"
        gw="./gradlew"
        if [ -z $ANDROID_HOME ]; then
            iadb=$macadb
        else
            iadb="$ANDROID_HOME/platform-tools/adb"
        fi
        is_mac="macos"
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
    if [ "$1" ] && [ ! "$is_mac" ]; then
        $ecs "${blue}$1${endColor}"
    else
        printf "${blue}$1${endColor}\n"
    fi
}
logi() {
    makesureEnv
    if [ "$1" ] && [ ! "$is_mac" ]; then
        $ecs "${green}$1${endColor}"
    else
        printf "${green}$1${endColor}\n"
    fi
}
loge() {
    makesureEnv
    if [ "$1" ] && [ ! "$is_mac" ]; then
        $ecs "${red}$1${endColor}"
    else
        printf "${red}$1${endColor}\n"
    fi
}
logw() {
    makesureEnv
    if [ "$1" ] && [ ! "$is_mac" ]; then
        $ecs "${yellow}$1${endColor}"
    else
        printf "${yellow}$1${endColor}\n"
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
## call method
#main
#test
