package com.fleet.knock.ui.page.gallery

import com.fleet.knock.info.gallery.GifImage
import com.fleet.knock.info.gallery.Video

interface GotoEditorDelegate {
    fun gotoEditor(gif:GifImage?)

    fun gotoEditor(video:Video?)
}