package com.example.bloc_note_projet

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class DateNotes (
    @PrimaryKey
    var id:Int?=null,
    var idNote:Int?=null,
    var date:String?=null
): RealmObject()