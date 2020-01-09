#!/bin/bash


: '
 清除程序入口
'


red='\033[31m'
green='\033[32m'
yellow='\033[33m'
end='\033[0m'


echo ">>>>clean project<<<<"
dir=("app" "dev-sdk" "buildSrc" "dex")
for element in ${dir[@]}
do
    #clean task
    rm -rf $element/build/
    rm -rf $element/bin/
    rm -rf $element/gen/
    rm -rf $element/.externalNativeBuild
    rm -rf $element/.gradle
    echo "${green}clean $element over.${end}"
done

rm -rf build/
rm -rf release/
rm -rf releasebak/

if  [ $# == 0 ]; then
    echo "${yellow}clean project success.${end} "
else
    echo "${red}clean project Failed!${end}"
fi
