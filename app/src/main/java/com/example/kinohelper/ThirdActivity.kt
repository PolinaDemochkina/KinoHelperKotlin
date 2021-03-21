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

/**
 * Third step (Recommendations)
 * @author Polina Demochkina
 */
class ThirdActivity : AppCompatActivity() {
    private var i: Int = 0
    private var showDescription = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        FilmDescription_step3.movementMethod = ScrollingMovementMethod()
        getFilm()
    }

    /**
     * Function for showing the next movie
     */
    fun nextStep(view: View) {
        i++
        if (i == SecondActivity.films.size)
            i = 0

        getFilm()
    }

    /**
     * Function for showing the previous movie
     */
    fun previousStep(view: View) {
        i--
        if (i < 0)
            i = SecondActivity.films.size - 1

        getFilm()
    }

    /**
     * Function for going back to the first window
     */
    fun home(view: View) {
        FirstActivity.ids.clear()
        FirstActivity.age = false
        SecondActivity.films.clear()
        val intent = Intent(this, FirstActivity::class.java)
        finish()
        startActivity(intent)
    }


    /**
     * Function fot getting a movie from the vector and displaying it
     */
    @SuppressLint("SetTextI18n")
    fun getFilm() {
        if (SecondActivity.films[i].release_date == "")
            FilmHeading_step3.text = SecondActivity.films[i].title
        else
            FilmHeading_step3.text = "${SecondActivity.films[i].title} (${SecondActivity.films[i].release_date})"

        if (SecondActivity.films[i].poster_path == null)
            FilmPoster_step3.setImageResource(R.drawable.error_poster)
        else
            Picasso.get().load(getString(R.string.Step2_3_poster_path, SecondActivity.films[i].poster_path)).into(FilmPoster_step3)
        Counter_step3.text = "${i + 1}/6"
    }

    /**
     * Function for showing or hiding the description of the film
     */
    fun openOrClose(view: View) {
        if (showDescription)
            closeDescription()
        else
            viewDescription()
    }

    /**
     * Function for showing the description of the film
     */
    private fun viewDescription() {
        Blackout_step3.visibility = View.VISIBLE
        FilmDescription_step3.visibility = View.VISIBLE
        if (SecondActivity.films[i].overview == "")
            FilmDescription_step3.text = getString(R.string.description)
        else
            FilmDescription_step3.text = SecondActivity.films[i].overview
        ViewDescription.text = getString(R.string.close)
        showDescription = true
    }

    /**
     * Function for hiding the description of the film
     */
    private fun closeDescription() {
        Blackout_step3.visibility = View.GONE
        FilmDescription_step3.visibility = View.GONE
        ViewDescription.text = getString(R.string.view_description)
        showDescription = false
    }
}