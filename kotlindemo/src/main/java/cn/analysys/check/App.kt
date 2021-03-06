package cn.analysys.check

import android.app.Application
import com.analysys.track.AnalysysTracker

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalysysTracker.init(this, "testappkey", "channel")
    }
}