package com.fleet.knock.utils.viewmodel

import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.fleet.knock.R
import com.google.firebase.firestore.FirebaseFirestore

class CheckTextViewModelHolder(private val minLength:Int,
                               private val collection:String,
                               private val field:String,
                               private val mismatchError:String){
    val text = MutableLiveData<String>()
    val check = MutableLiveData<Boolean>(false)
    val checkRes = Transformations.map(check){
        if(it) R.drawable.ic_account_checked else R.drawable.ic_account_unchecked
    }
    private val checkProgress = MutableLiveData<Boolean>(false)
    val checkVisibility = Transformations.map(checkProgress) {
        if (it) View.INVISIBLE else View.VISIBLE
    }
    val progressVisibility = Transformations.map(checkProgress){
        if (it) View.VISIBLE else View.INVISIBLE
    }
    val wrongMessage = MutableLiveData<String>()
    val openWrongMessage = Transformations.map(wrongMessage){
        (wrongMessage.value?.length?:0 > 0).also { show ->
            if(show){
                handler.postDelayed({
                    wrongMessage.value = ""
                },
                    WRONG_MESSAGE_HIDE_DELAY
                )
            }
        }
    }
    private val handler = Handler()

    private val checkCallback = Runnable {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .whereEqualTo(field, text.value).get()
            .addOnCompleteListener{
                if(it.isSuccessful){
                    (it.result?.isEmpty ?: false).also{c ->
                        check.value = c
                        if(!c){
                            wrongMessage.value = mismatchError
                        }
                    }
                }
                else{
                    check.value = false
                    wrongMessage.value = "서버와 연결에 실패 했습니다"
                }
                checkProgress.value = false
            }
    }

    val onChanged = TextViewBindingAdapter.OnTextChanged { text, _, _, _ ->
        check.value = false
        if(text.length >= minLength) {
            checkProgress.value = true

            handler.removeCallbacks(checkCallback)
            handler.postDelayed(checkCallback,
                WAIT_FOR_CHECK
            )
        }
        else{
            checkProgress.value = false
            handler.removeCallbacks(checkCallback)
        }
    }

    object Bind{
        @JvmStatic
        @BindingAdapter("openMessage")
        fun openMessage(view: TextView, open:Boolean){
            val root = view.parent as ConstraintLayout
            ConstraintSet().apply {
                clone(root)
                if(open){
                    constrainHeight(view.id, ConstraintSet.WRAP_CONTENT)
                }
                else{
                    constrainHeight(view.id, 0)
                }
                applyTo(root)
            }

            val trans = ChangeBounds().apply{
                interpolator = AccelerateInterpolator()
            }
            TransitionManager.beginDelayedTransition(root, trans)
        }
    }

    companion object{
        const val WRONG_MESSAGE_HIDE_DELAY = 1500L
        const val WAIT_FOR_CHECK = 1000L
    }
}