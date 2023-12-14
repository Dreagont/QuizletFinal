package com.example.quizletfinal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizletfinal.adapters.FolderAdapter
import com.example.quizletfinal.models.Folder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LibraryFragment : Fragment() {
    private lateinit var folderList: RecyclerView
    private lateinit var ifNoFolder: LinearLayout
    private lateinit var btnOpenAddFoder: Button
    val folders = mutableListOf<Folder>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        folderList = view.findViewById(R.id.folderList)
        ifNoFolder = view.findViewById(R.id.ifNoFolder)
        btnOpenAddFoder = view.findViewById(R.id.btnOpenAddFolder)

        ifNoFolder.visibility = View.GONE

        folderList.layoutManager = LinearLayoutManager(requireContext())

        val sharedPreferences = requireActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", "No Username")
        val email = sharedPreferences.getString("Email", "No Email")

        if (email != null) username?.let { loadFolders(it) }

        btnOpenAddFoder.setOnClickListener {
            activity?.let {
                startActivity(Intent(it, AddFolderActivity::class.java))
            }
        }
    }

    private fun loadFolders(username: String) {
        readFolders(username) { folderSnapshot ->
            folders.clear()

            folderSnapshot.children.forEach { folderDataSnapshot ->
                val folder = folderDataSnapshot.getValue(Folder::class.java)
                folder?.let {
                    folders.add(folder)
                }
            }

            val adapter = FolderAdapter(requireContext(), folders)
            folderList.adapter = adapter
            adapter.notifyDataSetChanged()

            if (folders.isEmpty()) {
                ifNoFolder.visibility = View.VISIBLE
            } else {
                ifNoFolder.visibility = View.GONE
            }
        }
    }

    private fun readFolders(username: String, processSnapshot: (DataSnapshot) -> Unit) {
        FirebaseDatabase.getInstance().getReference("users").child(username)
            .child("folders")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    processSnapshot(dataSnapshot)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FirebaseData", "loadPost:onCancelled", databaseError.toException())
                }
            })
    }
}