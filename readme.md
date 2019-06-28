# track SDK

> 新版本的设备SDK. 更新工作机制,增加定制成本。

## 编译方法

* 确定版本信息: 现阶段需要手动保持一致.
    * `./dev-sdk/src/com/analysys/track/internal/Content/EGContext.java` 中`SDK_VERSION`调整SDK的版本号
    * `./dev-sdk/build.gradle`中的`version`和`date`调整打包版本号和日期
* 编译: 根目录下执行`sh build.sh`即可