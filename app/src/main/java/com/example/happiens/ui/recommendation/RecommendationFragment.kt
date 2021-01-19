package com.example.happiens.ui.recommendation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import androidx.fragment.app.Fragment
import com.example.happiens.R
import com.example.happiens.dataclass.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_recommendation.*
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RecommendationFragment : Fragment() {

    private var database = FirebaseDatabase.getInstance()
    private var dbRef = database.reference
    var firebaseAuth: FirebaseAuth? =null
    private var authUser: String = ""

    var allUserList = ArrayList<String>()
    var activityCategoriesList = ArrayList<String>()

    private val diff: MutableMap<String, Map<String, Double>> = HashMap()
    private val freq: MutableMap<String, Map<String, Int>> = HashMap()
    private var inputData: MutableMap<Users, HashMap<String, Double>> = HashMap()
    private val outputData: MutableMap<Users, HashMap<String, Double>> = HashMap()

    private var recommendActivities = ShortUserActivity("", 0.0)

        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        authUser = firebaseAuth?.currentUser!!.uid

        getActivityCategoriesList()
        getUsersList()
        getAllUsersActivitiesList()
    }

    private fun createRecommendView(){
        chatWelcome.text = DateHelper.getGreetingQuote()

        if(recommendActivities.actName == ""){
            chatMessage.text = "You don't have any Recommend Activity of the Day."

            (recommendActivity.parent as ViewManager).removeView(recommendActivity)
            chatMessage3.visibility = View.GONE
            (chipGroup.parent as ViewManager).removeView(chipGroup)
        } else {
            actRecName.text = recommendActivities.actName
            val actNameLowerCase = "ic_baseline_" + FileNameStringHelper.newFileName(recommendActivities.actName) + "24"
            val actNameToSource = resources.getIdentifier(
                "com.example.happiens:drawable/$actNameLowerCase",
                null,
                null
            )
            podiumRankIcon1.setImageResource(actNameToSource)

            var accurateGmmUriString = GoogleStringHelper.accurateGmmUriString(recommendActivities.actName)
            var accurateBrowserIntentString = GoogleStringHelper.accurateBrowserIntentString(recommendActivities.actName)
            Log.e("TESTLOG", "accurateGmmUriString = $accurateGmmUriString")
            Log.e("TESTLOG", "accurateBrowserIntentString = $accurateBrowserIntentString")

            buttonToOpenMap2.visibility = View.GONE
            buttonToOpenMap3.visibility = View.GONE
            buttonToOpenBrowser2.visibility = View.GONE

            buttonToOpenMap1.text = accurateGmmUriString[0]
            buttonToOpenMap1.setOnClickListener{
                val gmmIntentUri = Uri.parse("geo:0,0?q=" + accurateGmmUriString[0])
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

            if(accurateGmmUriString.size == 2){
                buttonToOpenMap2.visibility = View.VISIBLE
                buttonToOpenMap2.text = accurateGmmUriString[1]
                buttonToOpenMap2.setOnClickListener{
                    val gmmIntentUri = Uri.parse("geo:0,0?q=" + accurateGmmUriString[1])
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            }

            if (accurateGmmUriString.size == 3){
                buttonToOpenMap3.visibility = View.VISIBLE
                buttonToOpenMap3.text = accurateGmmUriString[2]
                buttonToOpenMap3.setOnClickListener{
                    val gmmIntentUri = Uri.parse("geo:0,0?q=" + accurateGmmUriString[2])
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            }

            buttonToOpenBrowser1.text = GoogleStringHelper.plusToNormalText(accurateBrowserIntentString[0])
            buttonToOpenBrowser1.setOnClickListener{
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.google.com/search?q="+accurateBrowserIntentString[0]))
                startActivity(browserIntent)
            }

            if(accurateBrowserIntentString.size == 2){
                buttonToOpenBrowser2.visibility = View.VISIBLE
                buttonToOpenBrowser2.text = GoogleStringHelper.plusToNormalText(accurateBrowserIntentString[1])
                buttonToOpenBrowser2.setOnClickListener{
                    val browserIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.google.com/search?q="+accurateBrowserIntentString[1]))
                    startActivity(browserIntent)
                }
            }
        }
    }

    private fun getActivityCategoriesList(){
        activityCategoriesList = arrayListOf("Eating","Cooking", "Shopping",
            "Watching", "Reading", "Listening", "Singing", "Exercise", "Play Sports",
            "Gaming", "Traveling", "Playing with Pet", "Photography", "Arts and Crafts")
    }

    private fun getUsersList() {
        val getAllUser = dbRef.child("users")
        getAllUser.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val td = dataSnapshot.value as HashMap<*, *>
                    for (key in td.keys) {
                        allUserList.add(key.toString())
                    }

                } catch (ex: Exception) {
                }
            }
        })
    }

    private fun getAllUsersActivitiesList(){
        val allUserActivityHistory = dbRef.child("activities")
            .orderByChild("userId")
        //.equalTo(authUser)

        allUserActivityHistory.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val td = dataSnapshot.value as HashMap<*, *>
                    val sorted = td.toSortedMap(compareByDescending { it.toString() })

                    var newUser: HashMap<String, Double>

                    for (i in 0 until allUserList.size) {
                        newUser = HashMap()
                        for (key in sorted.keys) {
                            val act = sorted[key] as HashMap<*, *>

                            if (act["userId"].toString() == allUserList[i]) {

                                var actScoreString = "" + act["actScore"]
                                var actScore: Double = actScoreString.toDouble()

                                if (newUser[act["actName"].toString()] != null) {
                                    newUser[act["actName"].toString()] =
                                        (newUser[act["actName"].toString()]!! + actScore) / 2
                                } else {
                                    newUser[act["actName"].toString()] = actScore
                                }
                            }
                        }
                        inputData[Users(allUserList[i])] = newUser
                    }

                    Log.e("TESTLOG", "****************************************")
                    Log.e("TESTLOG", "buildDifferencesMatrix Method Prediction")
                    Log.e("TESTLOG", "****************************************")
                    buildDifferencesMatrix(inputData)
                    Log.e("TESTLOG", "****************************************")
                    Log.e("TESTLOG", "predict Method Prediction")
                    Log.e("TESTLOG", "****************************************")
                    predict(inputData)
                    Log.e("TESTLOG", "****************************************")

                    //adapter!!.notifyDataSetChanged()

                } catch (ex: Exception) {
                }
            }
        })
    }

    private fun buildDifferencesMatrix(data: Map<Users, HashMap<String, Double>>) {
        for (user in data.values) {
            for ((key, value) in user) {
                if (!diff.containsKey(key)) {
                    diff[key] = HashMap()
                    freq[key] = HashMap()
                }
                for ((key1, value1) in user) {
                    var oldCount = 0
                    if (freq[key]!!.containsKey(key1)) {
                        oldCount = freq[key]!!.getValue(key1).toInt()
                    }
                    var oldDiff = 0.0
                    if (diff[key]!!.containsKey(key1)) {
                        oldDiff = diff[key]!!.getValue(key1).toDouble()
                    }

                    val observedDiff = value - value1

                    var forFreq: MutableMap<String, Int> = HashMap()
                    forFreq[key1] = oldCount + 1
                    freq[key] = forFreq

                    var forDiff: MutableMap<String, Double> = HashMap()
                    forDiff[key1] = oldDiff + observedDiff
                    diff[key] = forDiff
                }
            }
        }

        for (j in diff.keys) {
            for (i in diff[j]!!.keys) {
                val oldValue: Double = diff[j]!!.getValue(i).toDouble()
                val count: Int = freq[j]!!.getValue(i).toInt()

                var forDiff: MutableMap<String, Double> = HashMap()
                forDiff[i] = oldValue / count
                diff[j] = forDiff
            }
        }

        /*Log.e("TESTLOG", "diff[] = $diff")
        Log.e("TESTLOG", "freq[] = $freq")*/
        Log.e("TESTLOG", "****************************************")
        Log.e("TESTLOG", "-----------------------------------------")
        Log.e("TESTLOG", "PRINTDATA DATA")
        printData(data)
    }

    private fun predict(data: Map<Users, HashMap<String, Double>>) {

        val uPred = HashMap<String, Double>()
        val uFreq = HashMap<String, Int>()

        for (j in diff.keys) {
            uFreq[j] = 0
            uPred[j] = 0.0
        }

        for ((key, value) in data) {
            Log.e("TESTLOG", "AT USER " + key.email)
            Log.e("TESTLOG", "****************************************")
            for (j in value.keys) {
                for (k in diff.keys) {
                    try {
                        val predictedValue: Double =
                            diff[k]!![j]!!.toDouble() + value[j]!!.toDouble()
                        val finalValue: Double = predictedValue * freq[k]!![j]!!.toInt()
                        uPred[k] = uPred[k]!! + finalValue
                        uFreq[k] = uFreq[k]!! + freq[k]!![j]!!.toInt()
                        Log.e("TESTLOG", "uPred[" + k + "] = " + uPred[k])
                        Log.e("TESTLOG", "uFreq[" + k + "] = " + uFreq[k])
                    } catch (e1: NullPointerException) {
                    }
                }
                val clean = HashMap<String, Double>()
                val formatter: NumberFormat = DecimalFormat("#0.0")
                for (j in uPred.keys) {
                    if (uFreq[j]!! > 0) {
                        var formatNum = formatter.format(uPred[j]!!.toDouble() / uFreq[j]!!.toInt())
                        clean[j] = formatNum.toDouble()
                        Log.e("TESTLOG", "clean[" + j + "] in uPred.keys = " + clean[j])
                    }
                }

                for (j in activityCategoriesList) {
                    if (value.containsKey(j)) {
                        var formatNum = formatter.format(value[j]!!.toDouble())
                        clean[j] = formatNum.toDouble()
                        Log.e("TESTLOG", "clean[" + j + "] in activityList = " + clean[j])
                    } else if (!value.containsKey(j) && key.email.equals(authUser) && clean[j] != null) {
                        if(recommendActivities.actScore < clean[j]!!.toDouble()){
                            var formatNum = formatter.format(clean[j]!!.toDouble())
                            recommendActivities = ShortUserActivity(j, formatNum.toDouble())
                            Log.e(
                                "TESTLOG",
                                "ADD [" + j + "] to recommendActivities = " + clean[j]!!.toDouble()
                            )
                        }
                    } else if (!clean.containsKey(j)) {
                        clean[j] = -1.0
                    }
                }

                if (recommendActivities.actName == ""){
                    for (j in activityCategoriesList) {
                        if (value.containsKey(j) && key.email.equals(authUser) && clean[j] != null) {
                            if(recommendActivities.actScore < clean[j]!!.toDouble()){
                                var formatNum = formatter.format(clean[j]!!.toDouble())
                                recommendActivities = ShortUserActivity(j, formatNum.toDouble())
                                Log.e(
                                    "TESTLOG",
                                    "ADD [" + j + "] to recommendActivities = " + clean[j]!!.toDouble()
                                )
                            }
                        }
                    }
                }

                outputData[key] = clean
                Log.e("TESTLOG", "-----------------------------------------")
            }

            Log.e("TESTLOG", "****************************************")
            Log.e("TESTLOG", "CLEAN OUTPUT DATA FOR " + key.email)
            Log.e("TESTLOG", outputData[key].toString())
            Log.e("TESTLOG", "****************************************")
        }
        Log.e("TESTLOG", "-----------------------------------------")
        Log.e("TESTLOG", "PRINTDATA OUTPUTDATA")
        printData(outputData)
        Log.e("TESTLOG", "-----------------------------------------")
        Log.e("TESTLOG", "MY recommendActivities")
        Log.e("TESTLOG", recommendActivities.actName)

        createRecommendView()
    }

    private fun printData(data: Map<Users, HashMap<String, Double>>) {
        for (user in data.keys) {
            Log.e("TESTLOG", "-----------------------------------------")
            Log.e("TESTLOG", user.email + " :")
            data[user]?.let { printOutByFormat(it) }
        }
    }

    private fun printOutByFormat(hashMap: HashMap<String, Double>) {
        val formatter: NumberFormat = DecimalFormat("#0.0")
        for (j in hashMap.keys) {
            Log.e(
                "TESTLOG",
                "  " + j + " --> " + formatter.format(
                    hashMap.getValue(j).toDouble()
                )
            )
        }
    }

}