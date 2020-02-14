#!/bin/bash


: '
 清除程序入口
'

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
elif  [[ ${unames} =~ $msys_nt ]]
then
    echo "win10......"
    red='\e[0;31m'
    green='\e[0;32m'
    yellow='\e[0;33m'
    endColor='\e[0m'
else
    red='\033[31m'
    green='\033[32m'
    yellow='\033[33m'
    endColor='\033[0m'
    ecs="echo"
fi


$ecs ">>>>clean project<<<<"
dir=("app" "dev-sdk" "buildSrc" "dex")
for element in ${dir[@]}
do
    #clean task
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.externalNativeBuild
    $ecs "${green} clean $element over.${endColor}"
done

rm -rf build/
rm -rf release/
rm -rf releasebak/
rm -rf sh.exe.stackdump
rm -rf classes.dex

if  [ $# == 0 ]; then
    $ecs "${yellow} clean project success.${endColor} "
else
    $ecs "${red} clean project Failed!${endColor}"
fi
