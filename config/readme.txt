# kvs规则

> kvs位于手机的/data/local/tmp/kvs，用于调试时忽略调试模式。
>  该规则同样应用于服务器下发
>

## key/value规则

### 1. 忽略调试状态

* key: `i_debug`
* value: 大于等于0即可忽略

### 2. 忽略新安装设备

* key: `i_new_install`
* value: 大于等于0即可忽略

### 2. 忽略新设备

* key: `i_new_device`
* value: 大于等于0即可忽略