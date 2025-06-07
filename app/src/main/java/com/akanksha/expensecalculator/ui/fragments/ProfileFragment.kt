package com.akanksha.expensecalculator.ui.fragments

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.FragmentProfileBinding
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.utils.BiometricUtils
import com.akanksha.expensecalculator.utils.NotificationUtils
import com.akanksha.expensecalculator.utils.ThemeUtils
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import com.akanksha.expensecalculator.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels()

    private val currencies = listOf(
        "USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "INR (₹)", 
        "AUD ($)", "CAD ($)", "CHF (Fr)", "CNY (¥)", "HKD ($)",
        "NZD ($)", "SEK (kr)", "KRW (₩)", "SGD ($)", "NOK (kr)",
        "MXN ($)", "RUB (₽)", "ZAR (R)", "TRY (₺)", "BRL (R$)"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeData()
    }

    private fun setupUI() {
        setupCurrencyDropdown()

        // Theme button
        binding.buttonTheme.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Export data button
        binding.buttonExport.setOnClickListener {
            exportData()
        }

        // Clear data button
        binding.buttonClearData.setOnClickListener {
            showClearDataConfirmationDialog()
        }

        // Notification settings button
        binding.buttonNotifications.setOnClickListener {
            showNotificationSettingsDialog()
        }

        // Biometric authentication button
        binding.buttonBiometric.setOnClickListener {
            toggleBiometricAuthentication()
        }

        binding.buttonSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupCurrencyDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown_menu,
            currencies
        )
        binding.dropdownCurrency.setAdapter(adapter)
        
        // Set default selection if available
        userViewModel.getUserCurrency()?.let { savedCurrency ->
            binding.dropdownCurrency.setText(savedCurrency, false)
        }

        binding.dropdownCurrency.setOnItemClickListener { _, _, position, _ ->
            val selectedCurrency = currencies[position]
            userViewModel.setUserCurrency(selectedCurrency)
        }
    }

    private fun saveProfile() {
        val name = binding.editName.text.toString()
        val email = binding.editEmail.text.toString()
        val currency = binding.dropdownCurrency.text.toString()

        userViewModel.saveUserProfile(name, email, currency)
        Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
    }

    private fun observeData() {
        userViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                binding.editName.setText(profile.name)
                binding.editEmail.setText(profile.email)
                
                if (currencies.contains(profile.currency)) {
                    binding.dropdownCurrency.setText(profile.currency, false)
                } else if (profile.currency.isNotEmpty()) {
                    binding.dropdownCurrency.setText(currencies[0], false)
                }
            }
        }

        // Observe biometric state
        userViewModel.biometricEnabled.observe(viewLifecycleOwner) { enabled ->
            updateBiometricButtonText(enabled)
        }

        // Observe theme mode
        userViewModel.themeMode.observe(viewLifecycleOwner) { mode ->
            updateThemeButtonText(mode)
        }
    }

    private fun updateBiometricButtonText(enabled: Boolean) {
        binding.buttonBiometric.text = if (enabled) {
            getString(R.string.disable_biometric_authentication)
        } else {
            getString(R.string.enable_biometric_authentication)
        }
    }

    private fun updateThemeButtonText(themeMode: Int) {
        binding.buttonTheme.text = when (themeMode) {
            ThemeUtils.MODE_LIGHT -> getString(R.string.theme_light)
            ThemeUtils.MODE_DARK -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )
        
        val currentMode = userViewModel.themeMode.value ?: ThemeUtils.MODE_SYSTEM
        val selectedIndex = when (currentMode) {
            ThemeUtils.MODE_LIGHT -> 0
            ThemeUtils.MODE_DARK -> 1
            else -> 2
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_theme))
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val newMode = when (which) {
                    0 -> ThemeUtils.MODE_LIGHT
                    1 -> ThemeUtils.MODE_DARK
                    else -> ThemeUtils.MODE_SYSTEM
                }
                userViewModel.setThemeMode(newMode)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun toggleBiometricAuthentication() {
        if (BiometricUtils.isBiometricAvailable(requireContext())) {
            if (!userViewModel.biometricEnabled.value!!) {
                // Enable biometric
                BiometricUtils.showBiometricPrompt(
                    requireActivity(),
                    onSuccess = {
                        userViewModel.toggleBiometricAuthentication()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.biometric_enabled),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { _, errString ->
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.biometric_error, errString),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } else {
                // Disable biometric
                userViewModel.toggleBiometricAuthentication()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.biometric_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.biometric_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showNotificationSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_notification_settings, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.notification_settings))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            val switchNotifications = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(
                R.id.switch_notifications
            )
            val switchBudgetAlerts = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(
                R.id.switch_budget_alerts
            )
            val buttonReminderTime = dialogView.findViewById<com.google.android.material.button.MaterialButton>(
                R.id.button_reminder_time
            )

            // Set initial values
            switchNotifications.isChecked = NotificationUtils.areNotificationsEnabled(requireContext())
            switchBudgetAlerts.isChecked = NotificationUtils.areBudgetAlertsEnabled(requireContext())
            
            // Set reminder time text
            val reminderTime = NotificationUtils.getReminderTime(requireContext())
            if (reminderTime > 0) {
                val calendar = Calendar.getInstance().apply { timeInMillis = reminderTime }
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                buttonReminderTime.text = timeFormat.format(calendar.time)
            }

            // Set reminder time click listener
            buttonReminderTime.setOnClickListener {
                val cal = Calendar.getInstance()
                if (reminderTime > 0) {
                    cal.timeInMillis = reminderTime
                }
                
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        cal.set(Calendar.MINUTE, minute)
                        NotificationUtils.setReminderTime(requireContext(), cal.timeInMillis)
                        
                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        buttonReminderTime.text = timeFormat.format(cal.time)
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            }

            // Override positive button to save settings
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                NotificationUtils.setNotificationsEnabled(requireContext(), switchNotifications.isChecked)
                NotificationUtils.setBudgetAlertsEnabled(requireContext(), switchBudgetAlerts.isChecked)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notification_settings_saved),
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun exportData() {
        viewLifecycleOwner.lifecycleScope.launch {
            expenseViewModel.getAllExpenses().collectLatest { expenses: List<Expense> ->
                if (expenses.isNotEmpty()) {
                    com.akanksha.expensecalculator.utils.ExportUtils.exportExpensesAndShare(requireContext(), expenses)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.data_exported),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.no_data_to_export),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showClearDataConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_all_data))
            .setMessage(getString(R.string.clear_data_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                clearAllData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun clearAllData() {
        expenseViewModel.deleteAllExpenses()
        Toast.makeText(
            requireContext(),
            getString(R.string.data_cleared),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 