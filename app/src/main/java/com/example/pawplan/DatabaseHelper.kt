package com.example.pawplan.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "PawPlanDB"
        private const val DATABASE_VERSION = 1

        // Tables
        private const val TABLE_USER = "user"
        private const val TABLE_PET = "pet"

        // User Columns
        private const val KEY_USER_ID = "id"
        private const val KEY_USER_NAME = "name"
        private const val KEY_USER_PHONE = "phone"

        // Pet Columns
        private const val KEY_PET_ID = "id"
        private const val KEY_PET_NAME = "name"
        private const val KEY_PET_BREED = "breed"
        private const val KEY_PET_WEIGHT = "weight"
        private const val KEY_PET_COLOR = "color"
        private const val KEY_PET_BIRTH_DATE = "birth_date"
        private const val KEY_PET_ADOPTION_DATE = "adoption_date"
        private const val KEY_PET_PICTURE = "picture"
        private const val KEY_PET_FOOD_IMAGE = "food_image"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUserTable = "CREATE TABLE $TABLE_USER ($KEY_USER_ID TEXT PRIMARY KEY, $KEY_USER_NAME TEXT, $KEY_USER_PHONE TEXT)"
        val createPetTable = "CREATE TABLE $TABLE_PET ($KEY_PET_ID TEXT PRIMARY KEY, $KEY_PET_NAME TEXT, $KEY_PET_BREED TEXT, $KEY_PET_WEIGHT INTEGER, $KEY_PET_COLOR TEXT, $KEY_PET_BIRTH_DATE TEXT, $KEY_PET_ADOPTION_DATE TEXT, $KEY_PET_PICTURE TEXT, $KEY_PET_FOOD_IMAGE TEXT)"
        db.execSQL(createUserTable)
        db.execSQL(createPetTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PET")
        onCreate(db)
    }

    // ✅ Insert or Update User
    fun saveUser(id: String, name: String, phone: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USER_ID, id)
            put(KEY_USER_NAME, name)
            put(KEY_USER_PHONE, phone)
        }
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // ✅ Get User
    fun getUser(): Map<String, String>? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_USER LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            mapOf(
                "id" to cursor.getString(0),
                "name" to cursor.getString(1),
                "phone" to cursor.getString(2)
            )
        } else null
    }

    // ✅ Insert or Update Pet
    fun savePet(id: String, name: String, breed: String, weight: Int, color: String, birthDate: String, adoptionDate: String, picture: String, foodImage: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PET_ID, id)
            put(KEY_PET_NAME, name)
            put(KEY_PET_BREED, breed)
            put(KEY_PET_WEIGHT, weight)
            put(KEY_PET_COLOR, color)
            put(KEY_PET_BIRTH_DATE, birthDate)
            put(KEY_PET_ADOPTION_DATE, adoptionDate)
            put(KEY_PET_PICTURE, picture)
            put(KEY_PET_FOOD_IMAGE, foodImage)
        }
        db.insertWithOnConflict(TABLE_PET, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    // ✅ Get Pet
    fun getPet(): Map<String, String>? {
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_PET LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            mapOf(
                "id" to cursor.getString(0),
                "name" to cursor.getString(1),
                "breed" to cursor.getString(2),
                "weight" to cursor.getInt(3).toString(),
                "color" to cursor.getString(4),
                "birth_date" to cursor.getString(5),
                "adoption_date" to cursor.getString(6),
                "picture" to cursor.getString(7),
                "food_image" to cursor.getString(8)
            )
        } else null
    }
}
