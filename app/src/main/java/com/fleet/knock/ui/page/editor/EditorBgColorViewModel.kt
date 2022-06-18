package com.fleet.knock.ui.page.editor

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.fleet.knock.utils.viewmodel.BaseViewModel

class EditorBgColorViewModel(application: Application) : BaseViewModel(application){
    val bgColorList = listOf(
        0xffb7dbf1, 0xffa8cfe7, 0xff8bcaf1, 0xff61bdf7, 0xff258dcd, 0xff0a5a8c, 0xff4b8495, 0xff3b6a87, 0xff08456a,
        0xffd7e0ff, 0xffacbffb, 0xff7c99f4, 0xff4669d9, 0xff1f3b93, 0xff102468, 0xff243365, 0xff658ea0, 0xff081a52,
        0xff99d3d3, 0xff86c1c9, 0xff5cb7b8, 0xff39b5b5, 0xff178787, 0xff056464, 0xff326464, 0xff3b5151, 0xff043939,
        /*000000*/  /*000000*/  /*000000*/  0xFF47cc7a, 0xFF629286, 0xFF478471, 0xFF829788, 0xFF8a9895, 0xFF7c9094,
        0xffcde9d8, 0xffbad9c6, 0xff95d4ad, 0xff66ba86, 0xff459062, 0xff196436, 0xff445e4f, 0xff224729, 0xff223125,
        /*000000*/  0xffe6edda, 0xffc9e19f, 0xffa9d45e, 0xff70952e, 0xff5c7b24, 0xff54692e, 0xff293f35, 0xff283611,
        0xfff5ebbc, 0xfff8de87, 0xffe4d590, 0xffe6c946, 0xffb7a348, 0xff998b4e, 0xff665f3d, 0xff5d5016, 0xff504b31,
        0xffe2d1b7, 0xffffd3a4, 0xfffeae6e, 0xfffbb264, 0xffe69846, 0xffb87a37, 0xff8c6c4a, 0xff62380b, 0xff462401,
        0xffd6b6af, 0xffe99987, 0xffc2675a, 0xffe66446, 0xffd15d42, 0xffaf432a, 0xff8a3f2d, 0xff8c7670, 0xff4d0f00,
        /*000000*/  0xffdf9d8f, 0xffc79a93, 0xffde8389, 0xffcb8997, 0xffbf6c85, /*000000*/  /*000000*/  /*000000*/
        0xffffa5a5, 0xffffacc3, /*000000*/  /*000000*/  0xffff6680, 0xffdf397f, 0xffaa0a40, 0xff5d2745, 0xff4a0120,
        0xffe0d7e6, 0xffd7baea, 0xffba8dd6, 0xffa287c1, 0xffbc61f7, 0xff8440b0, 0xff5d3874, 0xff382048, 0xff29083f,
        0xffe3e9f0, 0xffd3d9e8, 0xffc4c1ce, 0xffb4c0ce, 0xff8a8f9c, 0xff708aaa, 0xff59748e, 0xff495a72, 0xff325772,
        0xffffffff, 0xffececec, 0xffcccccc, 0xffa1a1a1, 0xff6c6c6c, 0xff5a5a5a, 0xff3e3e3e, 0xff323232, 0xff000000
    )

    val selectedBgColorIndex = MutableLiveData(bgColorList.size - 1)

    var lastSelectedBgColor = config.getLong(CONFIG_LAST_SELECTED_BG_COLOR, 0)
        set(value){
            field = 0xFFFFFFFF and value
        }

    fun saveLastSelectedBgColor(){
        config.edit().apply {
            putLong(CONFIG_LAST_SELECTED_BG_COLOR, lastSelectedBgColor)
        }.apply()
    }

    val lastSelectedBgColorIndex:Int
        get() = bgColorList.indexOf(lastSelectedBgColor)

    companion object{
        private const val CONFIG_LAST_SELECTED_BG_COLOR = "Config.LastSelectedGgColor"
    }
}