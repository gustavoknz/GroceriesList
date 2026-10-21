package com.kieling.groceries

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            AndroidContextHolder.applicationContext = applicationContext
            (application as? Application)?.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityResumed(activity: android.app.Activity) {
                        CurrentActivityHolder.currentActivity = activity
                    }

                    override fun onActivityPaused(activity: android.app.Activity) {
                        if (CurrentActivityHolder.currentActivity == activity) {
                            CurrentActivityHolder.currentActivity = null
                        }
                    }

                    override fun onActivityCreated(
                        activity: android.app.Activity,
                        savedInstanceState: Bundle?,
                    ) {}

                    override fun onActivityStarted(activity: android.app.Activity) {}

                    override fun onActivityStopped(activity: android.app.Activity) {}

                    override fun onActivitySaveInstanceState(
                        activity: android.app.Activity,
                        outState: Bundle,
                    ) {}

                    override fun onActivityDestroyed(activity: android.app.Activity) {}
                },
            )
            CurrentActivityHolder.currentActivity = this
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
