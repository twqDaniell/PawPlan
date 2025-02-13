package com.example.pawplan.Dao

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pawplan.models.Allergy
import com.example.pawplan.models.Pet
import com.example.pawplan.models.User
import com.example.pawplan.models.Memory
import com.example.pawplan.models.Vet
import com.example.pawplan.models.VetVisit
import java.util.*

class AppDatabase(context: Context) : SQLiteOpenHelper(context, "pawplan_db", null, 2) {

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

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS memories (
                id TEXT PRIMARY KEY,
                petId TEXT NOT NULL,
                picture TEXT NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vets (
                vetId TEXT PRIMARY KEY,
                vetName TEXT NOT NULL,
                phoneNumber TEXT NOT NULL
            )
            """
        )

        // Store visitDate as epoch milliseconds
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vetVisits (
                id TEXT PRIMARY KEY,
                petId TEXT NOT NULL,
                topic TEXT NOT NULL,
                visitDate INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS allergies (
                id TEXT PRIMARY KEY,
                allergyName TEXT NOT NULL,
                petId TEXT NOT NULL
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS pets")
        db.execSQL("DROP TABLE IF EXISTS memories")
        db.execSQL("DROP TABLE IF EXISTS vets")
        db.execSQL("DROP TABLE IF EXISTS vetVisits")
        db.execSQL("DROP TABLE IF EXISTS allergies")
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

    val memoryDao = object : MemoryDao {
        override fun insertMemory(memory: Memory) {
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO memories (id, petId, picture) VALUES (?, ?, ?)",
                arrayOf(memory.id, memory.petId, memory.picture)
            )
        }
        override fun getMemoriesByPetId(petId: String): List<Memory> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM memories WHERE petId = ?", arrayOf(petId))
            val list = mutableListOf<Memory>()
            if (cursor.moveToFirst()) {
                do {
                    list.add(Memory(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
                } while(cursor.moveToNext())
            }
            cursor.close()
            return list
        }
        override fun clearMemories() {
            writableDatabase.execSQL("DELETE FROM memories")
        }
    }

    val vetDao = object : VetDao {
        override fun insertVet(vet: Vet) {
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO vets (vetId, vetName, phoneNumber) VALUES (?, ?, ?)",
                arrayOf(vet.vetId, vet.vetName, vet.phoneNumber)
            )
        }
        override fun getVet(vetId: String): Vet? {
            val cursor = readableDatabase.rawQuery("SELECT * FROM vets WHERE vetId = ? LIMIT 1", arrayOf(vetId))
            return if (cursor.moveToFirst()) {
                Vet(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            } else null
        }
        override fun clearVets() {
            writableDatabase.execSQL("DELETE FROM vets")
        }
    }

    val vetVisitDao = object : VetVisitDao {
        override fun insertVetVisit(visit: VetVisit, petId: String) {
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO vetVisits (id, petId, topic, visitDate) VALUES (?, ?, ?, ?)",
                arrayOf(visit.id, petId, visit.topic, visit.visitDate.time)
            )
        }
        override fun getVetVisitsByPetId(petId: String): List<VetVisit> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM vetVisits WHERE petId = ?", arrayOf(petId))
            val list = mutableListOf<VetVisit>()
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(1)
                    val topic = cursor.getString(2)
                    val visitDateMillis = cursor.getLong(3)
                    val visitDate = Date(visitDateMillis)
                    list.add(VetVisit(id, topic, visitDate))
                } while (cursor.moveToNext())
            }
            cursor.close()
            return list
        }
        override fun deleteVetVisit(id: String) {
            writableDatabase.execSQL("DELETE FROM vetVisits WHERE id = ?", arrayOf(id))
        }
        override fun clearVetVisits() {
            writableDatabase.execSQL("DELETE FROM vetVisits")
        }
    }

    val allergyDao = object : AllergyDao {
        override fun insertAllergy(allergy: Allergy, petId: String) {
            val id = allergy.id.ifEmpty { UUID.randomUUID().toString() }
            writableDatabase.execSQL(
                "INSERT OR REPLACE INTO allergies (id, allergyName, petId) VALUES (?, ?, ?)",
                arrayOf(id, allergy.allergyName, petId)
            )
        }

        override fun getAllergiesByPetId(petId: String): List<Allergy> {
            val cursor = readableDatabase.rawQuery("SELECT * FROM allergies WHERE petId = ?", arrayOf(petId))
            val list = mutableListOf<Allergy>()
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(0)
                    val allergyName = cursor.getString(1)
                    val petIdRetrieved = cursor.getString(2)
                    list.add(Allergy(id, allergyName, petIdRetrieved))
                } while(cursor.moveToNext())
            }
            cursor.close()
            return list
        }

        override fun deleteAllergy(id: String) {
            writableDatabase.execSQL("DELETE FROM allergies WHERE id = ?", arrayOf(id))
        }

        override fun clearAllergies() {
            writableDatabase.execSQL("DELETE FROM allergies")
        }
    }

}
