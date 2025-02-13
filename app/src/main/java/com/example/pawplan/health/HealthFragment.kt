package com.example.pawplan.health

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pawplan.MainActivity
import com.example.pawplan.R
import com.example.pawplan.adapters.VetVisitAdapter
import com.example.pawplan.models.Vet
import com.example.pawplan.models.VetVisit
import com.example.pawplan.AppDatabase
import com.example.pawplan.formatDateString
import com.example.pawplan.showDatePickerDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.textfield.TextInputEditText

class HealthFragment : Fragment(), VetVisitAdapter.VetVisitActionListener {

    private lateinit var vetNameText: TextView
    private lateinit var vetPhoneText: TextView
    private lateinit var vetVisitsRecycler: RecyclerView
    private lateinit var addVetButton: ImageButton
    private lateinit var addVisitButton: ImageButton
    private lateinit var lastVisitText: TextView
    private lateinit var nextVisitText: TextView
    private lateinit var addVetSection: View
    private lateinit var vetDets: LinearLayout
    private lateinit var vetSection: View
    private lateinit var editVetButton: ImageButton

    private var vetId: String? = null
    private var petId: String? = null
    private var petWeight: Int? = null
    private val visitList = mutableListOf<VetVisit>()
    private lateinit var visitAdapter: VetVisitAdapter

