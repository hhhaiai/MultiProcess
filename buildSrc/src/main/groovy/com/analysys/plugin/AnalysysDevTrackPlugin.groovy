package com.analysys.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AnalysysDevTrackPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def android = project.extensions.android
        boolean islib = true
        if (android instanceof AppExtension) {
            islib = false
        }

        project.getTasks().getByName("preBuild").doFirst {
            println("开始加密相关的准备工作")
            GenerateKeyUtil.generateKey(project)
            println("开始加密相关的准备工作结束")
        }

        android.registerTransform(new AllStringTransform(project, islib))

    }


}
