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

class SecondActivity : AppCompatActivity() {
    companion object {
        var films: MutableList<Film> = mutableListOf()
    }
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

    private fun clearData () {
        i = 0
        randomNumbers.clear()
        getRandomNumbers()
    }

    private fun connection(id: Int) {
        if (i == 0) {
            "https://api.themoviedb.org/3/discover/movie?api_key=ec7e318de0e8caf8d6d9c6bbac87ed0e&language=en-US&sort_by=popularity.desc&include_adult=${FirstActivity.age}&include_video=false&page=$page&with_genres=$id"
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
                    Picasso.get().load("https://image.tmdb.org/t/p/original${jsonOfFilm.results[randomNumbers[i]].poster_path}").into(FilmPoster_step2)

                if (jsonOfFilm.results[randomNumbers[i]].release_date == "")
                    FilmHeading_step2.text = jsonOfFilm.results[randomNumbers[i]].title
                else
                    FilmHeading_step2.text = "${jsonOfFilm.results[randomNumbers[i]].title} (${jsonOfFilm.results[randomNumbers[i]].release_date})"
                Counter_step2.text = "${films.size}/6"
            }
        } else check()
    }

    fun previousStep(view: View) {
        FirstActivity.ids.clear()
        FirstActivity.age = false
        films.clear()
        val intent = Intent(this, FirstActivity::class.java)
        finish()
        startActivity(intent)
    }

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

    fun addFilm(view: View) {
        "https://api.themoviedb.org/3/movie/${jsonOfFilm.results[randomNumbers[i]].id}/similar?api_key=ec7e318de0e8caf8d6d9c6bbac87ed0e&language=en-US&page=1"
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

    fun didNotLike(view: View) {
        skipFilms.add(jsonOfFilm.results[randomNumbers[i]].title)
        i++
        check()
    }

    fun didNotWatch(view: View) {
        i++
        check()
    }

    fun openOrClose(view: View) {
        if (showDescription)
            closeDescription()
        else
            viewDescription()
    }

    @SuppressLint("SetTextI18n")
    fun viewDescription() {
        Blackout_step2.visibility = View.VISIBLE
        FilmDescription_step2.visibility = View.VISIBLE
        FilmDescription_step2.text = jsonOfFilm.results[randomNumbers[i]].overview
        ViewDescription.text = "close"
        showDescription = true
    }

    @SuppressLint("SetTextI18n")
    fun closeDescription() {
        Blackout_step2.visibility = View.GONE
        FilmDescription_step2.visibility = View.GONE
        ViewDescription.text = "Description"
        showDescription = false
    }
}

@Serializable
data class Result (val page: Int, val results: List<Film>, val total_pages: Int, val total_results: Int)

@Serializable
data class Film(val adult: Boolean, val backdrop_path: String?, val genre_ids: Array<Int>,
                val id: Int, val original_language: String, val original_title: String,
                val overview: String, val popularity: Double, val poster_path: String?,
                val release_date: String = "Film hasn't yet been released", val title: String, val video: Boolean,
                val vote_average: Double, val vote_count: Int)