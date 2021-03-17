package com.example.kinohelper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_third.*

class ThirdActivity : AppCompatActivity() {
    private var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        getFilm()
    }

    fun nextStep(view: View) {
        i++
        if (i == SecondActivity.films.size)
            i = 0

        getFilm()
    }
    fun previousStep(view: View) {
        i--
        if (i < 0)
            i = SecondActivity.films.size - 1

        getFilm()
    }

    fun home(view: View) {
        FirstActivity.ids.clear()
        FirstActivity.age = false
        SecondActivity.films.clear()
        val intent = Intent(this, FirstActivity::class.java)
        finish()
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n")
    fun getFilm() {
        FilmDescription_step3.text = "${SecondActivity.films[i].title} (${SecondActivity.films[i].release_date})"
        Picasso.get().load("https://image.tmdb.org/t/p/original${SecondActivity.films[i].poster_path}").into(FilmPoster_step3)
        Counter_step3.text = "${i + 1}/6"
    }
}