package com.example.bloc_note_projet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmQuery

class EditAndModifNotesActivity : AppCompatActivity() {

    private lateinit var titleEd: EditText
    private lateinit var description: EditText
    private lateinit var saveNoteBtn: MaterialButton
    private lateinit var realm: Realm
    private var lastLength: Int = 0
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

        lastLength = description.text.length
        val s = description.text;
        val content = SpannableString(s)
        val regex = "(([0123]?[0-9])\\s(janvier|février|fevrier|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|décembre|decembre)\\s)?(\\d{4}\\s)?([01]?[0-9]|2[0-3])h([0-5][0-9])?".toRegex()
        val matches = regex.findAll(s)
        matches.forEach { f ->
            val m = f.value
            val idx = ((f.range).toString().split(".."))[0]
            content.setSpan(UnderlineSpan(), idx.toInt(), idx.toInt() + m.length, 0)
        }
        description.setText(content)

        val builder = AlertDialog.Builder(this)

        description.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.length != lastLength) {
                    val position = description.selectionStart
                    lastLength = s.length;
                    val s = description.text;
                    val content = SpannableString(s)
                    val regex = "(([0123]?[0-9])\\s(janvier|février|fevrier|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|décembre|decembre)\\s)?(\\d{4}\\s)?(([01]?[0-9]|2[0-3])h([0-5][0-9])?)\\s".toRegex()
                    val matches = regex.findAll(s)
                    matches.forEach { f ->
                        val m = f.value
                        val idx = ((f.range).toString().split(".."))[0]
                        content.setSpan(UnderlineSpan(), idx.toInt(), idx.toInt() + m.length, 0)

                        var dateNote: DateNotes? = null
                        realm.beginTransaction()
                        dateNote = realm.where(DateNotes::class.java).equalTo("date", f.value).equalTo("idNote", index).findFirst()
                        realm.commitTransaction()
                        Log.e("Help", dateNote.toString())
                        if (dateNote == null) {
                            realm.beginTransaction()
                            val currentIdNumber:Number? = realm.where(DateNotes::class.java).max("id")
                            val nextId:Int = if (currentIdNumber == null) 1; else currentIdNumber.toInt()+1

                            val dateNote = DateNotes()
                            dateNote.date = f.value
                            dateNote.idNote = index
                            dateNote.id = nextId
                            realm.copyToRealmOrUpdate(dateNote)
                            realm.commitTransaction()

                            builder.setTitle("Voulez-vous rajouter cette alarme ?")
                            builder.setMessage(f.value)

                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                Toast.makeText(applicationContext,
                                    android.R.string.yes, Toast.LENGTH_SHORT).show()
                            }

                            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                                Toast.makeText(applicationContext,
                                    android.R.string.no, Toast.LENGTH_SHORT).show()
                            }
                            builder.show()
                        }
                    }
                    description.setText(content)
                    description.setSelection(position)
                }
            }
        })
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