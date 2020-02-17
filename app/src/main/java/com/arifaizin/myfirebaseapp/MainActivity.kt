package com.arifaizin.myfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import okhttp3.*
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var adapter: FirebaseRecyclerAdapter<ChatModel, ChatHolder>
    private lateinit var database: DatabaseReference

    companion object {
        private const val MESSAGES_CHILD = "message"
        private val TAG = MainActivity::class.java.simpleName

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
                sendNotif(name.toString(), message)
            }
        }

        // TODO 4 : Firebase UI
        showMessages(name)
    }

    private fun showMessages(name: String?) {
        val query = database
            .child(MESSAGES_CHILD)
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<ChatModel>()
            .setQuery(query, ChatModel::class.java)
            .build()

        adapter = object : FirebaseRecyclerAdapter<ChatModel, ChatHolder>(options) {
            private val MSG_TYPE_LEFT = 0
            private val MSG_TYPE_RIGHT = 1

            override fun getItemViewType(position: Int): Int {
                val itemName = getItem(position).name
                return if (itemName != null && itemName == name) {
                    MSG_TYPE_RIGHT
                } else {
                    MSG_TYPE_LEFT
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
                return if (viewType == MSG_TYPE_RIGHT) {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
                    ChatHolder(view)
                } else {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false)
                    ChatHolder(view)
                }
            }

            override fun onBindViewHolder(
                holder: ChatHolder,
                position: Int,
                model: ChatModel
            ) {
                if (model.text != null) {

                    holder.messageTextView.text = model.text
                    holder.messengerTextView.text = model.name
                    holder.timestamp.setReferenceTime(model.timestamp as Long)

                    if (model.photoUrl != null) {
                        Glide.with(this@MainActivity)
                            .load(model.photoUrl)
                            .apply(RequestOptions.circleCropTransform())
                            .error(R.drawable.ic_account_round)
                            .into(holder.messengerImageView)
                    }

                } else {
                    Toast.makeText(this@MainActivity, "Tidak ada data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        messageRecyclerView.adapter = adapter
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    class ChatHolder(v: View) : RecyclerView.ViewHolder(v) {
        var messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        var messengerTextView: TextView = itemView.findViewById(R.id.messengerTextView)
        var messengerImageView: ImageView = itemView.findViewById(R.id.messengerImageView)
        var timestamp: RelativeTimeTextView = itemView.findViewById(R.id.timestamp)
    }

    private fun sendNotif(mUsername: String, pesan: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("Sender", mUsername)
            .add("Message", pesan)
            .build()

        val request = Request.Builder()
            .url("https://firebasefcm.000webhostapp.com/firebasefcm/push_notification.php")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure: Gagal Push Notif ", e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.d(TAG, "onResponse: Berhasil Push Notif$response")
            }
        })
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
