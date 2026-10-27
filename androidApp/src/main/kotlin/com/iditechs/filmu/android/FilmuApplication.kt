package com.iditechs.filmu.android

import android.app.Application
import com.iditechs.filmu.CredentialStorage
import com.iditechs.filmu.FilmuClient
import com.iditechs.filmu.SecureCredentialStorage

class FilmuApplication : Application() {

    lateinit var credentialStorage: CredentialStorage
        private set

    var client: FilmuClient? = null

    override fun onCreate() {
        super.onCreate()
        credentialStorage = SecureCredentialStorage(this)
    }
}
