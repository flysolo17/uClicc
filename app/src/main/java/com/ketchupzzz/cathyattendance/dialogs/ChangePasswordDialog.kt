package com.ketchupzzz.cathyattendance.dialogs

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ketchupzzz.cathyattendance.databinding.FragmentChangePasswordDialogBinding
import com.ketchupzzz.cathyattendance.loginsystem.LoginActivity

import com.ketchupzzz.cathyattendance.viewmodels.UserViewModel

class ChangePasswordDialog : DialogFragment() {
    private lateinit var binding: FragmentChangePasswordDialogBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBack.setOnClickListener {
            dismiss()
        }
        binding.buttonChangePassword.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            authenticateOldPassword(user,binding.inputOldPassword.text.toString())
        }
    }

    private fun authenticateOldPassword(user: FirebaseUser?, oldPassword: String) {
        if (user != null && user.email != null) {
            val authCredential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(authCredential).addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    val newPassword: String =
                        binding.inputNewPassword.text.toString()
                    val confirmPassword: String =
                        binding.inputConfirmPassword.text.toString()
                    if (newPassword.length < 7) {
                        binding.inputNewPassword.error = "Password too weak!"
                    } else if (newPassword != confirmPassword) {
                        binding.inputConfirmPassword.error = "Password didn't match!"
                    } else {
                        updatePassword(user, newPassword)
                    }
                } else {
                    binding.inputOldPassword.error = "Invalid password"
                }
            }
        }
    }

    private fun updatePassword(user: FirebaseUser, newPassword: String) {
        user.updatePassword(newPassword).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                Toast.makeText(
                    binding.root.context,
                    "Password updated Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                showSuccessDialog()
            } else {
                Toast.makeText(binding.root.context, "Failed!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(
                binding.root.context, "Failed: " + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSuccessDialog() {
        val alertDialog = AlertDialog.Builder(binding.root.context)
        alertDialog
            .setTitle("Stay Logged in?")
            .setMessage("password updated successfully, do you want to continue logged in ?")
            .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                dismiss()
                dialog.dismiss()
            }.setNegativeButton("Logout") { _: DialogInterface?, _: Int ->
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            }.show()
    }


}