    private lateinit var appDatabase: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_health, container, false)

        vetNameText = view.findViewById(R.id.vetName)
        vetPhoneText = view.findViewById(R.id.vetPhone)
        vetVisitsRecycler = view.findViewById(R.id.vetVisitsRecycler)
        addVetButton = view.findViewById(R.id.addVetButton)
        addVisitButton = view.findViewById(R.id.addVisitButton)
        lastVisitText = view.findViewById(R.id.lastVisitText)
        nextVisitText = view.findViewById(R.id.nextVisitText)
        editVetButton = view.findViewById(R.id.editVetButton)

        petId = arguments?.getString("petId")
        vetId = arguments?.getString("vetId")
        petWeight = arguments?.getInt("petWeight")
        view.findViewById<TextView>(R.id.weightText).text = petWeight.toString()

        vetVisitsRecycler.layoutManager = LinearLayoutManager(requireContext())
        visitAdapter = VetVisitAdapter(visitList, this)
        vetVisitsRecycler.adapter = visitAdapter

        addVetSection = view.findViewById(R.id.addVetSection)
        vetDets = view.findViewById(R.id.vetDets)
        vetSection = view.findViewById(R.id.vetSection)

        appDatabase = AppDatabase(requireContext())

        if (vetId.isNullOrEmpty()) {
            addVetSection.visibility = View.VISIBLE
            vetDets.visibility = View.GONE
        } else {
            loadVetData()
            addVetSection.visibility = View.GONE
            vetSection.visibility = View.VISIBLE
            vetDets.visibility = View.VISIBLE
        }

        loadVetVisits()

        addVisitButton.setOnClickListener { showAddVisitDialog() }
        addVetButton.setOnClickListener { showVetDialog(null) } // for adding a new vet
        if (vetId != null && vetNameText != null && vetPhoneText != null) {
            editVetButton.setOnClickListener {
                showVetDialog(
                    Vet(
                        vetId.toString(),
                        vetNameText.text.toString(),
                        vetPhoneText.text.toString()
                    )
                )
            }
        }

        return view
    }

    private fun loadVetData() {
        vetId?.let { id ->
            val localVet = appDatabase.vetDao.getVet(id)
            if (localVet != null) {
                vetNameText.text = localVet.vetName
                vetPhoneText.text = localVet.phoneNumber
            } else {
                fetchVetDataFromRemote(id)
            }
        }
    }

    private fun fetchVetDataFromRemote(id: String) {
        FirebaseFirestore.getInstance().collection("vets").document(id)
            .get()
            .addOnSuccessListener { document ->
                val fetchedVetName = document.getString("vetName") ?: "Unknown"
                val fetchedVetPhone = document.getString("phoneNumber") ?: "Unknown"
                vetNameText.text = fetchedVetName
                vetPhoneText.text = fetchedVetPhone
                val vet = Vet(id, fetchedVetName, fetchedVetPhone)
                appDatabase.vetDao.insertVet(vet)
            }
    }

    private fun loadVetVisits() {
        petId?.let { id ->
            val localVisits = appDatabase.vetVisitDao.getVetVisitsByPetId(id)
            if (localVisits.isNotEmpty()) {
                visitList.clear()
                visitList.addAll(localVisits)
                sortAndDisplayVisits()
            } else {
                fetchVetVisitsFromRemote(id)
            }
        }
    }

    private fun fetchVetVisitsFromRemote(petId: String) {
        FirebaseFirestore.getInstance().collection("vetVisits")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { documents ->
                visitList.clear()
                val remoteVisits = mutableListOf<VetVisit>()
                for (doc in documents) {
                    val visitDate = doc.getDate("visitDate")
                    if (visitDate != null) {
                        val id = doc.id
                        val visit = VetVisit(id, doc.getString("topic") ?: "Unknown", visitDate)
                        remoteVisits.add(visit)
                        appDatabase.vetVisitDao.insertVetVisit(visit, petId)
                    }
                }
                visitList.addAll(remoteVisits)
                sortAndDisplayVisits()
            }
    }

    private fun sortAndDisplayVisits() {
        val today = Date()
        val pastVisits = visitList.filter { it.visitDate.before(today) }.sortedByDescending { it.visitDate }
        val futureVisits = visitList.filter { it.visitDate.after(today) }.sortedBy { it.visitDate }
        visitList.clear()
        visitList.addAll(futureVisits + pastVisits)
        visitAdapter.notifyDataSetChanged()
        lastVisitText.text = if (pastVisits.isNotEmpty()) formatDateString(pastVisits.first().visitDate.toString()) else "-"
        nextVisitText.text = if (futureVisits.isNotEmpty()) formatDateString(futureVisits.first().visitDate.toString()) else "-"
    }

    private fun showAddVisitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_visit, null)
        val topicInput = dialogView.findViewById<TextInputEditText>(R.id.editVisitTopic)
        val dateInput = dialogView.findViewById<TextInputEditText>(R.id.editVisitDate)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Vet Visit")
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
        fun shouldEnableSaveButton() = topicInput.text.toString().isNotBlank() && dateInput.text.toString().isNotBlank()
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { saveButton.isEnabled = shouldEnableSaveButton() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        topicInput.addTextChangedListener(textWatcher)
        dateInput.addTextChangedListener(textWatcher)
        dateInput.setOnClickListener { showDatePickerDialog(requireContext(), dateInput) }
        saveButton.setOnClickListener {
            addVetVisit(topicInput.text.toString().trim(), dateInput.text.toString().trim())
            dialog.dismiss()
        }
    }

    private fun addVetVisit(topic: String, dateStr: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val visitDate = dateFormat.parse(dateStr) ?: return
        val newVisitData = hashMapOf("petId" to petId, "topic" to topic, "visitDate" to visitDate)
        FirebaseFirestore.getInstance().collection("vetVisits")
            .add(newVisitData)
            .addOnSuccessListener { docRef ->
                Toast.makeText(requireContext(), "Visit added!", Toast.LENGTH_SHORT).show()
                val addedVisit = VetVisit(docRef.id, topic, visitDate)
                visitList.add(addedVisit)
                visitList.sortByDescending { it.visitDate }
                visitAdapter.notifyDataSetChanged()
                updateNextAndLastVisit()
                petId?.let { id -> appDatabase.vetVisitDao.insertVetVisit(addedVisit, id) }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add visit", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showVetDialog(existingVet: Vet?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_vet, null)
        val vetNameInput = dialogView.findViewById<TextInputEditText>(R.id.editVetName)
        val vetPhoneInput = dialogView.findViewById<TextInputEditText>(R.id.editVetPhone)
        val title = if (existingVet == null) "Add Vet" else "Edit Vet"
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle(title)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
        if (existingVet != null) {
            vetNameInput.setText(existingVet.vetName)
            vetPhoneInput.setText(existingVet.phoneNumber)
        }
        val originalName = vetNameInput.text.toString()
        val originalPhone = vetPhoneInput.text.toString()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false
        fun shouldEnableSaveButton(): Boolean {
            val name = vetNameInput.text.toString().trim()
            val phone = vetPhoneInput.text.toString().trim()
            if (name.isEmpty() || phone.isEmpty()) return false
            return if (existingVet != null) {
                name != originalName || phone != originalPhone
            } else true
        }
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                saveButton.isEnabled = shouldEnableSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        vetNameInput.addTextChangedListener(textWatcher)
        vetPhoneInput.addTextChangedListener(textWatcher)
        saveButton.setOnClickListener {
            val newName = vetNameInput.text.toString().trim()
            val newPhone = vetPhoneInput.text.toString().trim()
            if (existingVet == null) {
                addVet(newName, newPhone)
            } else {
                updateVet(existingVet.vetId, newName, newPhone)
            }
            dialog.dismiss()
        }
    }

    private fun addVet(newName: String, newPhone: String) {
        val db = FirebaseFirestore.getInstance()
        val vetData = mapOf("vetName" to newName, "phoneNumber" to newPhone)
        db.collection("vets")
            .add(vetData)
            .addOnSuccessListener { docRef ->
                val newVetId = docRef.id
                petId?.let { id ->
                    db.collection("pets").document(id)
                        .update("vetId", newVetId)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Vet added successfully!", Toast.LENGTH_SHORT).show()
                            vetId = newVetId
                            vetNameText.text = newName
                            vetPhoneText.text = newPhone
                            addVetSection.visibility = View.GONE
                            vetSection.visibility = View.VISIBLE
                            vetDets.visibility = View.VISIBLE
                            val mainActivity = requireActivity() as MainActivity
                            mainActivity.vetIdGlobal = newVetId
                            val vet = Vet(newVetId, newName, newPhone)
                            appDatabase.vetDao.insertVet(vet)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to link vet to pet", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add vet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateVet(vetId: String, newName: String, newPhone: String) {
        FirebaseFirestore.getInstance().collection("vets").document(vetId)
            .update(mapOf("vetName" to newName, "phoneNumber" to newPhone))
            .addOnSuccessListener {
                vetNameText.text = newName
                vetPhoneText.text = newPhone
                val vet = Vet(vetId, newName, newPhone)
                appDatabase.vetDao.insertVet(vet)
                Toast.makeText(requireContext(), "Vet updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to update vet", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateNextAndLastVisit() {
        val today = Date()
        val pastVisits = visitList.filter { it.visitDate.before(today) }.sortedByDescending { it.visitDate }
        val futureVisits = visitList.filter { it.visitDate.after(today) }.sortedBy { it.visitDate }
        lastVisitText.text = if (pastVisits.isNotEmpty()) formatDateString(pastVisits.first().visitDate.toString()) else "-"
        nextVisitText.text = if (futureVisits.isNotEmpty()) formatDateString(futureVisits.first().visitDate.toString()) else "-"
    }

    override fun onDeleteVetVisit(visit: VetVisit) {
        petId?.let { id ->
            FirebaseFirestore.getInstance().collection("vetVisits")
                .whereEqualTo("petId", id)
                .whereEqualTo("topic", visit.topic)
                .whereEqualTo("visitDate", visit.visitDate)
                .get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        doc.reference.delete()
                    }
                    visitList.remove(visit)
                    visitAdapter.notifyDataSetChanged()
                    updateNextAndLastVisit()
                    appDatabase.vetVisitDao.deleteVetVisit(visit.id)
                }
        }
    }
}
