package com.example.kinohelper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.github.kittinunf.result.Result

class FirstActivity : AppCompatActivity() {
    private var genres: List<CheckBox>? = null
    companion object {
        var age: Boolean = false
        var ids: MutableList<Int> = mutableListOf()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        genres = listOf (Action, Adventure, Animation, Comedy, Crime,
            Documentary, Drama, Family, Fantasy,
            History, Horror, Mystery, Romance, Science,
            Thriller)
    }

    fun blockCheckBox(view: View) {
        var numberOfCheckMark = 0
        genres?.forEach { genre ->
            if (genre.isChecked) numberOfCheckMark++
        }

        if (numberOfCheckMark == 3) {
            genres?.forEach { genre ->
                if (!genre.isChecked) genre.isEnabled = false
            }
        } else {
            genres?.forEach { genre ->
                genre.isEnabled = true
            }
        }

        NextButton.isEnabled = numberOfCheckMark > 0
    }

    fun nextStep(view: View) {
        age = Age.isChecked
        var data: String?
        "https://api.themoviedb.org/3/genre/movie/list?api_key=ec7e318de0e8caf8d6d9c6bbac87ed0e&language=en-US"
        .httpGet()
        .responseString { _, _, result ->
            when (result) {
                is Result.Failure ->
                    println(result.getException())
                is Result.Success -> {
                    data = result.get()
                    val json = Json.decodeFromString<GenreList>(data.toString())
                    json.genres.forEach { urlGenre ->
                        genres?.forEach { genre ->
                            if (genre.isChecked && genre.text == urlGenre.name)
                                ids.add(urlGenre.id)
                        }
                    }
                    val intent = Intent(this, SecondActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}

@Serializable
data class GenreList(val genres: List<Genre>)

@Serializable
data class Genre (val id: Int, val name: String)