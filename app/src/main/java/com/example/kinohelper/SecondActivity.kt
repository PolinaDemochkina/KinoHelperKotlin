package com.example.kinohelper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_second.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Second step (Movie preferences)
 * @author Polina Demochkina
 */
class SecondActivity : AppCompatActivity() {
    companion object {
        /**
         * Vector of recommended movies
         */
        var films: MutableList<Film> = mutableListOf()
    }
    /**
     * Vector of movies that the user had already watched
     */
    private var skipFilms: MutableList<String> = mutableListOf()
    private var randomNumbers: MutableList<Int> = mutableListOf()
    private var page: Int = 1
    private var i:Int = 0
    private lateinit var jsonOfFilm: com.example.kinohelper.Result
    private var check: Boolean = false
    private var showDescription = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        FilmDescription_step2.movementMethod = ScrollingMovementMethod()
        getRandomNumbers()
        check()
    }

    /**
     * Function for checking if enough movies have been selected
     */
    private fun check () {
        if (films.size == 6) {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            if (i == 5) {
                page++
                clearData()
            }
            if (FirstActivity.ids.size == 1) {
                connection(FirstActivity.ids[0])
            }
            else if (FirstActivity.ids.size == 2) {
                if (films.size < 3)
                    connection(FirstActivity.ids[0])
                else if (films.size == 3 && !check) {
                    check = true
                    page = 1
                    clearData()
                    connection(FirstActivity.ids[1])
                }
                else
                    connection(FirstActivity.ids[1])
            }
            else if (FirstActivity.ids.size == 3) {
                if (films.size < 2)
                    connection(FirstActivity.ids[0])
                else if (films.size == 2 && !check) {
                    check = true
                    page = 1
                    clearData()
                    connection(FirstActivity.ids[1])
                }
                else if (films.size < 4)
                    connection(FirstActivity.ids[1])
                else if (films.size == 4 && check) {
                    check = false
                    page = 1
                    clearData()
                    connection(FirstActivity.ids[2])
                }
                else
                    connection(FirstActivity.ids[2])
            }
        }
    }

    /**
     * Function for resetting the i and randomNumbers variables
     */
    private fun clearData () {
        i = 0
        randomNumbers.clear()
        getRandomNumbers()
    }

    /**
     * Function for fetching a movie
     * @param id - genre ID
     */
    private fun connection(id: Int) {
        if (i == 0) {
            getString(R.string.Step2_Connection_link, FirstActivity.age.toString(), page, id)
            .httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure ->
                        println(result.getException())
                    is Result.Success -> {
                        jsonOfFilm = Json.decodeFromString(result.get())
                        checkAndShowFilm()
                    }
                }
            }
        } else checkAndShowFilm()
    }

    /**
     * Function for displaying information about a movie
     */
    @SuppressLint("SetTextI18n")
    fun checkAndShowFilm() {
        var flag = false
        for (sFilm in skipFilms) {
            if (sFilm == jsonOfFilm.results[randomNumbers[i]].title) {
                i++
                flag = true
                break
            }
        }

        if (!flag) {
            runOnUiThread {
                if (jsonOfFilm.results[randomNumbers[i]].poster_path == null)
                    FilmPoster_step2.setImageResource(R.drawable.error_poster)
                else
                    Picasso.get().load(getString(R.string.Step2_3_poster_path, jsonOfFilm.results[randomNumbers[i]].poster_path)).into(FilmPoster_step2)

                if (jsonOfFilm.results[randomNumbers[i]].release_date == "")
                    FilmHeading_step2.text = jsonOfFilm.results[randomNumbers[i]].title
                else
                    FilmHeading_step2.text = "${jsonOfFilm.results[randomNumbers[i]].title} (${jsonOfFilm.results[randomNumbers[i]].release_date})"
                Counter_step2.text = "${films.size}/6"
            }
        } else check()
    }

    /**
     * Function for going back to the first step
     */
    fun previousStep(view: View) {
        FirstActivity.ids.clear()
        FirstActivity.age = false
        films.clear()
        val intent = Intent(this, FirstActivity::class.java)
        finish()
        startActivity(intent)
    }

    /**
     * Function for filling the randomNumbers vector
     */
    private fun getRandomNumbers() {
        var flag = false
        var numberOfFilms = 0
        while (numberOfFilms < 5) {
            val number = Random.nextInt(20)
            for (randomNumber: Int in randomNumbers) {
                if (number == randomNumber) {
                    flag = true
                    check()
                    break
                }
            }
            if (!flag) {
                numberOfFilms++
                randomNumbers.add(number)
            }
            else
                flag = false
        }
    }

    /**
     * Function for the "Watched and liked" button; fetches the recommended movie and adds it to the film vector.
     */
    fun addFilm(view: View) {
        getString(R.string.Step2_AddFilm_link, jsonOfFilm.results[randomNumbers[i]].id)
        .httpGet()
        .responseString { request, response, result ->
            when (result) {
                is Result.Failure ->
                    println(result.getException())
                is Result.Success -> {
                    val jsonOfNewFilm = Json.decodeFromString<com.example.kinohelper.Result>(result.get())
                    if (i < jsonOfNewFilm.results.size) {
                        var flag = false
                        for (sFilm in skipFilms) {
                            if (sFilm == jsonOfNewFilm.results[i].title) {
                                flag = true
                                break
                            }
                        }

                        if (!flag) {
                            films.add(jsonOfNewFilm.results[i])
                            skipFilms.add(jsonOfNewFilm.results[i].title)
                        }
                    }

                    i++
                    check()
                }
            }
        }
    }

    /**
     * "Function for the "Watched and disliked" button; adds movie to the blacklist
     */
    fun didNotLike(view: View) {
        skipFilms.add(jsonOfFilm.results[randomNumbers[i]].title)
        i++
        check()
    }

    /**
     * Function for the "Didn't watch" button
     */
    fun didNotWatch(view: View) {
        i++
        check()
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
        Blackout_step2.visibility = View.VISIBLE
        FilmDescription_step2.visibility = View.VISIBLE
        if (jsonOfFilm.results[randomNumbers[i]].overview == "")
            FilmDescription_step2.text = getString(R.string.description)
        else
            FilmDescription_step2.text = jsonOfFilm.results[randomNumbers[i]].overview
        ViewDescription.text = getString(R.string.close)
        showDescription = true
    }

    /**
     * Function for hiding the description of the film
     */
    private fun closeDescription() {
        Blackout_step2.visibility = View.GONE
        FilmDescription_step2.visibility = View.GONE
        ViewDescription.text = getString(R.string.view_description)
        showDescription = false
    }
}

/**
 * Class to parse json response string
 */
@Serializable
data class Result (val page: Int, val results: List<Film>, val total_pages: Int, val total_results: Int)

/**
 * Movie class
 */
@Serializable
data class Film(val adult: Boolean, val backdrop_path: String?, val genre_ids: Array<Int>,
                val id: Int, val original_language: String, val original_title: String,
                val overview: String, val popularity: Double, val poster_path: String?,
                val release_date: String = "Film hasn't yet been released", val title: String, val video: Boolean,
                val vote_average: Double, val vote_count: Int)