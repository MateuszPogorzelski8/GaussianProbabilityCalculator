package com.gaussapp.myapplication

import android.content.Context
import android.content.SharedPreferences

// DARK MODE CLASS; SAVING AND LOADING
class SaveData(context: Context) {
    private var sharedpreferences: SharedPreferences = context.getSharedPreferences("file", Context.MODE_PRIVATE)



    fun setDarkModeState(state: Boolean?){
        val editor = sharedpreferences.edit()
        editor.putBoolean("Dark", state!!)
        editor.apply()
    }

    fun loadDarkModeState(): Boolean?{
        val state = sharedpreferences.getBoolean("Dark", false)
        return state

    }





}