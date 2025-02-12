package com.example.pawplan.health

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
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
import com.example.pawplan.models.VetVisit
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.firebase.firestore.Query
import java.util.*

class HealthFragment : Fragment() {

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

    private var vetId: String? = null
    private var petId: String? = null
    private var petWeight: Int? = null
    private val visitList = mutableListOf<VetVisit>()
    private lateinit var visitAdapter: VetVisitAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_health, container, false)

        vetNameText = view.findViewById(R.id.vetName)
        vetPhoneText = view.findViewById(R.id.vetPhone)
        vetVisitsRecycler = view.findViewById(R.id.vetVisitsRecycler)
        addVetButton = view.findViewById(R.id.addVetButton)
        addVisitButton = view.findViewById(R.id.addVisitButton)

        lastVisitText = view.findViewById(R.id.lastVisitText)
        nextVisitText = view.findViewById(R.id.nextVisitText)

        petId = arguments?.getString("petId")
        vetId = arguments?.getString("vetId")
        petWeight = arguments?.getInt("petWeight")

        view.findViewById<TextView>(R.id.weightText).text = petWeight.toString()

        vetVisitsRecycler.layoutManager = LinearLayoutManager(requireContext())
        visitAdapter = VetVisitAdapter(visitList)
        vetVisitsRecycler.adapter = visitAdapter

        addVetSection = view.findViewById<View>(R.id.addVetSection)
        vetDets = view.findViewById<LinearLayout>(R.id.vetDets)
        vetSection = view.findViewById<View>(R.id.vetSection)

        if (vetId.isNullOrEmpty()) {
            addVetSection.visibility = View.VISIBLE
            vetDets.visibility = View.GONE
        } else {
            fetchVetData()
            addVetSection.visibility = View.GONE
            vetSection.visibility = View.VISIBLE
            vetDets.visibility = View.VISIBLE
        }

        fetchVetVisits()

        addVisitButton.setOnClickListener { showAddVisitDialog() }

        addVetButton.setOnClickListener {
            showAddVetDialog()
        }

        return view
    }

    private fun fetchVetData() {
        FirebaseFirestore.getInstance().collection("vets")
            .document(vetId!!)
            .get()
            .addOnSuccessListener { document ->
                vetNameText.text = document.getString("vetName") ?: "Unknown"
                vetPhoneText.text = document.getString("phoneNumber") ?: "Unknown"
            }
    }

    private fun fetchVetVisits() {
        FirebaseFirestore.getInstance().collection("vetVisits")
            .whereEqualTo("petId", petId)
            .get()
            .addOnSuccessListener { documents ->
                visitList.clear()
                val pastVisits = mutableListOf<VetVisit>()
                val futureVisits = mutableListOf<VetVisit>()

                val today = Date()

                for (doc in documents) {
                    val visitDate = doc.getDate("visitDate")
                    if (visitDate != null) {
                        val visit = VetVisit(
                            doc.getString("topic") ?: "Unknown",
                            visitDate
                        )

                        if (visitDate.before(today)) {
                            pastVisits.add(visit) // Past visit
                        } else {
                            futureVisits.add(visit) // Future visit
                        }
                    }
                }

                // Sort lists by date
                pastVisits.sortByDescending { it.visitDate } // Descending
                futureVisits.sortBy { it.visitDate } // Ascending

                // Combine lists: past visits first, then future visits
                visitList.addAll(futureVisits + pastVisits)
                visitAdapter.notifyDataSetChanged()

                // ✅ Set last visit (most recent past visit) and next visit (earliest future visit)
                lastVisitText.text = if (pastVisits.isNotEmpty()) formatDate(pastVisits.first().visitDate.toString()) else "-"
                nextVisitText.text = if (futureVisits.isNotEmpty()) formatDate(futureVisits.first().visitDate.toString()) else "-"
            }
    }

    private fun showAddVisitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_visit, null)
        val topicInput = dialogView.findViewById<EditText>(R.id.editVisitTopic)
        val dateInput = dialogView.findViewById<EditText>(R.id.editVisitDate)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Vet Visit")
            .setPositiveButton("Save", null) // Set to null, we handle it manually
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show() // Must call show() before accessing buttons

        // ✅ Reference Save Button AFTER show()
        val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        saveButton.isEnabled = false // Start as disabled

        // ✅ Function to Check for Filled Fields
        fun shouldEnableSaveButton(): Boolean {
            return topicInput.text.toString().isNotBlank() && dateInput.text.toString().isNotBlank()
        }

        // ✅ Listen for Text Changes
        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                saveButton.isEnabled = shouldEnableSaveButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        topicInput.addTextChangedListener(textWatcher)
        dateInput.addTextChangedListener(textWatcher)

        // ✅ Open DatePicker when clicking on date field
        dateInput.setOnClickListener {
            showDatePicker(dateInput)
        }

        // ✅ Handle Save Button Click
        saveButton.setOnClickListener {
            saveVetVisit(dialogView)
            dialog.dismiss()
        }
    }


    private fun showAddVetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_vet, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add Vet")
            .setPositiveButton("Save") { _, _ ->
                saveVetToFirestore(dialogView)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun saveVetVisit(dialogView: View) {
        val topic = dialogView.findViewById<EditText>(R.id.editVisitTopic).text.toString()
        val dateString = dialogView.findViewById<EditText>(R.id.editVisitDate).text.toString()

        if (topic.isEmpty() || dateString.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val visitDate = dateFormat.parse(dateString) ?: return

        val newVisit = hashMapOf(
            "petId" to petId,
            "topic" to topic,
            "visitDate" to visitDate
        )

        FirebaseFirestore.getInstance().collection("vetVisits")
            .add(newVisit)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Visit added!", Toast.LENGTH_SHORT).show()
                val addedVisit = VetVisit(topic, visitDate)

                visitList.add(addedVisit) // ✅ Add to list
                visitList.sortByDescending { it.visitDate } // ✅ Keep sorted

                visitAdapter.notifyItemInserted(visitList.indexOf(addedVisit))
                updateNextAndLastVisit()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add visit", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveVetToFirestore(dialogView: View) {
        val vetName = dialogView.findViewById<EditText>(R.id.editVetName).text.toString()
        val vetPhone = dialogView.findViewById<EditText>(R.id.editVetPhone).text.toString()

        if (vetName.isEmpty() || vetPhone.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val vetData = hashMapOf(
            "vetName" to vetName,
            "phoneNumber" to vetPhone
        )

        // ✅ Add new vet to Firestore
        db.collection("vets")
            .add(vetData)
            .addOnSuccessListener { vetDocRef ->
                val newVetId = vetDocRef.id // Get the new vet's ID

                // ✅ Update the pet document with the new vetId
                petId?.let { petId ->
                    db.collection("pets").document(petId)
                        .update("vetId", newVetId)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Vet added successfully!", Toast.LENGTH_SHORT).show()
                            vetId = newVetId // Update local variable
                            vetNameText.text = vetName
                            vetPhoneText.text = vetPhone

                            addVetSection.visibility = View.GONE
                            vetSection.visibility = View.VISIBLE
                            vetDets.visibility = View.VISIBLE

                            val mainActivity = requireActivity() as MainActivity
                            mainActivity.vetIdGlobal = newVetId
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

    private fun updateNextAndLastVisit() {
        val today = Date()

        val pastVisits = visitList.filter { it.visitDate.before(today) }.sortedByDescending { it.visitDate }
        val futureVisits = visitList.filter { it.visitDate.after(today) }.sortedBy { it.visitDate }

        lastVisitText.text = if (pastVisits.isNotEmpty()) formatDate(pastVisits.first().visitDate.toString()) else "-"
        nextVisitText.text = if (futureVisits.isNotEmpty()) formatDate(futureVisits.first().visitDate.toString()) else "-"
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val date: Date = inputFormat.parse(dateString) ?: return dateString
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString // Return the original if parsing fails
        }
    }
}
