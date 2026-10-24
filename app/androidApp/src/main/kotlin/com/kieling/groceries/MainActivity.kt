package com.kieling.groceries

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.FirebaseApp
import com.kieling.groceries.data.AndroidContextHolder
import com.kieling.groceries.data.CurrentActivityHolder
import com.kieling.groceries.di.appModule
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            AndroidContextHolder.applicationContext = applicationContext
            application?.registerActivityLifecycleCallbacks(
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityResumed(activity: Activity) {
                        CurrentActivityHolder.currentActivity = activity
                    }

                    override fun onActivityPaused(activity: Activity) {
                        if (CurrentActivityHolder.currentActivity == activity) {
                            CurrentActivityHolder.currentActivity = null
                        }
                    }

                    override fun onActivityCreated(
                        activity: Activity,
                        savedInstanceState: Bundle?,
                    ) {
                    }

                    override fun onActivityStarted(activity: Activity) {}

                    override fun onActivityStopped(activity: Activity) {}

                    override fun onActivitySaveInstanceState(
                        activity: Activity,
                        outState: Bundle,
                    ) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {}
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
    KoinApplication(
        configuration =
            koinConfiguration {
                modules(appModule)
            },
    ) {
        App()
    }
}
