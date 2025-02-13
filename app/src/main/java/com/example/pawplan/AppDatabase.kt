package com.example.pawplan

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pawplan.models.Pet
import com.example.pawplan.models.User

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "pawplan_db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                phoneNumber TEXT NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pets (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                breed TEXT NOT NULL,
                weight INTEGER NOT NULL,
                color TEXT NOT NULL,
                birthDate TEXT NOT NULL,
                adoptionDate TEXT NOT NULL,
                picture TEXT NOT NULL,
                foodImage TEXT NOT NULL
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS pets")
        onCreate(db)
    }

    val userDao = object : UserDao {
        override fun insertUser(user: User) {
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO users (id, name, phoneNumber) VALUES (?, ?, ?)",
                arrayOf(user.id, user.name, user.phoneNumber)
            )
        }

        override fun getUser(): User? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM users LIMIT 1", null)
            return if (cursor.moveToFirst()) {
                User(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            } else null
        }

        override fun clearUsers() {
            writableDatabase.execSQL("DELETE FROM users")
        }
    }

    val petDao = object : PetDao {
        override fun insertPet(pet: Pet) {
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO pets (id, name, breed, weight, color, birthDate, adoptionDate, picture, foodImage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    pet.id, pet.name, pet.breed, pet.weight, pet.color,
                    pet.birthDate, pet.adoptionDate, pet.picture, pet.foodImage
                )
            )
        }

        override fun getPet(): Pet? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM pets LIMIT 1", null)
            return if (cursor.moveToFirst()) {
                Pet(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
                )
            } else null
        }

        override fun clearPets() {
            writableDatabase.execSQL("DELETE FROM pets")
        }
    }
}
