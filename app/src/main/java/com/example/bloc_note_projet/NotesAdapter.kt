package com.example.bloc_note_projet

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.RealmResults

class NotesAdapter(private val context: Context?, private val notesList: ArrayList<Notes>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.notes_rv_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder.itemView.findViewById<TextView>(R.id.titleItem).text = notesList[position]!!.title
        holder.itemView.findViewById<TextView>(R.id.descItem).text = notesList[position]!!.description

        holder.itemView.findViewById<FloatingActionButton>(R.id.deleteNotes).setOnClickListener{
            Toast.makeText(context,"note supprimée", Toast.LENGTH_SHORT).show()
            val realm: Realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            val result= realm.where<Notes>(Notes::class.java).findAll()
            result.deleteFromRealm(position)
            notesList.clear()
            notesList.addAll(result)
            realm.commitTransaction()
            notifyDataSetChanged()
        }

        holder.itemView.findViewById<CardView>(R.id.cardNotes).setOnClickListener{
            val intent = Intent(context, EditAndModifNotesActivity::class.java)
            intent.putExtra("titre", notesList[position]!!.title.toString())
            intent.putExtra("description", notesList[position]!!.description.toString())
            intent.putExtra("index", notesList[position]!!.id)
            context?.startActivity(intent)
        }

    }

    class ViewHolder(v: View?): RecyclerView.ViewHolder(v!!){
        val title = itemView.findViewById<TextView>(R.id.titleItem)
        val desc = itemView.findViewById<TextView>(R.id.descItem)
    }
}