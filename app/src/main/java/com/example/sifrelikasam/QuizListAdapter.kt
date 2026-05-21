package com.example.sifrelikasam

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sifrelikasam.databinding.QuizItemRecyclerRowBinding

class QuizListAdapter(private val quizModelList : List<QuizModel>, private val completedQuizzes: Set<String>) :
    RecyclerView.Adapter<QuizListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding: QuizItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model : QuizModel, isCompleted: Boolean){
            binding.apply {
                quizTitleText.text = model.title
                quizSubtitleText.text = model.subtitle
                
                if (isCompleted) {
                    quizTimeText.text = "Tamamlandı"
                    quizTimeText.setTextColor(root.context.getColor(R.color.success_green))
                    root.alpha = 0.6f
                    root.setOnClickListener {
                        android.widget.Toast.makeText(root.context, "Bu testi zaten tamamladın!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    quizTimeText.text = model.time + " min"
                    quizTimeText.setTextColor(root.context.getColor(R.color.white))
                    root.alpha = 1.0f
                    root.setOnClickListener {
                        val intent  = Intent(root.context,QuizActivity::class.java)
                        QuizActivity.questionModelList = model.questionList
                        QuizActivity.time = model.time
                        QuizActivity.quizId = model.id
                        root.context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = QuizItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return quizModelList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = quizModelList[position]
        val isCompleted = completedQuizzes.contains(model.id)
        holder.bind(model, isCompleted)
    }
}