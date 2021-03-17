package com.example.bloc_note_projet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import java.lang.Exception

class AddNotesActivity : AppCompatActivity() {

    private lateinit var titleEd:EditText
    private lateinit var description:EditText
    private lateinit var saveNoteBtn:MaterialButton
    private lateinit var realm:Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_notes)

        //init vues
        realm = Realm.getDefaultInstance()
        titleEd = findViewById(R.id.title_EditText)
        description = findViewById(R.id.description_EditText)
        saveNoteBtn = findViewById(R.id.saveNotesBtn)

        //onClick

        saveNoteBtn.setOnClickListener{
            addNotesToDB()
        }

    }

    private fun addNotesToDB() {

        try{

            //auto incrément ID
            realm.beginTransaction()
            val currentIdNumber:Number? = realm.where(Notes::class.java).max("id")

            val nextId:Int = if(currentIdNumber == null){
                1
            }else {
                currentIdNumber.toInt()+1
            }

            val notes = Notes()
            notes.title = titleEd.text.toString()
            notes.description = description.text.toString()
            notes.id = nextId

            //copier la transaction et envoyer
            realm.copyToRealmOrUpdate(notes)
            realm.commitTransaction()

            Toast.makeText(this, "La note a été ajoutée", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()

        }catch (exception:Exception){

            Log.e("erreur message", exception.toString())
            Toast.makeText(this, "L'ajout de la note a échouée", Toast.LENGTH_SHORT).show()

        }

    }
}