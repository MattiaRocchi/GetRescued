package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.utils.Converters

@Database(
    entities = [
        User::class, Request::class, UserInfo::class,
        Mission::class, TitleBadge::class,
        generalMissionUser::class, settimanalMissionUser::class,
        Tags::class, TagsMission::class, TagsUser::class,
        UserBadgeCrossRef::class, UserPart::class,
    ],
    version = 5,
    exportSchema = true

)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Definisci qui i DAO man mano

    abstract fun userDao(): UserDao

    abstract fun requestDao(): RequestDao

    abstract fun titleBadgeDao(): TitleBadgeDao

}
/*
istanzia
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "rescued-database"
).build()
*/