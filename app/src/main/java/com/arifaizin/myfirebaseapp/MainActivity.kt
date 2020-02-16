package com.arifaizin.myfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*






class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FirebaseRecyclerAdapter<ChatModel, ChatHolder>
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

        showMessages(name)
    }

    private fun showMessages(name: String?) {
        val query = FirebaseDatabase.getInstance()
            .reference
            .child(MESSAGES_CHILD)
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<ChatModel>()
            .setQuery(query, ChatModel::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<ChatModel, ChatHolder>(options) {

            val MSG_TYPE_LEFT = 0
            val MSG_TYPE_RIGHT = 1

            override fun getItemViewType(position: Int): Int {
                val itemName = getItem(position).name
                return if (itemName == name){
                    MSG_TYPE_RIGHT
                } else {
                    MSG_TYPE_LEFT
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item

                return if (viewType == MSG_TYPE_RIGHT) {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_right, parent, false)
                    ChatHolder(view)
                } else {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_chat_left, parent, false)
                    ChatHolder(view)
                }
            }

            protected override fun onBindViewHolder(
                holder: ChatHolder,
                position: Int,
                model: ChatModel
            ) {
                //set data
                holder.tvMessage.text = model.text
                holder.tvMessenger.text = model.name
                holder.tvTimestamp.setReferenceTime(model.timestamp)
                Glide.with(this@MainActivity)
                    .load(model.photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivMessenger);
            }
        }

        messageRecyclerView.adapter = adapter
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    class ChatHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvMessage = itemView.findViewById<TextView>(R.id.messageTextView)
        val tvMessenger = itemView.findViewById<TextView>(R.id.messengerTextView)
        val tvTimestamp = itemView.findViewById<RelativeTimeTextView>(R.id.timestamp)
        val ivMessenger = itemView.findViewById<ImageView>(R.id.messengerImageView)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
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
