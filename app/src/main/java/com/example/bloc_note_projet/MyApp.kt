package com.example.bloc_note_projet

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        //initialiser realm
        Realm.init(this)

        val configuration = RealmConfiguration.Builder()
            .name("Notes.db")
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(0)
            .build()

        Realm.setDefaultConfiguration(configuration)
    }

}