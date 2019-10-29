package com.miqt.plugin


import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class CostTimePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("costtime", CostTimeConfig)
        def android = project.extensions.android
        android.registerTransform(new CostTransform(project))
        android.registerTransform(new StringFogTransform(project))
    }


}
