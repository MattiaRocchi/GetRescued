package com.example.myapplication.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.utils.Converters
import com.example.myapplication.ui.profile.loadTitlesFromJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class, Request::class, UserInfo::class,
        Mission::class, TitleBadge::class,
        generalMissionUser::class, settimanalMissionUser::class,
        Tags::class, TagsMission::class, TagsUser::class,
        UserBadgeCrossRef::class, UserPart::class,
    ],
    version = 6,
    exportSchema = true

)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    // Definisci qui i DAO man mano

    abstract fun userDao(): UserDao

    abstract fun requestDao(): RequestDao

    abstract fun titleBadgeDao(): TitleBadgeDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rescued-database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Quando il DB viene creato → inserisco i TitleBadge
                            CoroutineScope(Dispatchers.IO).launch {
                                val titles = loadTitlesFromJson(context) // <-- funzione helper che legge il JSON in assets
                                getDatabase(context).titleBadgeDao().insertAll(titles)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
/*
istanzia
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "rescued-database"
).build()
*/