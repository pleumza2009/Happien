package com.example.happiens.dataclass

import android.util.Log
import java.util.*

object FileNameStringHelper {

    fun newFileName(activity: String): String {
        var delimiter = " "
        var returnText = activity.toLowerCase(Locale.getDefault())
        val splitActivity = returnText.split(delimiter)
        returnText = ""
        for (text in splitActivity){
            returnText = returnText + text + "_"
        }
        return returnText
    }
}