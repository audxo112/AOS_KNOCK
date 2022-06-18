package com.fleet.knock.info.gallery

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class VideoBundle(val date:Long,
                  val list:List<Video>){

    private val sameYearFormat = SimpleDateFormat("M월 d일", Locale.KOREA)
    private val format = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)

    val dateStr: String
        get() = when {
            DateUtils.isToday(date) -> "오늘"
            DateUtils.isToday(date - DateUtils.DAY_IN_MILLIS) -> "어제"
            isSameYear() -> sameYearFormat.format(date)
            else -> format.format(date)
        }

    private fun isSameYear() : Boolean{
        val today = Calendar.getInstance()
        val cal = Calendar.getInstance()
        cal.timeInMillis = date

        return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
    }
}