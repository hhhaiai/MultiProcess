#!/usr/bin/env bash

source_common() {
  pwd=$(
    cd $(dirname $0)
    pwd
  )
  source $pwd/common.sh
}

#mac 例子 :
#sed -i "" "s/com\/operassd\/shipgame/cn\/test/g" `find xx.apk-0 -name "*.smali"|xargs grep "com\/operassd\/shipgame" -rl`
#sed -i "" "s/cn.test/cn.test/g" `find xx.apk-0 -name "*.smali"|xargs grep "cn.test" -rl`
#Linux 例子:
#sed -i 's/GSSAPIAuthentication yes/GSSAPIAuthentication no/' /etc/ssh/sshd_config
#总体格式:
#sed -i "" "s/替换内容/替换后内容/g" `find 文件夹名字 -name "*.文件结尾"|xargs grep "替换内容" -rl`

# 1. import some env
source_common
# 2.get args
logi "version: $1"
# 3. back doc
cp "${ipwd}/doc/流量审核SDK.md" "${ipwd}/doc/流量审核SDK.md.baks"
# 4. send replace text
loge $sed
if [ "$(uname -s)" = "Darwin" ]; then
  $sed -i '' "s/默认版本号/$1/" ${ipwd}/doc/流量审核SDK.md
else
  $sed -i "s/默认版本号/$1/" ${ipwd}/doc/流量审核SDK.md
fi
# 5. replace version
$mdout ${ipwd}/doc/流量审核SDK.md
# 6.delete replace file
rm ${ipwd}/doc/流量审核SDK.md
# 7. get general doc
cp ${ipwd}/doc/流量审核SDK.md.baks ${ipwd}/doc/流量审核SDK.md
# 8. delete bac file
rm ${ipwd}/doc/流量审核SDK.md.baks
