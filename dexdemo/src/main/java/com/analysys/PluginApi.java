package com.analysys;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 用于方便配置混淆和规范插件开发的注解，不参与运行，只参与代码规范性管理。
 * @Version: 1.0
 * @Create: 2020-09-15 16:37:24
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@interface PluginApi {
}
