package com.example.myapplication.data.repositories

import com.example.myapplication.data.database.User
import com.example.myapplication.data.database.UserDao

class UserDaoRepository(private val userDao: UserDao) {

    suspend fun getById(id: Int) = userDao.getById(id)


    suspend fun login(email: String, password: String) = userDao.login(email, password)



    suspend fun insertUser(user: User) = userDao.insert(user)


    suspend fun update(user: User) = userDao.update(user)

}

/*
es view model
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun login(email: String, password: String) = liveData {
        val user = repository.login(email, password)
        emit(user)
    }
}*/