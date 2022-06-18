package com.fleet.knock.utils.admob

import android.app.Application
import com.google.android.gms.ads.*

abstract class ADUtil protected constructor(application: Application, unitIdRes:Int){
    private var interstitialAd: InterstitialAd

//    val deviceId = Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID).let{
//        md5(it).toUpperCase()
//    }
//
//    fun md5(s: String): String {
//        try {
//            // Create MD5 Hash
//            val digest = MessageDigest
//                .getInstance("MD5")
//            digest.update(s.toByteArray())
//            val messageDigest = digest.digest()
//
//            // Create Hex String
//            val hexString = StringBuffer()
//            for (i in messageDigest.indices) {
//                var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
//                while (h.length < 2) h = "0$h"
//                hexString.append(h)
//            }
//            return hexString.toString()
//        } catch (e: NoSuchAlgorithmException) {
//            Log.d("MD5 TEST", "$e")
//        }
//        return ""
//    }

    init{
//        MoPub.initializeSdk(application,
//            SdkConfiguration.Builder(application.getString(R.string.mopub_ad_unit_id)).build(),
//            null)

        MobileAds.initialize(application)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(
                listOf(
                    "723B5AB97908F6E2970F60A373CB5B5B",  // 명석 Note8 Debug
                    "2348C57C6B99C6D0E036CC227CCAF5DB",  // 명석 Note8 Release
                    "FEE7186E2CE77664649C481599FF2462",  // 명석 Note8 AAB
                    "C7629FCBEBE4329F9D2682563C3B1191",  // 정현님 S20 Debug
                    "4F4E06D27E7BD2AE0E7E0FBCB9E7C951",  // 정현님 S20 Release
                    "C230B1901C5AE8A6772DCB553C011302",  // 정현님 S20 AAB
                    "12CCF99668B19D54782FACFF9353037E",  // S6
                    "516D14F37465E69526FE55C3ADBC9323"   // S4
                ))
                .build())

        interstitialAd = InterstitialAd(application)
        interstitialAd.apply {
            adUnitId = application.getString(unitIdRes)
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    loadAd()
                    onAdClosedAfter()
                }

            }
        }

        loadAd()
    }

    private var onAdClosedAfter:()->Unit = {}

    fun loadAd(){
        interstitialAd.loadAd(AdRequest.Builder().build())
    }

    fun requestAd(onAdClosed:()->Unit = {}){
        onAdClosedAfter = onAdClosed

        if (interstitialAd.isLoaded) {
            interstitialAd.show()
        } else {
            onAdClosedAfter()
        }

        loadAd()
    }
}