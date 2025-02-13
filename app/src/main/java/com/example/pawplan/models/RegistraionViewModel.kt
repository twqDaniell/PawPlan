package com.example.pawplan.models

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {
    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get() = _phoneNumber

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _petType = MutableLiveData<String>()
    val petType: LiveData<String> get() = _petType

    private val _petName = MutableLiveData<String>()
    val petName: LiveData<String> get() = _petName

    private val _petGender = MutableLiveData<String>()
    val petGender: LiveData<String> get() = _petGender

    private val _petBreed = MutableLiveData<String>()
    val petBreed: LiveData<String> get() = _petBreed

    private val _petBirthDate = MutableLiveData<String>()
    val petBirthDate: LiveData<String> get() = _petBirthDate

    private val _petWeight = MutableLiveData<Int>()
    val petWeight: LiveData<Int> get() = _petWeight

    private val _petColor = MutableLiveData<String>()
    val petColor: LiveData<String> get() = _petColor

    private val _petAdoptionDate = MutableLiveData<String>()
    val petAdoptionDate: LiveData<String> get() = _petAdoptionDate

    private val _petPicture = MutableLiveData<Uri>()
    val petPicture: LiveData<Uri> get() = _petPicture

    private val _foodImage = MutableLiveData<String>()
    val foodImage: LiveData<String> get() = _foodImage

    fun setPhoneNumber(number: String) {
        _phoneNumber.value = number
    }

    fun setUserName(name: String) {
        _userName.value = name
    }

    fun setPetType(type: String) {
        _petType.value = type
    }

    fun setPetName(name: String) {
        _petName.value = name
    }

    fun setPetGender(gender: String) {
        _petGender.value = gender
    }

    fun setPetBreed(breed: String) {
        _petBreed.value = breed
    }

    fun setPetBirthDate(date: String) {
        _petBirthDate.value = date
    }

    fun setPetWeight(weight: Int) {
        _petWeight.value = weight
    }

    fun setPetColor(color: String) {
        _petColor.value = color
    }

    fun setPetAdoptionDate(date: String) {
        _petAdoptionDate.value = date
    }

    fun setPetPicture(url: Uri) {
        _petPicture.value = url
    }

    fun setFoodImage(url: String) {
        _foodImage.value = url
    }
}
