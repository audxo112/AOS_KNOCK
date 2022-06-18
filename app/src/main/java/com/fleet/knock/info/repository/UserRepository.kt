package com.fleet.knock.info.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.info.user.User
import com.fleet.knock.info.user.UserConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

open class UserRepository(application: Application) : BaseRepository(application) {
    protected val fa = FirebaseAuth.getInstance()
    protected val userConfigDao = db.userConfigDao()

    val currentUserUid : String
        get() = fa.currentUser.let{user ->
            if(user == null || user.isAnonymous) User.DEFAULT_UID
            else user.uid
        }

    fun getUserConfig() : LiveData<UserConfig?> {
        scope.launch {
            if(userConfigDao.getSync(currentUserUid) == null){
                userConfigDao.insert(UserConfig(currentUserUid))
            }
        }
        return userConfigDao.getLive(currentUserUid)
    }

    suspend fun getUserConfigSync() = userConfigDao.getSync(currentUserUid)

    init{
        scope.launch {
            val config = userConfigDao.getSync(currentUserUid)
            if(config == null){
                userConfigDao.insert(UserConfig(currentUserUid))
            }
        }
    }

    suspend fun removeSelectTheme(){
        userConfigDao.removeSelectTheme(currentUserUid)
    }

    suspend fun restoreSelectTheme(){
        when(backupThemeType){
            UserConfig.SELECTED_THEME_TYPE_PUBLIC-> userConfigDao.selectPublicTheme(currentUserUid, backupPublicThemeId)
            UserConfig.SELECTED_THEME_TYPE_LOCAL-> userConfigDao.selectLocalTheme(currentUserUid, backupLocalThemeId)
            else-> userConfigDao.removeSelectTheme(currentUserUid)
        }
    }

    private var backupThemeType:String = ""
    private var backupPublicThemeId:String = ""
    private var backupLocalThemeId:Long = 0L

    private suspend fun backupSelectedTheme(){
        val config = userConfigDao.getSync(currentUserUid)

        backupThemeType = config?.selectedThemeType ?: ""
        backupPublicThemeId = config?.selectedPublicThemeId ?: ""
        backupLocalThemeId = config?.selectedLocalThemeId ?: 0L
    }

    suspend fun selectPublicTheme(themeId: String){
        backupSelectedTheme()

        userConfigDao.selectPublicTheme(currentUserUid, themeId)
    }

    suspend fun selectLocalTheme(themeId:Long){
        backupSelectedTheme()

        userConfigDao.selectLocalTheme(currentUserUid, themeId)
    }
}