package com.example.happiens.dataclass

import android.util.Log

object GoogleStringHelper {

    fun accurateGmmUriString(activity: String ): List<String> {
        var returnArray: List<String>
        when(activity){
            "Eating" -> returnArray = listOf("Restaurants","Takeout","Delivery")
            "Cooking" -> returnArray = listOf("Shopping Centers","Supermarket")
            "Shopping" -> returnArray = listOf("Shopping Centers")
            "Watching" -> returnArray = listOf("Movies")
            "Reading" -> returnArray = listOf("Libraries")
            "Listening" -> returnArray = listOf("Live Music")
            "Singing" -> returnArray = listOf("Karaoke")
            "Exercise" -> returnArray = listOf("Gyms", "Park")
            "Play Sports" -> returnArray = listOf("Gyms", "Park")
            "Gaming" -> returnArray = listOf("Internet Cafe")
            "Traveling" -> returnArray = listOf("Attractions")
            "Playing with Pet" -> returnArray = listOf("Pet Cafe")
            "Photography" -> returnArray = listOf("Attractions","Park")
            "Arts and Crafts" -> returnArray = listOf("Art and Craft Hobby","Art Museums")
            else -> returnArray = listOf("")
        }
        return returnArray
    }

    fun accurateBrowserIntentString(activity: String ): List<String> {
        var returnArray: List<String>
        when(activity){
            "Eating" -> returnArray = listOf("Learn+about+Eating")
            "Cooking" -> returnArray = listOf("Learn+about+Cooking")
            "Shopping" -> returnArray = listOf("Shopping+Benefits")
            "Watching" -> returnArray = listOf("Watching+Hobby","Learn+about+Watching")
            "Reading" -> returnArray = listOf("Reading+Hobby","Learn+about+Reading")
            "Listening" -> returnArray = listOf("Learn+about+Listening")
            "Singing" -> returnArray = listOf("Singing+Hobby","Learn+about+Singing")
            "Exercise" -> returnArray = listOf("Exercise+Hobby","Learn+about+Exercise")
            "Play Sports" -> returnArray = listOf("Play+Sports+Hobby","Learn+about+Play+Sports")
            "Gaming" -> returnArray = listOf("Gaming+Hobby","Learn+about+Gaming")
            "Traveling" -> returnArray = listOf("Traveling+Hobby","Learn+about+Traveling")
            "Playing with Pet" -> returnArray = listOf("Playing+with+Pet+Hobby","Learn+about+Playing+with+Pet")
            "Photography" -> returnArray = listOf("Photography+Hobby","Learn+about+Photography")
            "Arts and Crafts" -> returnArray = listOf("Arts+and+Crafts+Hobby","Learn+about+Arts+and+Crafts")
            else -> returnArray = listOf("")
        }
        return returnArray
    }

    fun plusToNormalText(activity: String): String {
        var delimiter = "+"
        val splitActivity = activity.split(delimiter)
        for (text in splitActivity){
            delimiter = "$delimiter $text"
        }
        return delimiter.substring(2)
    }
}