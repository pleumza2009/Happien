package com.example.happiens.dataclass

class UserActivity {
    var actId:String = String()
    var userId:String = String()
    var actName:String = String()
    var actScore:Double = 0.0
    var actAltText:String = String()
    var actImg:String = String()
    var timestamp:String = String()

    constructor(actId:String, userId:String, actName:String, actScore:Double, actAltText:String, actImg: String, timestamp:String){
        this.actId = actId
        this.userId = userId
        this.actName = actName
        this.actScore = actScore
        this.actAltText = actAltText
        this.actImg = actImg
        this.timestamp = timestamp
    }

    constructor(actName: String, actScore:Double, timestamp:String){
        this.actName = actName
        this.actScore = actScore
        this.timestamp = timestamp
    }



}