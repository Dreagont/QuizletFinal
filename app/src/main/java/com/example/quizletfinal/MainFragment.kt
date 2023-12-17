package com.example.quizletfinal

import TopicAdapter
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizletfinal.models.Card
import com.example.quizletfinal.models.Folder
import com.example.quizletfinal.models.OnItemClickListener
import com.example.quizletfinal.models.Topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainFragment : Fragment(), OnItemClickListener {
    private lateinit var searchBar: SearchView
    private lateinit var otherTopicList: RecyclerView
    private lateinit var ifNoTopic : LinearLayout
    private lateinit var progressDialog: ProgressDialog
    private val handler = Handler(Looper.getMainLooper())
    var loginUser: String? = ""
    val topics = mutableListOf<Topic>()

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)

        auth = FirebaseAuth.getInstance()

        auth.currentUser?.let { saveUserData(it) }

        searchBar = view.findViewById(R.id.search_bar)
        otherTopicList = view.findViewById(R.id.otherTopicList)
        otherTopicList.layoutManager = LinearLayoutManager(requireContext())
        ifNoTopic = view.findViewById(R.id.ifNoTopic)


        val sharedPreferences = requireActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "No Username")
        loginUser = sharedPreferences.getString("loginUsername","no")

        if (username != null) {
            loadTopic(username)
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                (otherTopicList.adapter as? TopicAdapter)?.filter?.filter(newText)
                return true
            }
        })
    }



    private fun loadTopic(currentUsername: String) {
        progressDialog.show()

        FirebaseDatabase.getInstance().getReference("users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val updatedTopics = mutableListOf<Topic>()

                    dataSnapshot.children.forEach { userSnapshot ->
                        val username = userSnapshot.child("username").value.toString()
                        if (username != currentUsername) {
                            userSnapshot.child("topics").children.forEach { topicSnapshot ->
                                val topic = topicSnapshot.getValue(Topic::class.java)
                                topic?.let {
                                    if (it.visibility.equals("public")) {
                                        updatedTopics.add(it)
                                    }
                                }
                            }
                        }
                    }
                    topics.clear()
                    topics.addAll(updatedTopics)

                    val adapter = TopicAdapter(requireContext(), updatedTopics, this@MainFragment)
                    otherTopicList.adapter = adapter
                    adapter.notifyDataSetChanged()

                    if (topics.isEmpty()) {
                        ifNoTopic.visibility = View.VISIBLE
                    } else {
                        ifNoTopic.visibility = View.GONE
                    }
                    handler.postDelayed({
                        progressDialog.dismiss()
                    }, 2000)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FirebaseData", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }


    private fun saveUserData(firebaseUser: FirebaseUser) {
        val email = firebaseUser.email

        readWithEmail(email) { dataSnapshot ->
            dataSnapshot.children.forEach { userSnapshot ->
                if (userSnapshot.child("email").value.toString() == email) {
                    with(requireActivity().getSharedPreferences("UserDetails", AppCompatActivity.MODE_PRIVATE).edit()) {
                        putString("username", userSnapshot.child("username").value.toString())
                        putString("Email", firebaseUser.email)
                        putString("image", userSnapshot.child("profileImage").value.toString())
                        apply()
                    }
                    return@readWithEmail
                }
            }
        }
    }

    private fun readWithEmail(email: String?, processSnapshot: (DataSnapshot) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    processSnapshot(dataSnapshot)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FirebaseData", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }

    override fun onItemClickListener(topic: Topic) {
        val intent = Intent(requireContext(), TopicActivity::class.java)
        intent.putExtra("topicData", topic)
        intent.putExtra("editable", false)
        intent.putExtra("loginUser",loginUser)
        startActivity(intent)
    }

    override fun onItemClickListener(folder: Folder) {
        TODO("Not yet implemented")
    }

    override fun onItemClickListener(card: Card) {
        TODO("Not yet implemented")
    }

    override fun onItemLongClickListener(card: Card) {
        TODO("Not yet implemented")
    }

    override fun onItemDeleteListener(topic: Topic) {
        TODO("Not yet implemented")
    }

    override fun onItemMoveListener(topic: Topic) {
        TODO("Not yet implemented")
    }

}