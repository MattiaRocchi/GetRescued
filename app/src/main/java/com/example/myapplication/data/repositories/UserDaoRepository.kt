package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.User
import com.example.myapplication.data.database.UserDao
import com.example.myapplication.data.database.UserInfo
import com.example.myapplication.data.database.UserWithInfo

class UserDaoRepository(private val userDao: UserDao) {

    suspend fun getById(id: Int) = userDao.getById(id)

    suspend fun getByEmail(email: String) = userDao.getByEmail(email)

    suspend fun login(email: String, password: String) = userDao.login(email, password)

    suspend fun findEmail(email: String) = userDao.findEmail(email)

    suspend fun insertUser(user: User): Long {
        return userDao.insert(user)
    }
    //suspend fun insertUser(user: User) = userDao.insert(user)

   // suspend fun insertUserInfo(userInfo: UserInfo) = userDao.insertInfo(userInfo)

    suspend fun insertUserWithInfo(user: User): Long {
        return userDao.insertUserWithInfo(user)
    }

    suspend fun update(user: User) = userDao.update(user)

    suspend fun getUserInfo(id: Int) = userDao.getUserInfo(id)

    suspend fun updateProfPic(id: Int, newFotoUri: String): Boolean {
        return userDao.updateProfPic(id, newFotoUri) > 0
    }

    suspend fun getUserWithInfo(userId: Int): UserWithInfo? {
        val user = userDao.getById(userId)
        val info = userDao.getUserInfo(userId)

        return if (user != null && info != null) {
            UserWithInfo(
                id = user.id,
                name = user.name,
                surname = user.surname,
                email = user.email,
                age = user.age,
                habitation = user.habitation,
                phoneNumber = user.phoneNumber,
                createdAt = user.createdAt,
                activeTitle = info.activeTitle,
                exp = info.exp,
                profileFoto = info.profileFoto
            )
        } else null
    }
}

/*
es view model
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun login(email: String, password: String) = liveData {
        val user = repository.login(email, password)
        emit(user)
    }
}*/