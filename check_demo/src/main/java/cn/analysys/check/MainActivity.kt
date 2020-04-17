package cn.analysys.check

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.analysys.track.internal.net.UploadImpl

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btnReport -> Thread({
                UploadImpl.getInstance(this).doUploadImpl();
            }).start()
        }
    }
}
