package com.example.happiens.ui.result

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happiens.R
import com.example.happiens.databinding.FragmentResultBinding
import com.example.happiens.dataclass.ActivityCount
import com.example.happiens.dataclass.FileNameStringHelper
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*
import kotlin.collections.ArrayList


class ResultFragment : Fragment() {

    data class UserData(val activityName: String, val activityScore: Double, val activityDate: Date)

    var job: Job? = null
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable -> "Error" }
    val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var database = FirebaseDatabase.getInstance()
    private var dbRef = database.reference

    private lateinit var messagesListener: ValueEventListener


    val firebaseAuth = FirebaseAuth.getInstance()
    val authUser = firebaseAuth.currentUser!!.uid
    val filterUserAllrating = dbRef.child("activities")
        .orderByChild("userId")
        .equalTo(authUser)


    //   private lateinit var resultViewModel: ResultViewModel
    private lateinit var binding: FragmentResultBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflater.inflate(R.layout.fragment_result, container, false)
        binding = FragmentResultBinding.inflate(layoutInflater)
        val view = binding.root
        return view


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    lateinit var activity: Context

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context!!
    }


    val monthString = arrayOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    val c = getInstance()
    var intMonthSelected: Int = c.get(Calendar.MONTH)

    var arrayListScore = ArrayList<Double>()
    var arrayListTimeStamp = ArrayList<Date>()
    var arrayListNameAct = ArrayList<String>()
    var arrayUserData = ArrayList<UserData>()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        filterUserAllrating.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {


                val td = dataSnapshot.value as HashMap<*, *>
                val sorted = td.toSortedMap(compareByDescending { it.toString() })


                var Score: Double = 0.0
                var DateTimeStamp: Date?
                var NameAct: String?
                val sdf = SimpleDateFormat("dd-MMM-yyyy-HH:mm:ss.SSS", Locale.ENGLISH)




                for (key in sorted.keys) {
                    val act = td[key] as HashMap<*, *>


                    var getScore = "" + act["actScore"]
                    Score = getScore.toDouble()


                    var getDate = act["timestamp"]
                    var DateTimeStampString = getDate.toString()
                    DateTimeStamp = sdf.parse(DateTimeStampString)


                    var getNameAct = act["actName"]
                    NameAct = getNameAct.toString()

                    arrayListScore.add(Score)
                    arrayListTimeStamp.add(DateTimeStamp)
                    arrayListNameAct.add(NameAct)
                    arrayUserData.add(UserData(NameAct, Score, DateTimeStamp))

                }
                val day = c.get(Calendar.DAY_OF_MONTH)
                val month = c.get(Calendar.MONTH)
                val year = c.get(Calendar.YEAR)

                val monthpicker = binding.monthpicker1
                val previous1 = binding.btnprevious
                val next1 = binding.btnNext

                monthpicker.setOnClickListener {
                    val dpd = DatePickerDialog(
                        activity,
                        android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth,
                        DatePickerDialog.OnDateSetListener { datePicker, year, monthOfYear, dayOfMonth ->
                            //  val monthStringName = c.getDisplayName(month, Calendar.LONG, Locale.getDefault())
                            intMonthSelected = monthOfYear
                            monthpicker.text = monthString[intMonthSelected!!]
                            ActiveActivityWhenSelected()
                        },
                        year,
                        month,
                        day
                    )
                    //show datepicker
                    dpd.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dpd.show()

                    val yearRemove = dpd.findViewById<View>(
                        Resources.getSystem().getIdentifier(
                            "android:id/year",
                            null,
                            null
                        )
                    )
                    if (yearRemove != null) {
                        yearRemove.visibility = View.GONE
                    }

                    val dateRemove = dpd.findViewById<View>(
                        Resources.getSystem().getIdentifier(
                            "android:id/day",
                            null,
                            null
                        )
                    )
                    if (dateRemove != null) {
                        dateRemove.visibility = View.GONE
                    }
                }


                previous1.setOnClickListener {
                    if (intMonthSelected == Calendar.DECEMBER) {
                        next1.setEnabled(true)
                    }

                    if (intMonthSelected > Calendar.JANUARY) {
                        intMonthSelected--
                        monthpicker.text = monthString[intMonthSelected]

                    }
                    if (intMonthSelected == Calendar.JANUARY) {
                        monthpicker.text = monthString[intMonthSelected]
                        previous1.setEnabled(false)
                    }



                    ActiveActivityWhenSelected()
                }


                next1.setOnClickListener {
                    if (intMonthSelected == Calendar.JANUARY) {

                        previous1.setEnabled(true)
                    }

                    if (intMonthSelected < Calendar.DECEMBER) {
                        intMonthSelected++
                        monthpicker.text = monthString[intMonthSelected]

                    }
                    if (intMonthSelected == Calendar.DECEMBER) {
                        monthpicker.text = monthString[intMonthSelected]
                        // disabled
                        next1.setEnabled(false)
                    }

                    ActiveActivityWhenSelected()
                }




                monthpicker.setEnabled(false)

                previous1.setEnabled(false)
                next1.setEnabled(false)

                val calendar = getInstance()
                val lastAndFirstDayOfWeek = ArrayList<String>()
                var result = ""
                calendar.firstDayOfWeek = SUNDAY
                calendar.minimalDaysInFirstWeek = 1
                var firstDayOfWeek = calendar.firstDayOfWeek
                for (i in firstDayOfWeek until firstDayOfWeek + 7) {
                    calendar[DAY_OF_WEEK] = i
                    result = SimpleDateFormat("d MMM").format(calendar.time).toString()
                    lastAndFirstDayOfWeek.add(result)
                    //  Log.d("WEEK", result)
                }
                monthpicker.text =
                    lastAndFirstDayOfWeek.first() + " - " + lastAndFirstDayOfWeek.last()

                happinessByDayChartWeek(arrayListScore, arrayListTimeStamp)
                rankingHappinessActivityWeek(arrayUserData)
                BarTabResultWeek(arrayListScore, arrayListTimeStamp, arrayListNameAct)
                activityCountWeek(arrayUserData)



                binding.SelectedradioGroup.setOnCheckedChangeListener { group, isChecked ->
                    when (isChecked) {
                        R.id.radioSelectWeek -> {
                            happinessByDayChartWeek(arrayListScore, arrayListTimeStamp)
                            BarTabResultWeek(arrayListScore, arrayListTimeStamp, arrayListNameAct)
                            rankingHappinessActivityWeek(arrayUserData)
                            activityCountWeek(arrayUserData)
                            monthpicker.setEnabled(false)
                            previous1.setEnabled(false)
                            next1.setEnabled(false)
                            monthpicker.text =
                                lastAndFirstDayOfWeek.first() + " - " + lastAndFirstDayOfWeek.last()


                        }

                        R.id.radioSelectMonth -> {
                            happinessByDayChartMonth(arrayListScore, arrayListTimeStamp)
                            BarTabResultMonth(arrayListScore, arrayListTimeStamp, arrayListNameAct)
                            rankingHappinessActivityMonth(arrayUserData)
                            activityCountMonth(arrayUserData)
                            monthpicker.setEnabled(true)
                            previous1.setEnabled(true)
                            next1.setEnabled(true)
                            monthpicker.text = monthString[intMonthSelected!!]
                        }
                    }

                }


            }


        })

    }


    fun ActiveActivityWhenSelected() {
        happinessByDayChartMonth(arrayListScore, arrayListTimeStamp)
        rankingHappinessActivityMonth(arrayUserData)
        BarTabResultMonth(arrayListScore, arrayListTimeStamp, arrayListNameAct)
        activityCountMonth(arrayUserData)
    }


    private fun happinessByDayChartWeek(ArrayScore: ArrayList<Double>, ArrayDate: ArrayList<Date>) {
        val radioSelectWeek = binding.radioSelectWeek
        val radioSelectMonth = binding.radioSelectMonth
        radioSelectWeek.isChecked = true
        radioSelectWeek.setTextColor(Color.WHITE)
        radioSelectMonth.setTextColor(Color.BLACK)

        val adhcBarChart = binding.AverageDailyHappyChart


        // XAxis
        val xAxisValue = arrayOf("Sun", "Mon", "Tus", "Wed", "Thu", "Fri", "Sat")


//setting AverageChart
        //yValue
        //getScoredatatoBarChart
        val yValueGroup = ArrayList<BarEntry>()
        var barDataSet: BarDataSet
        barDataSet = BarDataSet(yValueGroup, "")
        var getScore: Double = 0.0

        val resultService = ResultService()

        //set up Day of week for average  Score
        var arrayListSunday = ArrayList<Double>()
        var arrayListMonday = ArrayList<Double>()
        var arrayListTusday = ArrayList<Double>()
        var arrayListWednesday = ArrayList<Double>()
        var arrayListThursday = ArrayList<Double>()
        var arrayListFriday = ArrayList<Double>()
        var arrayListSaturday = ArrayList<Double>()

//set value for averagechart

        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1

        var dayOfWeek: Int?
        var WeekOfYear: Int?
        var currentWeekOfYear: Int?
        currentWeekOfYear = calendar.get(WEEK_OF_YEAR)

//get value of all Chart


        var index = 0
        while (index < ArrayScore.size && index < ArrayDate.size) {


            calendar.time = ArrayDate[index]
            dayOfWeek = calendar.get(DAY_OF_WEEK)

            getScore = ArrayScore[index]
            WeekOfYear = calendar.get(WEEK_OF_YEAR)

            if (currentWeekOfYear == WeekOfYear) {

                //Filter Score by DayOfWeek FOR average
                if (dayOfWeek == SUNDAY) {
                    arrayListSunday.add(getScore)

                } else if (dayOfWeek == MONDAY) {
                    arrayListMonday.add(getScore)
                } else if (dayOfWeek == TUESDAY) {
                    arrayListTusday.add(getScore)
                } else if (dayOfWeek == WEDNESDAY) {
                    arrayListWednesday.add(getScore)
                } else if (dayOfWeek == THURSDAY) {
                    arrayListThursday.add(getScore)
                } else if (dayOfWeek == FRIDAY) {
                    arrayListFriday.add(getScore)
                } else if (dayOfWeek == SATURDAY) {
                    arrayListSaturday.add(getScore)
                }

            }
            index++
        }


//averageChart
//calculate average and setting color for averagechart


        val averageOfSunday = resultService.calculateAverage(arrayListSunday)
        val averageOfMonday = resultService.calculateAverage(arrayListMonday)
        val averageOfTusday = resultService.calculateAverage(arrayListTusday)
        val averageOfWednesday = resultService.calculateAverage(arrayListWednesday)
        val averageOfThursday = resultService.calculateAverage(arrayListThursday)
        val averageOfFriday = resultService.calculateAverage(arrayListFriday)
        val averageOfSatursday = resultService.calculateAverage(arrayListSaturday)


        val allAverageDay = mutableListOf<Float>(
            averageOfSunday,
            averageOfMonday,
            averageOfTusday,
            averageOfWednesday,
            averageOfThursday,
            averageOfFriday,
            averageOfSatursday
        )
        val colors = resultService.settingColorsFromAverageDay(allAverageDay)


        yValueGroup.clear()
        // set up value to Bar
        yValueGroup.add(BarEntry(0.5f, averageOfSunday))
        yValueGroup.add(BarEntry(1.5f, averageOfMonday))
        yValueGroup.add(BarEntry(2.5f, averageOfTusday))
        yValueGroup.add(BarEntry(3.5f, averageOfWednesday))
        yValueGroup.add(BarEntry(4.5f, averageOfThursday))
        yValueGroup.add(BarEntry(5.5f, averageOfFriday))
        yValueGroup.add(BarEntry(6.5f, averageOfSatursday))


        //setDataSetForAverageChart
        barDataSet = BarDataSet(yValueGroup, "")
        barDataSet.colors = colors


//Customize average chart
        barDataSet.setDrawIcons(false)
        barDataSet.setDrawValues(false)
        val barData = BarData(barDataSet)

        adhcBarChart.description.isEnabled = false
        adhcBarChart.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        adhcBarChart.data = barData
        adhcBarChart.barData.barWidth = 0.70f
        adhcBarChart.xAxis.axisMinimum = 0f
        adhcBarChart.xAxis.axisMaximum = 8f
        adhcBarChart.data.isHighlightEnabled = false
        val barChartRender = CustomBarChartRender(
            adhcBarChart,
            adhcBarChart.getAnimator(),
            adhcBarChart.getViewPortHandler()
        )
        barChartRender.setRadius(5)
        adhcBarChart.setRenderer(barChartRender)
        adhcBarChart.invalidate()

        // setAxis
        val xAxis = adhcBarChart.xAxis
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 8f


        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValue)

        xAxis.labelCount = 7
        xAxis.mAxisMaximum = 7f
        xAxis.setCenterAxisLabels(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.spaceMin = 4f
        xAxis.spaceMax = 4f

        adhcBarChart.setVisibleXRangeMaximum(8f)
        adhcBarChart.setVisibleXRangeMinimum(8f)
        adhcBarChart.isDragEnabled = true

        //Y-axis
        adhcBarChart.axisRight.isEnabled = false
        adhcBarChart.setScaleEnabled(false)

        val leftAxis = adhcBarChart.axisLeft
        leftAxis.valueFormatter = LargeValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.setDrawZeroLine(true)
        leftAxis.axisMaximum = 10f
        leftAxis.spaceTop = 0f
        leftAxis.axisMinimum = 0f


        adhcBarChart.data = barData
        adhcBarChart.setVisibleXRange(0f, 7f)

        val legend = adhcBarChart.legend
        legend.isEnabled = false


    }

    private fun happinessByDayChartMonth(
        ArrayScore: ArrayList<Double>,
        ArrayDate: ArrayList<Date>
    ) {
        val radioSelectWeek = binding.radioSelectWeek
        val radioSelectMonth = binding.radioSelectMonth
        radioSelectWeek.setTextColor(Color.BLACK)
        radioSelectMonth.setTextColor(Color.WHITE)

        val adhcBarChart = binding.AverageDailyHappyChart


        // XAxis
        val xAxisValue = arrayOf("Sun", "Mon", "Tus", "Wed", "Thu", "Fri", "Sat")


//setting AverageChart
        //yValue
        //getScoredatatoBarChart
        val yValueGroup = ArrayList<BarEntry>()
        var barDataSet: BarDataSet
        barDataSet = BarDataSet(yValueGroup, "")
        var getScore: Double = 0.0

        val resultService = ResultService()

        //set up Day of week for average  Score
        var arrayListSunday = ArrayList<Double>()
        var arrayListMonday = ArrayList<Double>()
        var arrayListTusday = ArrayList<Double>()
        var arrayListWednesday = ArrayList<Double>()
        var arrayListThursday = ArrayList<Double>()
        var arrayListFriday = ArrayList<Double>()
        var arrayListSaturday = ArrayList<Double>()

//set value for averagechart


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1
        //Locale ควรเปลี่ยนเป็น Locale.getdefault
        //ที่เปลี่ยนไม่ไได้เนื่องจาก timestamp ใน firebase ถูกset timezone  เป็น English

        var dayOfWeek: Int?
        var month: Int?
        var currentMonth: Int?
//get value of all Chart

        //set up current Month
        // calendar.time = Date()
        currentMonth = intMonthSelected

        var index = 0
        while (index < ArrayScore.size && index < ArrayDate.size) {
            calendar.time = ArrayDate[index]
            month = calendar.get(MONTH)

            if (currentMonth == month) {


                dayOfWeek = calendar.get(DAY_OF_WEEK)

                getScore = ArrayScore[index]


                //Filter Score by DayOfWeek FOR average
                if (dayOfWeek == SUNDAY) {
                    arrayListSunday.add(getScore)

                } else if (dayOfWeek == MONDAY) {
                    arrayListMonday.add(getScore)
                } else if (dayOfWeek == TUESDAY) {
                    arrayListTusday.add(getScore)
                } else if (dayOfWeek == WEDNESDAY) {
                    arrayListWednesday.add(getScore)
                } else if (dayOfWeek == THURSDAY) {
                    arrayListThursday.add(getScore)
                } else if (dayOfWeek == FRIDAY) {
                    arrayListFriday.add(getScore)
                } else if (dayOfWeek == SATURDAY) {
                    arrayListSaturday.add(getScore)
                }


            }
            index++
        }


//averageChart
//calculate average and setting color for averagechart


        val averageOfSunday = resultService.calculateAverage(arrayListSunday)
        val averageOfMonday = resultService.calculateAverage(arrayListMonday)
        val averageOfTusday = resultService.calculateAverage(arrayListTusday)
        val averageOfWednesday = resultService.calculateAverage(arrayListWednesday)
        val averageOfThursday = resultService.calculateAverage(arrayListThursday)
        val averageOfFriday = resultService.calculateAverage(arrayListFriday)
        val averageOfSatursday = resultService.calculateAverage(arrayListSaturday)


        val allAverageDay = mutableListOf<Float>(
            averageOfSunday,
            averageOfMonday,
            averageOfTusday,
            averageOfWednesday,
            averageOfThursday,
            averageOfFriday,
            averageOfSatursday
        )
        val colors = resultService.settingColorsFromAverageDay(allAverageDay)


        yValueGroup.clear()
        // set up value to Bar
        yValueGroup.add(BarEntry(0.5f, averageOfSunday))
        yValueGroup.add(BarEntry(1.5f, averageOfMonday))
        yValueGroup.add(BarEntry(2.5f, averageOfTusday))
        yValueGroup.add(BarEntry(3.5f, averageOfWednesday))
        yValueGroup.add(BarEntry(4.5f, averageOfThursday))
        yValueGroup.add(BarEntry(5.5f, averageOfFriday))
        yValueGroup.add(BarEntry(6.5f, averageOfSatursday))


        //setDataSetForAverageChart
        barDataSet = BarDataSet(yValueGroup, "")
        barDataSet.colors = colors

//Customize average chart
        barDataSet.setDrawIcons(false)
        barDataSet.setDrawValues(false)
        val barData = BarData(barDataSet)

        adhcBarChart.description.isEnabled = false
        adhcBarChart.description.textSize = 0f
        barData.setValueFormatter(LargeValueFormatter())
        adhcBarChart.data = barData
        adhcBarChart.barData.barWidth = 0.70f
        adhcBarChart.xAxis.axisMinimum = 0f
        adhcBarChart.xAxis.axisMaximum = 8f
        adhcBarChart.data.isHighlightEnabled = false
        adhcBarChart.setScaleEnabled(false)
        val barChartRender = CustomBarChartRender(
            adhcBarChart,
            adhcBarChart.getAnimator(),
            adhcBarChart.getViewPortHandler()
        )
        barChartRender.setRadius(5)
        adhcBarChart.setRenderer(barChartRender)
        adhcBarChart.invalidate()

        // setAxis
        val xAxis = adhcBarChart.xAxis
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 8f


        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisValue)

        xAxis.labelCount = 7
        xAxis.mAxisMaximum = 7f
        xAxis.setCenterAxisLabels(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.spaceMin = 4f
        xAxis.spaceMax = 4f

        adhcBarChart.setVisibleXRangeMaximum(8f)
        adhcBarChart.setVisibleXRangeMinimum(8f)
        adhcBarChart.isDragEnabled = true

        //Y-axis
        adhcBarChart.axisRight.isEnabled = false
        adhcBarChart.setScaleEnabled(true)

        val leftAxis = adhcBarChart.axisLeft
        leftAxis.valueFormatter = LargeValueFormatter()
        leftAxis.setDrawGridLines(false)
        leftAxis.spaceTop = 0f
        leftAxis.axisMinimum = 0f


        adhcBarChart.data = barData
        adhcBarChart.setVisibleXRange(0f, 7f)

        val legend = adhcBarChart.legend
        legend.isEnabled = false


    }


    private fun BarTabResultWeek(
        ArrayScore: ArrayList<Double>,
        ArrayDate: ArrayList<Date>,
        ArrayName: ArrayList<String>
    ) {


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1

        var WeekOfYear: Int?
        var currentWeekOfYear: Int?
        currentWeekOfYear = calendar.get(WEEK_OF_YEAR)

//get value of all Chart


        val getScoreArray = ArrayList<Double>()
        val getNameArray = ArrayList<String>()
        val getDateArray = ArrayList<Date>()

        var index = 0
        while (index < ArrayScore.size && index < ArrayDate.size) {


            calendar.time = ArrayDate[index]


            WeekOfYear = calendar.get(WEEK_OF_YEAR)

            if (currentWeekOfYear == WeekOfYear) {
                getScoreArray.add(ArrayScore[index])
                getNameArray.add(ArrayName[index])
                getDateArray.add(ArrayDate[index])
            }
            index++
        }


        //TopActivity
        val groupTopAct = getNameArray.groupBy { it }.mapValues { it.value.count() }
        val maxTopAct = groupTopAct.maxBy { it.value }?.key
        binding.TopActivityText.text = maxTopAct.toString()

        //Count Activity
        val countActivity = groupTopAct.maxBy { it.value }?.value
        binding.CountActivityText.text = countActivity.toString()

        //Top Actvity Mood Rating and Record Activity in Day
        val topActAverageRating = ArrayList<Double>()
        val lastrecordTopAct = java.util.ArrayList<Date>()
        var indexlastrecord = 0
        while (indexlastrecord < getNameArray.size) {
            if (getNameArray[indexlastrecord].equals(maxTopAct)) {
                val getScore = getScoreArray[indexlastrecord]
                topActAverageRating.add(getScore)
                val getDate = getDateArray[indexlastrecord]
                lastrecordTopAct.add(getDate)
            }
            indexlastrecord++


        }
        if (topActAverageRating.isNotEmpty()) {
            val moodRating = topActAverageRating.average()
            val moodRating2digit = String.format("%.2f", moodRating)
            binding.moodRatingText.text = moodRating2digit
        } else if (topActAverageRating.isEmpty()) {
            binding.moodRatingText.text = "No data"
            binding.CountActivityText.text = "No data"
            binding.TopActivityText.text = "No data"

        }

        if (lastrecordTopAct.isNotEmpty()) {
            //Record Activity in Day
            val lastRecord = lastrecordTopAct.first()
            val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
            val date = sdf.format(lastRecord)
            binding.lastDateRecord.text = date
        } else if (topActAverageRating.isEmpty()) {
            binding.lastDateRecord.text = "No data"
        }


    }

    private fun BarTabResultMonth(
        ArrayScore: ArrayList<Double>,
        ArrayDate: ArrayList<Date>,
        ArrayName: ArrayList<String>
    ) {


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1

        var month: Int?
        var currentMonth: Int?
        currentMonth = intMonthSelected

//get value of all Chart


        val getScoreArray = ArrayList<Double>()
        val getNameArray = ArrayList<String>()
        val getDateArray = ArrayList<Date>()

        var index = 0
        while (index < ArrayScore.size && index < ArrayDate.size) {


            calendar.time = ArrayDate[index]



            month = calendar.get(MONTH)

            if (currentMonth == month) {
                getScoreArray.add(ArrayScore[index])
                getNameArray.add(ArrayName[index])
                getDateArray.add(ArrayDate[index])
            }
            index++
        }


        //TopActivity
        val groupTopAct = getNameArray.groupBy { it }.mapValues { it.value.count() }
        val maxTopAct = groupTopAct.maxBy { it.value }?.key
        binding.TopActivityText.text = maxTopAct.toString()

        //Count Activity
        val countActivity = groupTopAct.maxBy { it.value }?.value
        binding.CountActivityText.text = countActivity.toString()

        //Top Actvity Mood Rating and Record Activity in Day
        val topActAverageRating = ArrayList<Double>()
        val lastrecordTopAct = java.util.ArrayList<Date>()
        var indexlastrecord = 0
        while (indexlastrecord < getNameArray.size) {
            if (getNameArray[indexlastrecord].equals(maxTopAct)) {
                val getScore = getScoreArray[indexlastrecord]
                topActAverageRating.add(getScore)
                val getDate = getDateArray[indexlastrecord]
                lastrecordTopAct.add(getDate)
            }


            indexlastrecord++


        }

        if (topActAverageRating.isNotEmpty()) {
            val moodRating = topActAverageRating.average()
            val moodRating2digit = String.format("%.2f", moodRating)
            binding.moodRatingText.text = moodRating2digit
        } else if (topActAverageRating.isEmpty()) {
            binding.moodRatingText.text = "No data"
            binding.CountActivityText.text = "No data"
            binding.TopActivityText.text = "No data"

        }

        if (lastrecordTopAct.isNotEmpty()) {
            //Record Activity in Day
            val lastRecord = lastrecordTopAct.first()
            val sdf = SimpleDateFormat("dd MMM", Locale.ENGLISH)
            val date = sdf.format(lastRecord)
            binding.lastDateRecord.text = date
        } else if (topActAverageRating.isEmpty()) {
            binding.lastDateRecord.text = "No data"
        }


    }


    private fun rankingHappinessActivityWeek(userData: ArrayList<UserData>) {

        val ArrayUserData = userData


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1


        var week: Int?
        var currentWeek: Int?
        var month: Int?
        var currentMonth: Int?
        currentMonth = intMonthSelected
        currentWeek = calendar.get(WEEK_OF_YEAR)

        val weekRankingHappinessData = ArrayList<UserData>()

        ArrayUserData.forEach { userData ->
            calendar.time = userData.activityDate

            week = calendar.get(WEEK_OF_YEAR)
            if (currentWeek == week) {

                weekRankingHappinessData.add(
                    UserData(
                        userData.activityName,
                        userData.activityScore,
                        userData.activityDate
                    )
                )
            }

        }


        val weekRankingHappinessAverage = weekRankingHappinessData.groupBy { it.activityName }
            .mapValues { it.value.map { userData -> userData.activityScore }.average() }.toList()
            .sortedBy { (key, value) -> value }.asReversed()
        // Log.d("Average", weekRankingHappinessAverage.toString())


        val podiumArray = ArrayList<String>()
        val ScoreArray = ArrayList<Double>()

        weekRankingHappinessAverage.forEach { userData ->
            podiumArray.add(userData.first)
            val number2digits: Double = String.format("%.2f", userData.second).toDouble()
            ScoreArray.add(number2digits)
            //  Log.d("Average", userData.first+ " : "+userData.second+currentMonth.toString()+"/n")
        }

        showRanking(binding, podiumArray, ScoreArray)

    }

    private fun rankingHappinessActivityMonth(userData: ArrayList<UserData>) {

        val ArrayUserData = userData

        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1


        var month: Int?
        var currentMonth: Int?
        currentMonth = intMonthSelected


        val weekRankingHappinessData = ArrayList<UserData>()

        ArrayUserData.forEach { userData ->
            calendar.time = userData.activityDate
            month = calendar.get(MONTH)

            if (currentMonth == month) {

                weekRankingHappinessData.add(
                    UserData(
                        userData.activityName,
                        userData.activityScore,
                        userData.activityDate
                    )
                )
            }
        }


        val weekRankingHappinessAverage = weekRankingHappinessData.groupBy { it.activityName }
            .mapValues { it.value.map { userData -> userData.activityScore }.average() }.toList()
            .sortedBy { (key, value) -> value }.asReversed()
        // Log.d("Average", weekRankingHappinessAverage.toString())


        val podiumArray = ArrayList<String>()
        val ScoreArray = ArrayList<Double>()
        podiumArray.clear()
        ScoreArray.clear()
        weekRankingHappinessAverage.forEach { userData ->
            podiumArray.add(userData.first)
            val number2digits: Double = String.format("%.2f", userData.second).toDouble()
            ScoreArray.add(number2digits)
        }

        showRanking(binding, podiumArray, ScoreArray)


    }

    private fun showRanking(
        binding: FragmentResultBinding,
        podiumArray: ArrayList<String>,
        ScoreArray: ArrayList<Double>
    ) {

        var podiumranktext1 = binding.podiumranktext1
        var podiumRankIcon1 = binding.podiumRankIcon1
        var picRankList1 = binding.picRankList1
        var rankList1 = binding.rankList1
        var scoreList1 = binding.scoreList1
        var linearLayoutRank1 = binding.linearLayoutRank1
        podiumranktext1.isVisible = false
        podiumRankIcon1.isVisible = false
        linearLayoutRank1.visibility = View.GONE


        var podiumranktext2 = binding.podiumranktext2
        var podiumRankIcon2 = binding.podiumRankIcon2
        var picRankList2 = binding.picRankList2
        var rankList2 = binding.rankList2
        var scoreList2 = binding.scoreList2
        var linearLayoutRank2 = binding.linearLayoutRank2
        podiumranktext2.isVisible = false
        podiumRankIcon2.isVisible = false
        linearLayoutRank2.visibility = View.GONE

        var podiumranktext3 = binding.podiumranktext3
        var podiumRankIcon3 = binding.podiumRankIcon3
        var picRankList3 = binding.picRankList3
        var rankList3 = binding.rankList3
        var scoreList3 = binding.scoreList3
        var linearLayoutRank3 = binding.linearLayoutRank3
        podiumranktext3.isVisible = false
        podiumRankIcon3.isVisible = false
        linearLayoutRank3.visibility = View.GONE

        var picRankList4 = binding.picRankList4
        var rankList4 = binding.rankList4
        var scoreList4 = binding.scoreList4
        var linearLayoutRank4 = binding.linearLayoutRank4
        linearLayoutRank4.visibility = View.GONE

        var picRankList5 = binding.picRankList5
        var rankList5 = binding.rankList5
        var scoreList5 = binding.scoreList5
        var linearLayoutRank5 = binding.linearLayoutRank5
        linearLayoutRank5.visibility = View.GONE

        var index = 0
        while (index < podiumArray.size) {

            val actNameLowerCase =
                "ic_baseline_" + FileNameStringHelper.newFileName(podiumArray[index]) + "24"
            val actNameToSource = activity.resources.getIdentifier(
                "com.example.happiens:drawable/$actNameLowerCase",
                null,
                null
            )

            if (index == 0) {
                podiumranktext1.isVisible = true
                podiumRankIcon1.isVisible = true
                linearLayoutRank1.visibility = View.VISIBLE

                podiumranktext1.text = podiumArray[index]
                podiumRankIcon1.setImageResource(actNameToSource)
                picRankList1.setImageResource(actNameToSource)
                rankList1.text = podiumArray[index]
                scoreList1.text = ScoreArray[index].toString()
            }

            if (index == 1) {
                podiumranktext2.isVisible = true
                podiumRankIcon2.isVisible = true
                linearLayoutRank2.visibility = View.VISIBLE

                podiumranktext2.text = podiumArray[index]
                podiumRankIcon2.setImageResource(actNameToSource)
                picRankList2.setImageResource(actNameToSource)
                rankList2.text = podiumArray[index]
                scoreList2.text = ScoreArray[index].toString()
            }

            if (index == 2) {
                podiumranktext3.isVisible = true
                podiumRankIcon3.isVisible = true
                linearLayoutRank3.visibility = View.VISIBLE

                podiumranktext3.text = podiumArray[index]
                podiumRankIcon3.setImageResource(actNameToSource)
                picRankList3.setImageResource(actNameToSource)
                rankList3.text = podiumArray[index]
                scoreList3.text = ScoreArray[index].toString()
            }

            if (index == 3) {
                linearLayoutRank4.visibility = View.VISIBLE

                picRankList4.setImageResource(actNameToSource)
                rankList4.text = podiumArray[index]
                scoreList4.text = ScoreArray[index].toString()
            }

            if (index == 4) {
                linearLayoutRank5.visibility = View.VISIBLE

                picRankList5.setImageResource(actNameToSource)
                rankList5.text = podiumArray[index]
                scoreList5.text = ScoreArray[index].toString()
            }
            index++
        }
    }


    fun activityCountWeek(userData: ArrayList<UserData>) {
        val ArrayUserData = userData
        val arrayActivityCount = ArrayList<ActivityCount>()


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1


        var week: Int?
        var currentWeek: Int?
        var month: Int?
        var currentMonth: Int?
        currentMonth = intMonthSelected
        currentWeek = calendar.get(WEEK_OF_YEAR)

        val weekActivityCountData = ArrayList<UserData>()

        ArrayUserData.forEach { userData ->
            calendar.time = userData.activityDate

            week = calendar.get(WEEK_OF_YEAR)
            if (currentWeek == week) {

                weekActivityCountData.add(
                    UserData(
                        userData.activityName,
                        userData.activityScore,
                        userData.activityDate
                    )
                )
            }

        }


        val weekActivityCount = weekActivityCountData.groupBy { it.activityName }
            .mapValues { it.value.map { userData -> userData.activityScore }.count() }.toList()
            .sortedBy { (key, value) -> value }.asReversed()


        weekActivityCount.forEach { it ->
            val actNameLowerCase =
                "ic_baseline_" + FileNameStringHelper.newFileName(it.first) + "24"
            val actNameToSource = activity.resources.getIdentifier(
                "com.example.happiens:drawable/$actNameLowerCase",
                null,
                null
            )


            arrayActivityCount.add(ActivityCount(it.first, actNameToSource, it.second))

        }
        showRecyclerView(arrayActivityCount)
        arrayActivityCount.forEach{it ->

        }


    }

    fun activityCountMonth(userData: ArrayList<UserData>) {
        val ArrayUserData = userData
        val arrayActivityCount = ArrayList<ActivityCount>()


        var calendar = getInstance()
        calendar.firstDayOfWeek = SUNDAY
        calendar.minimalDaysInFirstWeek = 1


        var month: Int?
        var currentMonth: Int?
        currentMonth = intMonthSelected

        val weekActivityCountData = ArrayList<UserData>()

        ArrayUserData.forEach { userData ->
            calendar.time = userData.activityDate

            month = calendar.get(MONTH)
            if (currentMonth == month) {

                weekActivityCountData.add(
                    UserData(
                        userData.activityName,
                        userData.activityScore,
                        userData.activityDate
                    )
                )
            }

        }


        val weekActivityCount = weekActivityCountData.groupBy { it.activityName }
            .mapValues { it.value.map { userData -> userData.activityScore }.count() }.toList()
            .sortedBy { (key, value) -> value }.asReversed()


        weekActivityCount.forEach { it ->
            val actNameLowerCase =
                "ic_baseline_" + FileNameStringHelper.newFileName(it.first) + "24"
            val actNameToSource = activity.resources.getIdentifier(
                "com.example.happiens:drawable/$actNameLowerCase",
                null,
                null
            )


            arrayActivityCount.add(ActivityCount(it.first, actNameToSource, it.second))

        }
        showRecyclerView(arrayActivityCount)
        arrayActivityCount.forEach{it ->

        }
    }


    fun showRecyclerView(arrayAC: ArrayList<ActivityCount>) {


        var recyclerView: RecyclerView? = null
        var gridLayoutManager: GridLayoutManager? = null
        var activityCountAdapter: ActivityCountAdapter? = null
        var arrayList : ArrayList<ActivityCount>
        arrayList = arrayAC

        recyclerView = binding.activityCountRecyclerView
        gridLayoutManager = GridLayoutManager(activity.applicationContext,4,LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(false)
        recyclerView.isNestedScrollingEnabled=false
        activityCountAdapter = ActivityCountAdapter(activity.applicationContext,arrayList)
        recyclerView?.adapter = activityCountAdapter



    }


}





















