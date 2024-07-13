package com.biometricing.biometricattendance

import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val checkInButton=findViewById<Button>(R.id.check_in_button)
        val checkOutButton=findViewById<Button>(R.id.view_attendance_button)
//Handle button clicks
        checkInButton.setOnClickListener {
            val progressBar = findViewById<ProgressBar>(R.id.progress_bar) // Assuming you have a progress bar
            progressBar.visibility = View.VISIBLE // Show progress indicator

            // Biometric authentication logic (from previous steps)

            val success
            if (success) { // User successfully authenticated using biometrics
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val isWithinOfficeLocation = isWithinOfficeLocation(location.latitude, location.longitude)

                        if (isWithinOfficeLocation) {
                            // User is within office, proceed with attendance marking
                            val attendanceDao = AppDatabase.getInstance(this).attendanceDao
                            val attendance = Attendance(timestamp = System.currentTimeMillis(), location = "Office")
                            attendanceDao.insertAttendance(attendance)
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE // Hide progress indicator
                                    Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    progressBar.visibility = View.GONE // Hide progress indicator
                                    Toast.makeText(this, "Attendance marking failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            progressBar.visibility = View.GONE // Hide progress indicator
                            Toast.makeText(this, "Attendance cannot be marked outside office", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        progressBar.visibility = View.GONE // Hide progress indicator
                        Toast.makeText(this, "Location data unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                progressBar.visibility = View.GONE // Hide progress indicator
                val errorMessage = getErrorMessage(errorCode)
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        checkOutButton.setOnClickListener() {
            //Handle check-out functionality
            //Similar logic to check-in
        }
        viewAttendanceButton.setOnClickListener() {
            //Navigate to activity/fragment for viewing attendance history
            val intent = Intent(this, AttendanceHistoryActivity::class.java)
            startActivity(intent) //Assuming AttendanceHistoryActivity exists
        }
      }
    }
// Using BiometricManager


// Assuming BiometricManager is in the package com.biometricing.biometricattendance

val biometricManager = com.biometricing.biometricattendance.BiometricManager.from(this)
val capabilities = biometricManager.canAuthenticate()

val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

if (capabilities.hasBiometric) {
    // Biometric authentication is available, proceed with prompt

    val authenticationPrompt = BiometricPrompt.Builder(this)
        .setTitle("Attendance Login")
        .setDescription("Use your fingerprint or other biometric method to mark attendance")
        .setNegativeButton("Cancel", null) // Handle negative button click (optional)
        .build()

    // Start authentication process
    authenticationPrompt.authenticate(cancellationSignal = null, // Optional cancellation signal
        cryptoObject = null, // Optional cryptographic object for encryption
        flags = BiometricPrompt.BIOMETRIC_CONFORMITY_MODE_DEVICE) { success, errorCode ->
        if (success) {
            // User successfully authenticated using biometrics
            Toast.makeText(this, "Authentication successful!", Toast.LENGTH_SHORT).show()

            // Proceed with location verification
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatitude = location.latitude
                    val userLongitude = location.longitude

                    // Check if userLocation is within the office boundaries
                    val isWithinOfficeLocation = isWithinOfficeLocation(userLatitude, userLongitude)

                    if (isWithinOfficeLocation) {
                        // User is within the office premises, proceed with attendance marking
                        // (database storage and UI updates)
                    } else {
                        // User is not within the expected location
                        Toast.makeText(this, "Attendance cannot be marked outside office", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Authentication failed
            val errorMessage = getErrorMessage(errorCode)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

} else {
    // Handle no biometric case (alternative authentication or inform user)
    Toast.makeText(this, "Biometric authentication not available on this device", Toast.LENGTH_SHORT).show()
    // Provide alternative login method (e.g., PIN, password) or explain why biometrics is unavailable
}
}

//Attendance storage. Using Room Persistence Library
@Dao
interface AttendanceDao {
    @insert
    fun insertAttendance(attendance:Attendance)

    @Query("SELECT * FROM Attendance ORDER BY timestamp DESC")
    fun getAllAttendance(): LiveData<List<Attendance>>
}
//Inserting attendance record
val attendanceDao=AppDatabase.getInstance(this).attendanceDao
val attendance=Attendance(timestamp=System.currentTimeMillis(), fusedLocationClient = "Office")
attendanceDao.insertAttendance(attendance)
val attendanceList= attendanceDao.getAllAttendance().observeForever {
    attendance -> //Update UI with attendance data
}
//Check Biometric Availability
val biometricManager = BiometricManager.from(this)
val capabilities = com.biometricing.biometricattendance.biometricManager.canAuthenticate()

 if(capabilities.hasBiometric) {
     //Proceed with biometric authentication
 } else {
     //Inform user that biometric authentication is not available on their device
     Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_SHORT).show()

 }
//Build the Authentication Prompt
val authenticationPrompt = BiometricPrompt.Builder(this)
    .setTitle("Attendance Login")
    .setDescription("Use your finger print to mark attendance")
    .setNegativeButton("Cancel", null)
//Handle negative button click (optional)
    .build()

//Authentication with BiometricPrompt
authenticationPrompt.authenticate(cancellationSignal = null,
    //Optional cancellation signal
cryptoObject = null, //Optional cryptographic object for encryption
flags=BiometricPrompt.BIOMETRIC_CONFORMITY_MODE_DEVICE) {
    success, errorCode->
    if (success) {
        //User successfully authenticated using biometrics
        Toast.makeText(
            this, "Authentication successful",
            Toast.LENGTH_SHORT
        ).show()
        //Proceed with attendance marking (
        // location verification, database storage)
    } else {
        //Authentication failed
        val errorMessage = getErrorMessage(errorCode)
        Toast.makeText(
            this, errorMessage,
            Toast.LENGTH_SHORT
        ).show()
    }
}
//Error Handling
private fun getErrorMessage(errorCode: Int): String {
    return when (errorCode) {
        BiometricPrompt.ERROR_CANCELED ->
            "Authentication canceled by user"

        BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
            "Biometric sensor is unavailable"

        BiometricPrompt.ERROR_NEGATIVE_AUTH ->
            "User didn't authenticate"
        //Handle other potential error codes
        else -> "An unknown error occured"
    }

}




