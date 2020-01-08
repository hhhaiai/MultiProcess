#!/bin/sh

# 彩色输出，定义变量
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
endColor='\e[0m'
# mac
#red='\033[31m'
#green='\033[32m'
#yellow='\033[33m'
#endColor='\033[0m'


ecs="echo"
unames=$(uname -s)
n=${unames}
cygwnin="CYGWIN"
mingw="MINGW"
if [[ $n =~ $cygwnin ]]
then
    red='\e[0;31m'
    green='\e[0;32m'
    yellow='\e[0;33m'
    endColor='\e[0m'
    ecs="echo -e"
     dx="dx.bat"
elif  [[ $n =~ $mingw ]]
then
    red='\e[0;31m'
    green='\e[0;32m'
    yellow='\e[0;33m'
    endColor='\e[0m'
    ecs="echo -e"
     dx="dx.bat"
else
    red='\033[31m'
    green='\033[32m'
    yellow='\033[33m'
    endColor='\033[0m'
    ecs="echo"
    dx="dx"
fi
time=$(date "+%Y%m%d-%H%M%S")

## 红色打印
#$ecs "${red}输出色彩：红色${endColor}"
## 绿色打印
#$ecs "${green}输出色彩：红色${endColor}"
## 黄色打印
#$ecs "${yellow}输出色彩：红色${endColor}"


#
##windows 需要dx.bat，mac需要dx
#unames=$(uname -s)
#n=${unames}
#cygwnin="CYGWIN"
#mingw="MINGW"
#
#if [[ $n =~ $cygwnin ]]
#then
#    dx="dx.bat"
#elif  [[ $n =~ $mingw ]]
#then
#    dx="dx.bat"
#else
#    dx="dx"
#fi

$ecs "${yellow}[==========================================================]${endColor}"
$ecs  "${yellow}[======================   开始编译  =======================]${endColor}"
$ecs  "${yellow}[==========================================================]${endColor}"
./gradlew build

if [ $? -ne 0 ]; then
    $ecs  "${red}[********************************]${endColor}\n"
    $ecs  "${red}[**** graddew build 执行失败  ****]${endColor}\n"
    $ecs  "${red}[********************************]${endColor}\n"
else
    $ecs  "${green}[********************************]${endColor}"
    $ecs  "${green}[******* graddew build 成功 *****]${endColor}"
    $ecs  "${green}[********************************]${endColor}"
    # need delay .wait for build over
    cd ./dex/build/intermediates/classes/debug/
    $dx --dex --output=temp.dex cn/com/analysys/dex/Test.class com/analysys/Ab.class
    if [ $? -ne 0 ]; then
        $ecs  "${red}[********************************]${endColor}\n"
        $ecs  "${red}[*********** dx打包失败 **********]${endColor}\n"
        $ecs  "${red}[********************************]${endColor}\n"
    else
        $ecs  "${green}[********************************]${endColor}"
        $ecs  "${green}[*********** dx打包成功 **********]${endColor}"
        $ecs  "${green}[********************************]${endColor}"
        jar cvf temp.jar temp.dex

        if [ $? -ne 0 ]; then
                $ecs  "${red}[********************************]${endColor}\n"
                $ecs  "${red}[***********jar打包失败 **********]${endColor}\n"
                $ecs  "${red}[********************************]${endColor}\n"
            else
                $ecs  "${green}[********************************]${endColor}\n"
                $ecs  "${green}[*********** 打jar成功 **********]${endColor}\n"
                $ecs  "${green}[********************************]${endColor}\n"
                mv -f temp.jar $HOME/Desktop/temp_$time.jar
                $ecs  "${yellow}[==========================================================]${endColor}"
                $ecs  "${yellow}[======================   移动完毕   =======================]${endColor}"
                $ecs  "${yellow}[==========================================================]${endColor}"
            fi
    fi
fi



