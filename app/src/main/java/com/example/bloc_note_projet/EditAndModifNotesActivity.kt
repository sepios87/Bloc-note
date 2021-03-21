package com.example.bloc_note_projet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import io.realm.Realm

class EditAndModifNotesActivity : AppCompatActivity() {

    private lateinit var titleEd: EditText
    private lateinit var description: EditText
    private lateinit var saveNoteBtn: MaterialButton
    private lateinit var realm: Realm
    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_modif_notes)

        realm = Realm.getDefaultInstance()
        titleEd = findViewById(R.id.title_EditText)
        description = findViewById(R.id.description_EditText)
        saveNoteBtn = findViewById(R.id.saveNotesBtn)
        val btnRetour = findViewById<Button>(R.id.btn_retour)
        val btnShare = findViewById<ImageButton>(R.id.btn_share)

        val intent = intent
        val index = intent.getIntExtra("index", 0)

        var note: Notes? = null

        realm.beginTransaction()
        note = realm.where(Notes::class.java).equalTo("id", index).findFirst()
        realm.commitTransaction()

        if (note != null) {
            titleEd.text = intent.getStringExtra("titre").toString().toEditable()
            description.text = intent.getStringExtra("description").toString().toEditable()
        }

        saveNoteBtn.setOnClickListener{
            if (note == null) addNotesToDB(); else modifNotesToBD(note)
            finish()
        }

        btnRetour.setOnClickListener{
            finish()
        }

        btnShare.setOnClickListener{
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, description.text.toString())
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleEd.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Partager sur"))
        }

    }

    private fun modifNotesToBD(note:Notes){
        try{
            realm.beginTransaction()

            note?.title = titleEd.text.toString()
            note?.description = description.text.toString()

            realm.copyToRealmOrUpdate(note)
            realm.commitTransaction()
            Toast.makeText(this, "Modification effectuée", Toast.LENGTH_SHORT).show()
        }catch (exception:Exception){
            Toast.makeText(this, "La modification de la note a échouée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNotesToDB() {

        try{
            realm.beginTransaction()

            //auto incrément ID
            val currentIdNumber:Number? = realm.where(Notes::class.java).max("id")
            val nextId:Int = if (currentIdNumber == null) 1; else currentIdNumber.toInt()+1

            val notes = Notes()
            notes.title = titleEd.text.toString()
            notes.description = description.text.toString()
            notes.id = nextId

            //copier la transaction et envoyer
            realm.copyToRealmOrUpdate(notes)
            realm.commitTransaction()

            Toast.makeText(this, "La note a été ajoutée", Toast.LENGTH_SHORT).show()

        }catch (exception:Exception){
            Toast.makeText(this, "L'ajout de la note a échouée", Toast.LENGTH_SHORT).show()
        }

    }
}