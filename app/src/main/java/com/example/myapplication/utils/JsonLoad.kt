package com.example.myapplication.utils

import android.content.Context
import com.example.myapplication.data.database.Mission
import com.example.myapplication.data.database.Tags
import com.example.myapplication.data.database.TitleBadge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

fun loadTitleBadgesFromRaw(context: Context): List<TitleBadge> {
    return try {
        val inputStream = context.resources.openRawResource(
            com.example.myapplication.R.raw.titles
        )
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<TitleBadge>>() {}.type
        Gson().fromJson<List<TitleBadge>>(reader, type)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun loadTagsFromRaw(context: Context): List<Tags> {
    return try {
        val inputStream = context.resources.openRawResource(
            com.example.myapplication.R.raw.tags
        )
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<Tags>>() {}.type
        Gson().fromJson<List<Tags>>(reader, type)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun loadMissionsFromRaw(context: Context, isGeneral: Boolean): List<Mission> {
    return try {
        val inputStream = if (isGeneral) {
            context.resources.openRawResource(com.example.myapplication.R.raw.mission_general)
        } else {
            context.resources.openRawResource(com.example.myapplication.R.raw.mission_week)
        }
        val reader = InputStreamReader(inputStream)
        val type = object : com.google.gson.reflect.TypeToken<List<Mission>>() {}.type
        val missions = com.google.gson.Gson().fromJson<List<Mission>>(reader, type)

        // Forza il campo "type" in base alla sorgente
        missions.map { mission ->
            mission.copy(type = isGeneral)  // true = generale, false = settimanale
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}