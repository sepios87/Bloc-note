package com.example.bloc_note_projet

import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import io.realm.RealmResults

class EditNotesActivity : AppCompatActivity() {

    private lateinit var titleEd: EditText
    private lateinit var description: EditText
    private lateinit var saveNoteBtn: MaterialButton
    private lateinit var realm: Realm
    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        realm = Realm.getDefaultInstance()
        titleEd = findViewById(R.id.title_EditText)
        description = findViewById(R.id.description_EditText)
        saveNoteBtn = findViewById(R.id.saveNotesBtn)
        val btn_retour = findViewById<Button>(R.id.btn_retour);

        val intent = intent
        titleEd.text = intent.getStringExtra("titre").toString().toEditable()
        description.text = intent.getStringExtra("description").toString().toEditable()

        saveNoteBtn.setOnClickListener{
            modif()
        }

        btn_retour.setOnClickListener{
            finish()
        }

    }

    private fun modif(){
        val intent = intent
        val index = intent.getIntExtra("index", 0)
        realm.beginTransaction()
        var note: Notes? = realm.where(Notes::class.java).equalTo("id", index).findFirst()
        if (note == null) {
            note = realm.createObject(Notes::class.java, index)
        }
        note?.title = titleEd.text.toString()
        note?.description = description.text.toString()
        realm.copyToRealmOrUpdate(note)
        realm.commitTransaction()
        Toast.makeText(this, "Modification effectuée", Toast.LENGTH_SHORT).show()

        finish()
    }
}