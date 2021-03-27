package com.example.bloc_note_projet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.RealmResults

class MainActivity : AppCompatActivity() {

    private lateinit var addNotes: FloatingActionButton
    private lateinit var notesRV:RecyclerView
    private lateinit var notesList:ArrayList<Notes>
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialiser les vues
        realm = Realm.getDefaultInstance()
        addNotes = findViewById(R.id.addNotes)
        notesRV = findViewById(R.id.notesRV)

        //ajout du clique du boutton
        addNotes.setOnClickListener{
            startActivity(Intent(this, EditAndModifNotesActivity::class.java))
        }

        notesRV.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        getAllNotes()

    }

    override fun onResume() {
        super.onResume()
        notesRV.adapter!!.notifyDataSetChanged()
    }

    private fun getAllNotes() {
        notesList = ArrayList()
        val results:RealmResults<Notes> = realm.where<Notes>(Notes::class.java).findAll()
        notesRV.adapter = NotesAdapter(this, results)
        //notesRV.adapter!!.notifyDataSetChanged()
    }
}