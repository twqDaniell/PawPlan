package com.example.pawplan

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

fun formatDateString(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date: Date = inputFormat.parse(dateString) ?: return dateString
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}

fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}

fun showDatePickerDialog(context: Context, editText: EditText) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            editText.setText(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

fun uploadImageToFirebase(
    imageUri: Uri,
    folder: String = "images",
    onSuccess: (downloadUrl: String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storageRef = FirebaseStorage.getInstance().reference.child("$folder/${UUID.randomUUID()}.jpg")
    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                onSuccess(imageUrl.toString())
            }.addOnFailureListener { e ->
                onFailure(e)
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}