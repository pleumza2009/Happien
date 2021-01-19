package com.example.happiens.ui.result

import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultService {

    fun filterDayOfWeek(){

    }




     fun  calculateAverage(arrayListDay: ArrayList<Double>): Float {

         var averageResult = arrayListDay.average()
         var Result = averageResult.toFloat()


         return Result



    }

    fun settingColorsFromAverageDay (allAverageDay :MutableList<Float>): MutableList<Int> {
            var colors = mutableListOf<Int>()

            for (item in allAverageDay) {

                if (item >= 0.0f && item <= 2.5f) {
                    colors.add(Color.parseColor("#B9EFFF"))
                } else if (item >= 2.5f && item <= 5.0f) {

                    colors.add(Color.parseColor("#F7F719"))
                } else if (item >= 5.0f && item <= 7.5f) {

                    colors.add(Color.parseColor("#ADFF2F"))
                } else if (item >= 7.5f && item <= 10.0f) {

                    colors.add(Color.parseColor("#00CE1B"))
                } else {
                    colors.add(Color.RED)
                }
            }

            return colors

    }



    fun FilterScorebyDayOfWeekForAverage (arrayListDay: ArrayList<Double>) :  ArrayList<Double>{

        return  arrayListDay
    }



}