package com.example.pawplan

import com.example.pawplan.models.User

interface UserDao {
    fun insertUser(user: User)
    fun getUser(): User?
    fun clearUsers()
}
