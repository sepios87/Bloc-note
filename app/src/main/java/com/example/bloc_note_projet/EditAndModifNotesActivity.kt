package com.example.bloc_note_projet

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import io.realm.Realm
import java.util.*

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
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
        var days = -1
            // R??cup??ration du jour :
            val dayregex = "(Lundi|lundi|Mardi|mardi|Mercredi|mercredi|Jeudi|jeudi|Vendredi|vendredi|Samedi|samedi|Dimanche|dimanche)".toRegex()
            val hasDay = dayregex.containsMatchIn(dateHeure)
            if (hasDay){
                days = convertDays(dayregex.find(dateHeure, 0)!!.value.toLowerCase())
            }
            // R??cup??ration de l'heure :
            val timeregex ="([01]?[0-9]|2[0-3])h(([0-5][0-9])?)".toRegex()
            val matches = timeregex.find(dateHeure, 0)!!.value
            var hourmin = matches.split("h").toTypedArray()
            if(hourmin.size ==2){
                if ( hourmin[1].toString() == "") {
                    setAlarm(hourmin[0].toInt(), 0, titleEd.text.toString(), days)
                    Toast.makeText(applicationContext, "Alarme ajout??e ?? "+hourmin[0].toInt()+"h00 " + dateHeure, Toast.LENGTH_SHORT).show()
                } else {
                    setAlarm(hourmin[0].toInt(), hourmin[1].toInt(), titleEd.text.toString(), days)
                    Toast.makeText(applicationContext, "Alarme ajout??e ?? "+hourmin[0].toInt()+"h"+hourmin[1].toInt(), Toast.LENGTH_SHORT).show()
                }
            }

        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
            Toast.makeText(applicationContext, "L'alarme n'as pas ??t?? ajout??e", Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    fun convertDays(day : String): Int {
        when (day) {
            "lundi" -> return Calendar.MONDAY
            "mardi" -> return Calendar.TUESDAY
            "mercredi" -> return Calendar.WEDNESDAY
            "jeudi" -> return Calendar.THURSDAY
            "vendredi" -> return Calendar.FRIDAY
            "samedi" -> return Calendar.SATURDAY
            "dimanche" -> return Calendar.SUNDAY
            else -> { // En cas d'erreur / impossible mais on sait jamais
                print("Y'a erreur l?? non ?")
                return -1
            }
        }
    }

    // Fonction qui rajoute une alarme en allant chercher directement le composant
    fun setAlarm(hour : Int, minute : Int, titre : String, day : Int){
        val intent : Intent = Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour)
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute)
        if (day == -1){
            intent.putExtra(AlarmClock.EXTRA_DAYS, Calendar.SUNDAY)
        }
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, titre) // A d??finir avec le titre de la note

        if (hour <= 24 && minute <= 60) {
            startActivity(intent);
        }
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
            Toast.makeText(this, "L'ajout de la note a ??chou??e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun matchRegex(): Pair<SpannableString, Sequence<MatchResult>> {
        val s = description.text;
        val content = SpannableString(s)
        val regex =
            "((Lundi|lundi|Mardi|mardi|Mercredi|mercredi|Jeudi|jeudi|Vendredi|vendredi|Samedi|samedi|Dimanche|dimanche)\\s)?([01]?[0-9]|2[0-3])h(([0-5][0-9])?)".toRegex()
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
            Toast.makeText(this, "Modification effectu??e", Toast.LENGTH_SHORT).show()
        }catch (exception:Exception){
            Toast.makeText(this, "La modification de la note a ??chou??e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addNotesToDB() {
        try{
            realm.beginTransaction()
            //auto incr??ment ID
            val currentIdNumber:Number? = realm.where(Notes::class.java).max("id")
            val nextId:Int = if (currentIdNumber == null) 1; else currentIdNumber.toInt()+1

            val notes = Notes()
            notes.title = titleEd.text.toString()
            notes.description = description.text.toString()
            notes.id = nextId
            //copier la transaction et envoyer
            realm.copyToRealmOrUpdate(notes)
            realm.commitTransaction()
            Toast.makeText(this, "La note a ??t?? ajout??e", Toast.LENGTH_SHORT).show()
        }catch (exception:Exception){
            Toast.makeText(this, "L'ajout de la note a ??chou??e", Toast.LENGTH_SHORT).show()
        }
    }
}