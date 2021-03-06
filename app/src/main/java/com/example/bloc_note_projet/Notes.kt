package com.example.bloc_note_projet

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Notes (
    @PrimaryKey
    var id:Int?=null,
    var title:String?=null,
    var description:String?=null
): RealmObject()