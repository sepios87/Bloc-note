package com.example.bloc_note_projet

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import android.widget.SearchView

class MainActivity : AppCompatActivity() {

    private lateinit var addNotes: FloatingActionButton
    private lateinit var notesRV:RecyclerView
    private lateinit var realm: Realm
    private lateinit var list:ArrayList<Notes>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        list = ArrayList()

        //initialiser les vues
        realm = Realm.getDefaultInstance()
        addNotes = findViewById(R.id.addNotes)
        notesRV = findViewById(R.id.notesRV)
        notesRV.adapter = NotesAdapter(this, list)

        //ajout du clique du boutton
        addNotes.setOnClickListener{
            startActivity(Intent(this, EditAndModifNotesActivity::class.java))
        }

        notesRV.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        getAllNotes()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu,menu)
        val menuItem: MenuItem? =menu?.findItem(R.id.search)
        val searchView: SearchView = menuItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                val results = if (newText !== "") realm.where<Notes>(Notes::class.java).contains("title", newText, Case.INSENSITIVE).findAll()
                else realm.where<Notes>(Notes::class.java).findAll()
                refreshNote(results)
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun getAllNotes() {
        val results: RealmResults<Notes> = realm.where<Notes>(Notes::class.java).findAll()
        refreshNote(results)
    }

    private fun refreshNote(results: RealmResults<Notes>){
        list.clear()
        list.addAll(realm.copyFromRealm(results))
        notesRV.adapter!!.notifyDataSetChanged()
    }
}