package com.example.pawplan.models

import androidx.lifecycle.ViewModel
import java.sql.Date

class RegistrationViewModel : ViewModel() {
    var userName: String? = null
    var phoneNumber: String? = null
    var petName: String? = null
    var petType: String? = null
    var petGender: String? = null
    var petBreed: String? = null
    var petBirthDate: Date? = null
    var petWeight: Int? = null
    var petColor: String? = null
    var petAdoptionDate: Date? = null
    var petPicture: String? = null
}
