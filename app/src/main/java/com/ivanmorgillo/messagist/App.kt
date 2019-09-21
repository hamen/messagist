package com.ivanmorgillo.messagist

import android.app.Application
import android.os.StrictMode
import com.google.gson.Gson
import com.ivanmorgillo.messagist.sync.MessagesSyncManager
import com.ivanmorgillo.messagist.sync.MessagesSyncManagerImpl
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        enableStrictMode()

        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }

    private fun enableStrictMode() {
        @Suppress("ConstantConditionIf")
        if (BuildConfig.BUILD_TYPE == "debug") {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}

val appModule = module {
    single<SqlDriver> { AndroidSqliteDriver(Database.Schema, androidContext(), "messagist.db") }

    single<MessagesSyncManager> {
        MessagesSyncManagerImpl(
            context = androidApplication(),
            gson = Gson(),
            sqlDriver = get()
        )
    }

    viewModel {
        MessageListViewModel(
            syncManager = get(),
            database = Database(driver = get())
        )
    }
}

class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
    }
}
