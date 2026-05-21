package com.example.sifrelikasam

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sifrelikasam.databinding.ActivityYolculukDagBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class YolculukDagActivity : AppCompatActivity() {

    lateinit var quizModelList : MutableList<QuizModel>
    lateinit var adapter: QuizListAdapter
    private var completedQuizzes = mutableSetOf<String>()

    private lateinit var binding : ActivityYolculukDagBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityYolculukDagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quizModelList = mutableListOf()
        
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onResume() {
        super.onResume()
        quizModelList.clear()
        completedQuizzes.clear()
        getDataFromFirebase()
    }

    override fun onBackPressed() {
        // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setupRecyclerView(){
        binding.progressBar.visibility = View.GONE
        adapter = QuizListAdapter(quizModelList, completedQuizzes)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseDatabase.getInstance().getReference("CompletedQuizzes").child(user.uid)
                .get()
                .addOnSuccessListener { completedSnapshot ->
                    if (completedSnapshot.exists()) {
                        for (child in completedSnapshot.children) {
                            completedQuizzes.add(child.key ?: "")
                        }
                    }
                    fetchQuizzes()
                }
                .addOnFailureListener {
                    fetchQuizzes()
                }
        } else {
            fetchQuizzes()
        }
    }

    private fun fetchQuizzes() {
        FirebaseDatabase.getInstance().reference.child("Sorular")
            .get()
            .addOnSuccessListener { dataSnapshot->
                if(dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children){
                        val quizModel = snapshot.getValue(QuizModel::class.java)
                        if (quizModel != null) {
                            quizModelList.add(quizModel)
                        }
                    }
                }
                setupRecyclerView()
            }
    }
}