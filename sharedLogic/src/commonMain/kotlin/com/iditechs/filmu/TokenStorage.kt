package com.iditechs.filmu

interface CredentialStorage {
    suspend fun getCredentials(): ServerCredentials?
    suspend fun setCredentials(credentials: ServerCredentials?)
    suspend fun clear()
}

class InMemoryCredentialStorage : CredentialStorage {
    private var credentials: ServerCredentials? = null

    override suspend fun getCredentials(): ServerCredentials? = credentials

    override suspend fun setCredentials(credentials: ServerCredentials?) {
        this.credentials = credentials
    }

    override suspend fun clear() {
        credentials = null
    }
}
