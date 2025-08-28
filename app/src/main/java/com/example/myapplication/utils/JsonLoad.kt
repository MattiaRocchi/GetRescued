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
        Gson().fromJson(reader, type)
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
        Gson().fromJson(reader, type)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
data class MissionJson(
    val name: String,
    val description: String,
    val exp: Int? = 0,
    val titleBadge: Int? = null,
    val requirement: Int,
    val tag: String
)

fun loadMissionsFromRaw(context: Context, isGeneral: Boolean): List<Mission> {
    return try {
        val inputStream = if (isGeneral) {
            context.resources.openRawResource(com.example.myapplication.R.raw.mission_general)
        } else {
            context.resources.openRawResource(com.example.myapplication.R.raw.mission_week)
        }
        val reader = InputStreamReader(inputStream)
        val type = object : TypeToken<List<MissionJson>>() {}.type
        val missionJsons = Gson().fromJson<List<MissionJson>>(reader, type)

        // Converti da MissionJson a Mission, impostando id = 0 per l'autogenerazione
        missionJsons.mapIndexed { index, missionJson ->
            Mission(
                id = 0, // Questo farà sì che Room generi automaticamente l'ID
                name = missionJson.name,
                description = missionJson.description,
                exp = missionJson.exp,
                titleBadgeId = missionJson.titleBadge,
                requirement = missionJson.requirement,
                tag = missionJson.tag,
                type = isGeneral
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}