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

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    companion object{
        private const val MESSAGES_CHILD = "message"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val user = FirebaseAuth.getInstance().currentUser
        val name = user?.displayName
        val photoUrl = user?.photoUrl

        database = FirebaseDatabase.getInstance().reference

        btnSend.setOnClickListener {
            //siapin data yg dimasukkan ke model
            val message = edMessage.text.toString()
            val timestamp = java.util.Date().time

            //cek message jika ksosong
            if (message.isEmpty()){
                Toast.makeText(this,
                    "Teks masih kosong!",
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                //masukkan ke model
                val chatMessage = ChatModel(
                    message,
                    name.toString(),
                    photoUrl.toString(),
                    timestamp)

                database.child(MESSAGES_CHILD).push().setValue(chatMessage)
                    .addOnCompleteListener {
                        Toast.makeText(this,
                            "Terkirim!",
                            Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener {
                        Toast.makeText(this,
                            "Gagal terkirim!",
                            Toast.LENGTH_SHORT)
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
