package com.example.kinohelper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_first.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * First step (Genres)
 * @author Polina Demochkina
 */
class FirstActivity : AppCompatActivity() {
    /**
     * All available genres
     */
    private var genres: List<CheckBox>? = null

    companion object {
        var age: Boolean = false

        /**
         * Genre IDs
         */
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

    /**
     * Function to disabling checkboxes after choosing max allowed number of genres
     */
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

    /**
     * Function for sending a request and parsing the result
     */
    fun nextStep(view: View) {
        age = Age.isChecked
        var data: String?
        getString(R.string.Step1_Connection_link)
        .httpGet()
        .responseString { request, response, result ->
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

/**
 * Class to parse json response string
 */
@Serializable
data class GenreList(val genres: List<Genre>)

/**
 * Genre class
 */
@Serializable
data class Genre (val id: Int, val name: String)