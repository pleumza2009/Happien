package com.example.happiens.dataclass

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateHelper {

    fun getTimestamp(): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy-HH:mm:ss.SSS", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun checkTodayOrYesterday(date: String): String {
        val inputDate = this.splitTimestampOnlyDate(date)
        val todayDate = this.splitTimestampOnlyDate(this.getTimestamp())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        val format = SimpleDateFormat("dd MMM yyyy")
        val yesterdayDate = format.format(cal.time)

        return when {
            inputDate.equals(todayDate, true) -> {
                "Today, "
            }
            inputDate.equals(yesterdayDate, true) -> {
                "Yesterday, "
            }
            else -> {
                ""
            }
        }
    }

    fun splitTimestampOnlyDate(timestamp: String): String {
        var delimiter = "-"
        val splitTimestamp = timestamp.split(delimiter)
        return splitTimestamp[0] + " " + splitTimestamp[1] + " " + splitTimestamp[2]
    }

    fun splitTimestampOnlyTime(timestamp: String): String {
        var delimiter = "-"
        val splitTimestamp = timestamp.split(delimiter)
        delimiter = "."
        val splitSecond = splitTimestamp[3].split(delimiter)
        return splitSecond[0]
    }

    fun splitTimestamp(timestamp: String): String {
        var delimiter = "-"
        val splitTimestamp = timestamp.split(delimiter)
        delimiter = "."
        val splitSecond = splitTimestamp[3].split(delimiter)
        val newTimestamp = splitTimestamp[0] + " " + splitTimestamp[1] + " " + splitTimestamp[2] +
                ", " + splitSecond[0]
        return newTimestamp
    }

    fun splitTimestampInEditPage(timestamp: String): List<String> {
        var delimiter = "-"
        val splitTimestamp = timestamp.split(delimiter)
        delimiter = ":"
        val splitSecond = splitTimestamp[3].split(delimiter)
        val newTimestampDate = splitTimestamp[0] + " " + splitTimestamp[1] + " " + splitTimestamp[2]
        val newTimestampTime = splitSecond[0] + ":" +splitSecond[1]
        val finalList: List<String> = listOf(newTimestampDate, newTimestampTime)
        return finalList
    }

    fun getDay(): String {
        val dateFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun getDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun getDayAndDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun getTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun getHour(): Int {
        val dateFormat = SimpleDateFormat("HH", Locale.ENGLISH)
        return dateFormat.format(Date()).toInt()
    }

    fun getGreetingQuote(): String {
        var hr = getHour()
        if(hr in 3..12){
            return "Good Morning! I hope today is a good day!"
        } else if (hr in 12..12) {
            return "Lunch Time! Don't forget to eat!"
        } else if (hr in 13..21) {
            return "Good Evening! How are you today?"
        } else {
            return "Time to Sleep! Don't sleep too late"
        }
    }
}
