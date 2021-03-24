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

        realm.beginTransaction()
        var note: Notes? = realm.where(Notes::class.java).equalTo("id", index).findFirst()
        realm.commitTransaction()
        if (note != null) {
            titleEd.text = intent.getStringExtra("titre").toString().toEditable()
            description.text = intent.getStringExtra("description").toString().toEditable()
        }

        saveNoteBtn.setOnClickListener{
            if (note == null) addNotesToDB(); else modifNotesToBD(note)
            finish()
        }

        btnRetour.setOnClickListener{ finish() }

        btnShare.setOnClickListener{
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, description.text.toString())
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleEd.text.toString())
            startActivity(Intent.createChooser(shareIntent, "Partager sur"))
        }

        lastLength = description.text.length
        val (content, matches) = matchRegex()
        matches.forEach { f ->
            val m = f.value
            val idx = ((f.range).toString().split(".."))[0]
            content.setSpan(UnderlineSpan(), idx.toInt(), idx.toInt() + m.length, 0)
        }
        description.setText(content)

        description.addTextChangedListener(object: TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length != lastLength) {
                    val position = description.selectionStart
                    lastLength = s.length;
                    val (content, matches) = matchRegex()
                    matches.forEach { f ->
                        val dateHeure = f.value
                        val idx = ((f.range).toString().split(".."))[0]
                        content.setSpan(UnderlineSpan(), idx.toInt(), idx.toInt() + dateHeure.length, 0)

                        realm.beginTransaction()
                        val dateNote: DateNotes? = realm.where(DateNotes::class.java).equalTo("date", f.value).equalTo("idNote", index).findFirst()
                        realm.commitTransaction()

                        if (dateNote == null) {
                            addNoteDateToBD(dateHeure, index)
                            popUp(dateHeure)
                        }

                        description.setText(content)
                        description.requestFocus()
                        description.setSelection(position)
                    }
                }
            }
        })
    }

    private fun popUp(dateHeure : String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Voulez-vous rajouter cette alarme ?")
        builder.setMessage(dateHeure)

        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            Toast.makeText(applicationContext, "Alarme ajoutée", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
            Toast.makeText(applicationContext, "L'alarme n'as pas été ajoutée", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun addNoteDateToBD(value : String, index : Int){
        try{
        realm.beginTransaction()
        val currentIdNumber:Number? = realm.where(DateNotes::class.java).max("id")
        val nextId:Int = if (currentIdNumber == null) 1; else currentIdNumber.toInt()+1

        val dateNote = DateNotes()
        dateNote.date = value
        dateNote.idNote = index
        dateNote.id = nextId
        realm.copyToRealmOrUpdate(dateNote)
        realm.commitTransaction()
        }catch (exception:Exception){
            Toast.makeText(this, "L'ajout de la note a échouée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun matchRegex(): Pair<SpannableString, Sequence<MatchResult>> {
        val s = description.text;
        val content = SpannableString(s)
        val regex =
            "(([0123]?[0-9])\\s(janvier|février|fevrier|mars|avril|mai|juin|juillet|aout|août|septembre|octobre|novembre|décembre|decembre)\\s)?(\\d{4}\\s)?([01]?[0-9]|2[0-3])h([0-5][0-9])?".toRegex()
        val matches = regex.findAll(s)
        return Pair(content, matches)
    }


    private fun modifNotesToBD(note:Notes){
        try{
            realm.beginTransaction()

            note.title = titleEd.text.toString()
            note.description = description.text.toString()

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