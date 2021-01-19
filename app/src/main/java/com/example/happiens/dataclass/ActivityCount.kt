package com.example.happiens.dataclass

import android.media.Image

class ActivityCount {
    var activityName :String = String()
    var image : Int = 0
    var countActivity :Int = 0

    constructor(activityName :String , image: Int , countActivity: Int){
        this.activityName =activityName
        this.image = image
        this.countActivity = countActivity

    }
}