package com.example.mathtrainer

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import org.w3c.dom.Text
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iron = MediaPlayer.create(this, R.raw.iron)
        val click = MediaPlayer.create(this, R.raw.click)
        val finish = MediaPlayer.create(this, R.raw.finish)
        val delete = MediaPlayer.create(this, R.raw.delete)
        val quit = MediaPlayer.create(this, R.raw.quit)

        val start : Button = findViewById(R.id.start)
        val reset : Button = findViewById(R.id.reset)
        var isGame : Boolean = false
        var round : Int = 0

        var result : Int = 0

        var currentScore : Int = 0

        load()

        start.setOnClickListener {
            click.start()
            val image : ImageView = findViewById(R.id.meme)
            image.setImageResource(R.drawable.mt)

            if (isGame == true){
                currentScore += checkAnswer(result)

                val answer : EditText = findViewById(R.id.answer)
                answer.setText("")

                if (round == 4) {
                    finish.start()
                    showResults(currentScore)
                    isGame = false
                    showStart()
                    round = 0
                }
                else {
                    result = generateTask()
                    round++
                }
            }
            else{
                isGame = true
                load()
                showGame()
                currentScore = 0
                result = generateTask()
            }
        }
        reset.setOnClickListener {
            Log.d("ISGAME", isGame.toString())
            iron.start()
            if (isGame == true){
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Are you sure?")
                builder.setMessage("Do you really want to reset your current result?")
                builder.setPositiveButton("YES", { dialog, whichButton ->
                    quit.start()
                    val task : TextView = findViewById(R.id.task)
                    isGame = false
                    task.text = ""
                    showStart()
                    round = 0
                    currentScore = 0
                })
                builder.show()
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Are you sure?")
                builder.setMessage("Do you really want to reset your store and level?")
                builder.setPositiveButton("YES", { dialog, whichButton ->
                    delete.start()
                    resetScore()
                    load()
                })
                builder.show()
                load()
            }
        }

    }

    private fun resetScore(){
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putInt("SCORE", 0)
            putInt("LEVEL", 0)
        }.apply()
    }

    private fun load(){
        val stats : TextView = findViewById(R.id.score)

        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val score = sharedPreferences.getInt("SCORE", 0)
        val level = sharedPreferences.getInt("LEVEL", 0)

        var scoreToShow : Int = 0

        if (level != 0){
            scoreToShow = score / level
        }

        stats.text = "Score: " + scoreToShow.toString() + ", Level " + level.toString()
    }

    private fun generateTask() : Int{
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val level = sharedPreferences.getInt("LEVEL", 0)

        val task : TextView = findViewById(R.id.task)

        var max : Int = 10
        max = Math.pow(max.toDouble(), (level / 4 + 1).toDouble()).toInt()


        var a : Int = (0..max).random()
        var b : Int = (0..max).random()
        var det : Int = (0..level%4).random()

        var correct : Int = 0

        var sgn : Char

        if (det == 0){
            correct = a + b
            sgn = '+'
        }
        else if (det == 1){
            correct = a - b
            sgn = '-'
        }
        else if (det == 2){
            correct = a * b
            sgn = '*'
        }
        else{
            sgn = '/'
            if (b > a){
                var c = a
                a = b
                b = c
            }
            if (b == 0)
                b = 1
            correct = a / b
            b = a / correct
            correct = a / b
        }

        showTask(a, b, sgn)

        return correct
    }

    private fun showTask(a: Int, b: Int, sgn: Char){
        val task : TextView = findViewById(R.id.task)
        task.text = a.toString() + ' ' + sgn + ' ' + b.toString() + " ="
    }

    private fun checkAnswer(expected: Int) : Int{
        val answer : TextView = findViewById(R.id.answer)

        var currentAnswer : String = answer.text.toString()
        if (currentAnswer == expected.toString())
            return 1
        return 0
    }

    private fun showStart(){
        val start : Button = findViewById(R.id.start)
        val reset : Button = findViewById(R.id.reset)

        start.text = "Start game"
        reset.text = "Reset score"
    }

    private fun showGame(){
        val start : Button = findViewById(R.id.start)
        val reset : Button = findViewById(R.id.reset)

        start.text = "Next question"
        reset.text = "Quit"
    }

    private fun showResults(results : Int){
        val task : TextView = findViewById(R.id.task)
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var level = sharedPreferences.getInt("LEVEL", 0)
        var bestScore = sharedPreferences.getInt("BESTSCORE", 0)

        level = level + 1

        showImage(results)

        var oldScore = sharedPreferences.getInt("SCORE", 0)
        var addScore = (results * 100) / 5
        var newScore = (oldScore + addScore)

        task.text = "Your result: " + results.toString() + " / 5"

        editor.apply{
            putInt("SCORE", newScore)
            putInt("LEVEL", level)
        }.apply()

        if(level == 20){
            val showBest : TextView = findViewById(R.id.bestScore)
            showBest.text = "Current result: " + newScore / level + " Best result: " + bestScore.toString()
            if (newScore / level > bestScore){
                bestScore = newScore / level
            }
            resetScore()
        }

        editor.apply{
            putInt("BESTSCORE", bestScore)
        }.apply()
    }

    private fun showImage(res: Int){
        val image : ImageView = findViewById(R.id.meme)
        when(res){
            0 -> {
                image.setImageResource(R.drawable.zero)
            }
            1 -> {
                image.setImageResource(R.drawable.one)
            }
            2 -> {
                image.setImageResource(R.drawable.two)
            }
            3 -> {
                image.setImageResource(R.drawable.three)
            }
            4 -> {
                image.setImageResource(R.drawable.four)
            }
            5 -> {
                image.setImageResource(R.drawable.five)
            }
        }
    }
}