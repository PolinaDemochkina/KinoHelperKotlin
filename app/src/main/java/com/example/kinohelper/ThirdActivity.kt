package com.example.kinohelper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_third.*
import kotlinx.android.synthetic.main.activity_third.Blackout_step3
import kotlinx.android.synthetic.main.activity_third.FilmDescription_step3
import kotlinx.android.synthetic.main.activity_third.ViewDescription

class ThirdActivity : AppCompatActivity() {
    private var i: Int = 0
    private var showDescription = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        FilmDescription_step3.movementMethod = ScrollingMovementMethod()
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
        FilmHeading_step3.text = "${SecondActivity.films[i].title} (${SecondActivity.films[i].release_date})"
        if (SecondActivity.films[i].poster_path == null)
            FilmPoster_step3.setImageResource(R.drawable.error_poster)
        else
            Picasso.get().load("https://image.tmdb.org/t/p/original${SecondActivity.films[i].poster_path}").into(FilmPoster_step3)
        Counter_step3.text = "${i + 1}/6"
    }

    fun openOrClose(view: View) {
        if (showDescription)
            closeDescription()
        else
            viewDescription()
    }

    @SuppressLint("SetTextI18n")
    fun viewDescription() {
        Blackout_step3.visibility = View.VISIBLE
        FilmDescription_step3.visibility = View.VISIBLE
        FilmDescription_step3.text = SecondActivity.films[i].overview
        ViewDescription.text = "close"
        showDescription = true
    }

    @SuppressLint("SetTextI18n")
    fun closeDescription() {
        Blackout_step3.visibility = View.GONE
        FilmDescription_step3.visibility = View.GONE
        ViewDescription.text = "Description"
        showDescription = false
    }
}