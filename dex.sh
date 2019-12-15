#!/bin/sh

# 彩色输出，定义变量
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
endColor='\e[0m'

## 红色打印
#echo -e "${red}输出色彩：红色${endColor}"
## 绿色打印
#echo -e "${green}输出色彩：红色${endColor}"
## 黄色打印
#echo -e "${yellow}输出色彩：红色${endColor}"



#windows 需要dx.bat，mac需要dx
unames=$(uname -s)
n=${unames}
cygwnin="CYGWIN"
mingw="MINGW"

if [[ $n =~ $cygwnin ]]
then
    dx="dx.bat"
elif  [[ $n =~ $mingw ]]
then
    dx="dx.bat"
else
    dx="dx"
fi

echo  -e "${yellow}[==========================================================]${endColor}"
echo  -e "${yellow}[======================   开始编译  =======================]${endColor}"
echo  -e "${yellow}[==========================================================]${endColor}"
./gradlew build

if [ $? -ne 0 ]; then
    echo -e "${red}[********************************]${endColor}\n"
    echo -e "${red}[**** graddew build 执行失败  ****]${endColor}\n"
    echo -e "${red}[********************************]${endColor}\n"
else
    echo -e "${green}[********************************]${endColor}"
    echo -e "${green}[******* graddew build 成功 *****]${endColor}"
    echo -e "${green}[********************************]${endColor}"
    # need delay .wait for build over
    cd ./app/build/intermediates/classes/debug/
    $dx --dex --output=temp.dex com/device/impls/Test.class
    if [ $? -ne 0 ]; then
        echo -e "${red}[********************************]${endColor}\n"
        echo -e "${red}[*********** dx打包失败 **********]${endColor}\n"
        echo -e "${red}[********************************]${endColor}\n"
    else
        echo -e "${green}[********************************]${endColor}"
        echo -e "${green}[*********** dx打包成功 **********]${endColor}"
        echo -e "${green}[********************************]${endColor}"
        jar cvf temp.jar temp.dex

        if [ $? -ne 0 ]; then
                echo -e "${red}[********************************]${endColor}\n"
                echo -e "${red}[***********jar打包失败 **********]${endColor}\n"
                echo -e "${red}[********************************]${endColor}\n"
            else
                echo -e "${green}[********************************]${endColor}\n"
                echo -e "${green}[*********** 打jar成功 **********]${endColor}\n"
                echo -e "${green}[********************************]${endColor}\n"
                mv -f temp.jar $HOME/Desktop/temp_005.jar
                echo -e "${yellow}[==========================================================]${endColor}"
                echo -e "${yellow}[======================   移动完毕   =======================]${endColor}"
                echo -e "${yellow}[==========================================================]${endColor}"
            fi
    fi
fi



