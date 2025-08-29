package com.example.myapplication.data.database


import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.ui.theme.LocalTitleColors
import com.example.myapplication.ui.theme.TitleColors
import java.time.LocalDate
import java.time.ZoneId




@Entity
data class Request(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "title") val title: String,
    @ColumnInfo(name= "sender") val sender: Int, //id dell'utente richiedente
    @ColumnInfo(name= "rescuers") val rescuers: List<Int>, //list di id dei soccorritori
    @ColumnInfo(name= "difficulty") var difficulty: String,
    @ColumnInfo(name= "peopleRequired") val peopleRequired: Int,

    @ColumnInfo(name= "place") val place: String? = null,
    @ColumnInfo(name= "fotos") var fotos: List<String>, //quale tipo sarebbe meglio usare per questo?
    @ColumnInfo(name= "description") var description: String,
    @ColumnInfo(name = "date") val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "scheduledDate") val scheduledDate: Long,
    @ColumnInfo(name= "completed") var completed: Boolean=false,
){
    // Utility functions per gestire le date e stati
    fun isScheduledForToday(): Boolean {
        val today = LocalDate.now()
        val scheduled = java.time.Instant.ofEpochMilli(scheduledDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return scheduled == today
    }

    fun isScheduledForTomorrow(): Boolean {
        val tomorrow = LocalDate.now().plusDays(1)
        val scheduled = java.time.Instant.ofEpochMilli(scheduledDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return scheduled == tomorrow
    }

    fun isScheduledInPast(): Boolean {
        val today = LocalDate.now()
        val scheduled = java.time.Instant.ofEpochMilli(scheduledDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return scheduled.isBefore(today)
    }

    fun canBeDeleted(): Boolean {
        // Può essere eliminata solo se è programmata per dopo domani
        val tomorrow = LocalDate.now().plusDays(1)
        val scheduled = java.time.Instant.ofEpochMilli(scheduledDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        return scheduled.isAfter(tomorrow)
    }

    fun canBeModified(): Boolean {
        // Può essere modificata solo se è programmata per dopo domani
        return canBeDeleted()
    }

    fun getRequestState(): String {
        return when {
            isScheduledInPast() -> "Scaduta"
            isScheduledForToday() -> "In corso"
            isScheduledForTomorrow() -> "In preparazione"
            else -> "Programmata"
        }
    }

    fun getAvailableActions(): List<String> {
        return when {
            isScheduledInPast() -> listOf("complete")
            isScheduledForToday() -> listOf("complete")
            isScheduledForTomorrow() -> emptyList()
            else -> listOf("edit", "delete", "manage_participants")
        }
    }
}


@Entity(primaryKeys = ["requestId", "userId"])
data class PendingRequest(
    val requestId: Int,
    val userId: Int,
    @ColumnInfo(name = "requested_at") val requestedAt: Long = System.currentTimeMillis()
)

@Entity(primaryKeys = ["idUser", "idMissionId"])
data class UserPart(
    //tag necessari per poter dare
    // una mano nel soccorrimento
    val idUser: Int,
    val idMissionId: Int,
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
@Entity(primaryKeys = ["idTags", "idRequest"])
data class TagsRequest(
    val idTags: Int,
    val idRequest: Int
)
@Entity(primaryKeys = ["idTags", "idUser"])
data class TagsUser(
    val idTags: Int,
    val idUser: Int
)
@Entity(
    tableName = "User",
    indices = [Index(value = ["email"], unique = true)] // Email univoca
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
    @ColumnInfo(name= "surname") val surname: String,
    @ColumnInfo(name= "email") var email: String,
    @ColumnInfo(name= "password") var password: String,

    @ColumnInfo(name= "age") var age: Int,
    @ColumnInfo(name= "habitation") var habitation: String? = null,
    @ColumnInfo(name= "phoneNumber") var phoneNumber: String? = null,

    //data di creazione dell'account
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
    )
@Entity
data class UserInfo(
    @PrimaryKey val id: Int, //Id dell'utente di riferimento
    @ColumnInfo(name= "activeTitle") var activeTitle: Int,
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
    @ColumnInfo(name= "titleBadge") val titleBadgeId: Int? = null,
    @ColumnInfo(name= "Requirement") val requirement: Int,
    @ColumnInfo(name= "Tag") val tag: String,
    @ColumnInfo(name= "type") val type: Boolean=true //generale (true) o settimanale (false)
)
@Entity(primaryKeys = ["id", "idUser"])
data class GeneralMissionUser(
    val id: Int, //Id della missione di riferimento
    val idUser: Int,
    @ColumnInfo(name= "progression") var progression: Int=0, //valore di indice di completamento
    @ColumnInfo(name= "active") var active: Boolean=true, //se questa missione è attiva o meno,
    // di defaultinizialmente messa a true
    //Se la missione è claimable, ovvero se l'utente ha completato la missione e può
    //richiedere la ricompensa
    @ColumnInfo(name = "claimable") var claimable: Boolean = false

)
@Entity(primaryKeys = ["id", "idUser"])
data class WeeklyMissionUser(
    val id: Int, //Id della missione di riferimento
    val idUser: Int,
    @ColumnInfo(name= "progression") var progression: Int=0, //valore di indice di completamento
    @ColumnInfo(name= "active") var active: Boolean=true, //se questa missione è attiva o meno,
    // di default inizialmente messa a true
    @ColumnInfo(name = "claimable") var claimable: Boolean = false
)

@Entity
data class TitleBadge(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name= "name") val name: String,
    @ColumnInfo(name= "rarity") val rarity: String
)
