package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.utils.Converters


@Database(
    entities = [
        User::class, Request::class, UserInfo::class,
        Mission::class, TitleBadge::class,
        generalMissionUser::class, settimanalMissionUser::class,
        Tags::class, TagsMission::class, TagsUser::class,
        UserBadgeCrossRef::class, UserPart::class,
    ],
    version = 7,
    exportSchema = true

)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Definisci qui i DAO man mano

    abstract fun userDao(): UserDao

    abstract fun requestDao(): RequestDao

    abstract fun titleBadgeDao(): TitleBadgeDao

    abstract fun missionDao(): MissionDao


}
/*
istanzia
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "rescued-database"
).build()
*/