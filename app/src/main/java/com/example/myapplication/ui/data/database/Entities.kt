package com.example.myapplication.ui.data.database


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Request(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "sender") val sender: Int, //id dell'utente richiedente
    @ColumnInfo(name= "rescuers") val rescuers: List<Int>, //list di id dei soccorritori
    @ColumnInfo(name= "difficulty") var difficulty: String,
    @ColumnInfo(name= "peopleRequired") val peopleRequired: Int,

    @ColumnInfo(name= "place") val place: String? = null,
    @ColumnInfo(name= "fotos") var fotos: List<String>, //quale tipo sarebbe meglio usare per questo?
    @ColumnInfo(name= "description") var description: String,
    @ColumnInfo(name = "date") val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name= "completed") var completed: Boolean=false,
)
@Entity
data class Tags(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
)
@Entity(primaryKeys = ["idTags", "idMissionId"])
data class TagsMission(
    //tag necessari per poter dare
    // una mano nel soccorrimento
    val idTags: Int,
    val idMissionId: Int,
)
@Entity(primaryKeys = ["idTags", "idUser"])
data class TagsUser(
    val idTags: Int,
    val idUser: Int
)
@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
    @ColumnInfo(name= "surname") val surname: String,
    @ColumnInfo(name= "email") var email: String,

    @ColumnInfo(name= "age") var age: Int,
    @ColumnInfo(name= "habitation") var habitation: String? = null,
    @ColumnInfo(name= "phoneNumber") var phoneNumber: String? = null,
    @ColumnInfo(name= "password") var password: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
    )
@Entity
data class UserInfo(
    @PrimaryKey val id: Int, //Id dell'utente di riferimento
    @ColumnInfo(name= "activeTitle") var activeTitle: Int,
    @ColumnInfo(name= "possessedTitles") val possessedTitles: List<Int>,
    @ColumnInfo(name= "exp") val exp: Int,
    @ColumnInfo(name= "profileFoto") val profileFoto: String?, //quale tipo sarebbe meglio utilizzare?
)
@Entity(primaryKeys = ["userId", "badgeId"])
data class UserBadgeCrossRef(
    val userId: Int,
    val badgeId: Int
)

@Entity
data class Mission(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
    @ColumnInfo(name= "description") val description: String,
    @ColumnInfo(name= "exp") val exp: Int? = 0,
    @ColumnInfo(name= "titleBadge") val titleBadgeId: Int? = null


)
@Entity(primaryKeys = ["id", "idUser"])
data class generalMissionUser(
    val id: Int, //Id della missione di riferimento
    val idUser: Int,
    @ColumnInfo(name= "progression") var progression: Int=0, //valore di indice di completamento
    @ColumnInfo(name= "active") var active: Boolean=true, //se questa missione è attiva o meno,
    // di defaultinizialmente messa a true


)
@Entity(primaryKeys = ["id", "idUser"])
data class settimanalMissionUser(
    val id: Int, //Id della missione di riferimento
    val idUser: Int,
    @ColumnInfo(name= "progression") var progression: Int=0, //valore di indice di completamento
    @ColumnInfo(name= "active") var active: Boolean=true, //se questa missione è attiva o meno,
    // di default inizialmente messa a true


)

@Entity
data class TitleBadge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
    @ColumnInfo(name= "color") val color: String,
    @ColumnInfo(name= "rarity") val rarity: String,
)