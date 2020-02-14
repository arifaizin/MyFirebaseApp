package com.arifaizin.myfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    companion object {
        private const val MESSAGES_CHILD = "message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO 2 : get data from Auth
        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName
        val photoUrl = user?.photoUrl.toString()

        //TODO 3 : send to firebase database
        database = FirebaseDatabase.getInstance().reference

        btnSend.setOnClickListener {
            val message = edMessage.text.toString()
            val timestamp = Date().time
            if (message.isEmpty()) {
                Toast.makeText(this@MainActivity, "Tidak bisa mengirim teks kosong", Toast.LENGTH_SHORT).show()
            } else {
                val chatMessage = ChatModel(message, name, photoUrl, timestamp)
                database.child(MESSAGES_CHILD).push().setValue(chatMessage)
                    .addOnSuccessListener {
                        Toast.makeText(this@MainActivity, "Terkirim.", Toast.LENGTH_SHORT).show()
                        edMessage.setText("")
                    }.addOnFailureListener {
                        Toast.makeText(this@MainActivity, "Gagal Terkirim.", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_signout) {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
        return true
    }
}
