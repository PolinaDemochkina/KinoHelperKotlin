package com.example.kinohelper

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    var skipFilms: MutableList<String> = mutableListOf()
    var randomNumbers: MutableList<Int> = mutableListOf()
    var page: Int = 1
    var i:Int = 0
    lateinit var jsonOfFilm: com.example.kinohelper.Result

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        getRandomNumbers()
        check()
    }

    fun check () {
        if (films.size == 6) {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            if (i == 5) {
                page++
                i = 0
                randomNumbers.clear()
                getRandomNumbers()
            }
            if (FirstActivity.ids.size == 1) {
                connection(FirstActivity.ids[0]);
            } else if (FirstActivity.ids.size == 2) {
                if (films.size < 3)
                    connection(FirstActivity.ids[0]);
                else
                    connection(FirstActivity.ids[1]);
            } else if (FirstActivity.ids.size == 3) {
                if (films.size < 2)
                    connection(FirstActivity.ids[0]);
                else if (films.size < 4)
                    connection(FirstActivity.ids[1]);
                else
                    connection(FirstActivity.ids[2]);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun connection(id: Int) {
        if (i == 0) {
            var js: String? = null
            "https://api.themoviedb.org/3/discover/movie?api_key=ec7e318de0e8caf8d6d9c6bbac87ed0e&language=en-US&sort_by=popularity.desc&include_adult=${FirstActivity.age}&include_video=false&page=$page&with_genres=$id"
            .httpGet()
            .responseString { request, response, result ->
                when (result) {
                    is Result.Failure ->
                        println(result.getException())
                    is Result.Success ->
                        js = result.get()
                }
            }

            while (js == null)
                Thread.sleep(50)

            jsonOfFilm = Json.decodeFromString<com.example.kinohelper.Result>(js.toString())
        }

        var flag: Boolean = false
        for (sFilm in skipFilms) {
            if (sFilm == jsonOfFilm.results[randomNumbers[i]].title) {
                i++
                flag = true
                break
            }
        }

        if (!flag) {
            if (jsonOfFilm.results[randomNumbers[i]].poster_path == null)
                FilmPoster_step2.setImageResource(R.drawable.error_poster)
            else
                Picasso.get().load("https://image.tmdb.org/t/p/original${jsonOfFilm.results[randomNumbers[i]].poster_path}").into(FilmPoster_step2)

            FilmDescription_step2.text = "${jsonOfFilm.results[randomNumbers[i]].title} (${jsonOfFilm.results[randomNumbers[i]].release_date})"
            Counter_step2.text = "${films.size}/6"
        }
    }

    fun previousStep(view: View) {
        FirstActivity.ids.clear()
        FirstActivity.age = false
        val intent = Intent(this, FirstActivity::class.java)
        finish()
        startActivity(intent)
    }

    fun getRandomNumbers() {
        var flag: Boolean = false
        var numberOfFilms = 0
        while (numberOfFilms < 5) {
            val number = Random.nextInt(20)
            for (randomNumber: Int in randomNumbers) {
                if (number == randomNumber) {
                    flag = true
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
        var js: String? = null
        "https://api.themoviedb.org/3/movie/${jsonOfFilm.results[randomNumbers[i]].id}/similar?api_key=ec7e318de0e8caf8d6d9c6bbac87ed0e&language=en-US&page=1"
                .httpGet()
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure ->
                            println(result.getException())
                        is Result.Success ->
                            js = result.get()
                    }
                }

        while (js == null)
            Thread.sleep(50)

        val jsonOfNewFilm = Json.decodeFromString<com.example.kinohelper.Result>(js.toString())
        if (i < jsonOfNewFilm.results.size) {
            var flag: Boolean = false
            for (sFilm in skipFilms) {
                if (sFilm == jsonOfNewFilm.results[i].title) {
                    i++
                    flag = true
                    break
                }
            }

            if (!flag) {
                films.add(jsonOfNewFilm.results[i])
            }
        }

        i++
        check()
    }

    fun didNotLike(view: View) {
        skipFilms.add(jsonOfFilm.results.get(randomNumbers[i]).title)
        i++
        check()
    }

    fun didNotWatch(view: View) {
        i++
        check()
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