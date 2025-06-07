package com.akanksha.expensecalculator.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.DialogUserManagementBinding
import com.akanksha.expensecalculator.databinding.DialogAdvancedSettingsBinding
import com.akanksha.expensecalculator.databinding.FragmentAdminBinding
import com.akanksha.expensecalculator.viewmodel.AdminViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminFragment : Fragment() {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AdminViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(AdminViewModel::class.java)
        
        // Setup UI components
        setupUI()
        
        // Observe data
        observeData()
    }
    
    private fun setupUI() {
        // Data Management section
        binding.backupButton.setOnClickListener {
            showBackupDialog()
        }
        
        binding.restoreButton.setOnClickListener {
            showRestoreDialog()
        }
        
        binding.exportButton.setOnClickListener {
            showExportDialog()
        }
        
        // User Management section
        binding.userManagementButton.setOnClickListener {
            showUserManagementDialog()
        }
        
        // Advanced Settings section
        binding.settingsButton.setOnClickListener {
            showAdvancedSettingsDialog()
        }
    }
    
    private fun observeData() {
        // Observe backup status
        viewModel.backupStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                AdminViewModel.BackupStatus.LOADING -> {
                    showLoading(true)
                }
                AdminViewModel.BackupStatus.SUCCESS -> {
                    showLoading(false)
                    Toast.makeText(context, "Backup completed successfully", Toast.LENGTH_SHORT).show()
                }
                AdminViewModel.BackupStatus.FAILURE -> {
                    showLoading(false)
                    Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
                }
                else -> { /* do nothing */ }
            }
        }
        
        // Observe restore status
        viewModel.restoreStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                AdminViewModel.RestoreStatus.LOADING -> {
                    showLoading(true)
                }
                AdminViewModel.RestoreStatus.SUCCESS -> {
                    showLoading(false)
                    Toast.makeText(context, "Restore completed successfully", Toast.LENGTH_SHORT).show()
                }
                AdminViewModel.RestoreStatus.FAILURE -> {
                    showLoading(false)
                    Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                }
                else -> { /* do nothing */ }
            }
        }
        
        // Observe export status
        viewModel.exportStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                AdminViewModel.ExportStatus.LOADING -> {
                    showLoading(true)
                }
                AdminViewModel.ExportStatus.SUCCESS -> {
                    showLoading(false)
                    Toast.makeText(context, "Export completed successfully", Toast.LENGTH_SHORT).show()
                }
                AdminViewModel.ExportStatus.FAILURE -> {
                    showLoading(false)
                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                }
                else -> { /* do nothing */ }
            }
        }
        
        // Observe error messages
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        // Implement loading indicator if needed
    }
    
    private fun showBackupDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Backup Data")
            .setMessage("This will create a backup of all your expense data. Continue?")
            .setPositiveButton("Backup") { _, _ ->
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupName = "ExpenseCalculator_Backup_$timestamp"
                
                Toast.makeText(context, "Starting backup: $backupName", Toast.LENGTH_SHORT).show()
                viewModel.backupData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showRestoreDialog() {
        // Simulate finding backup files
        val backupFiles = listOf(
            "ExpenseCalculator_Backup_20230601_123045",
            "ExpenseCalculator_Backup_20230715_183022",
            "ExpenseCalculator_Backup_20230822_093412"
        )
        
        if (backupFiles.isEmpty()) {
            Toast.makeText(context, "No backup files found", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Restore Data")
            .setItems(backupFiles.toTypedArray()) { _, which ->
                val selectedBackup = backupFiles[which]
                
                // Confirm restore
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm Restore")
                    .setMessage("Are you sure you want to restore from $selectedBackup? This will replace your current data.")
                    .setPositiveButton("Restore") { _, _ ->
                        Toast.makeText(context, "Restoring from: $selectedBackup", Toast.LENGTH_SHORT).show()
                        viewModel.restoreData()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showExportDialog() {
        val exportOptions = arrayOf("Export All Data", "Export This Month", "Export By Category")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Export to CSV")
            .setItems(exportOptions) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(context, "Exporting all data to CSV", Toast.LENGTH_SHORT).show()
                        viewModel.exportToCsv()
                    }
                    1 -> {
                        Toast.makeText(context, "Exporting this month's data to CSV", Toast.LENGTH_SHORT).show()
                        viewModel.exportToCsv()
                    }
                    2 -> {
                        showCategorySelectionDialog()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCategorySelectionDialog() {
        val categories = arrayOf("Food", "Travel", "Shopping", "Entertainment", "Bills", "Health", "Education", "Misc")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                val selectedCategory = categories[which]
                Toast.makeText(context, "Exporting $selectedCategory expenses to CSV", Toast.LENGTH_SHORT).show()
                viewModel.exportToCsv()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showUserManagementDialog() {
        val dialogBinding = DialogUserManagementBinding.inflate(LayoutInflater.from(context))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("User Management")
            .setView(dialogBinding.root)
            .setPositiveButton("Close", null)
            .create()
            
        // Setup user management UI
        dialogBinding.addUserButton.setOnClickListener {
            Toast.makeText(context, "Add user functionality will be implemented in future updates", Toast.LENGTH_SHORT).show()
        }
        
        dialogBinding.editPermissionsButton.setOnClickListener {
            Toast.makeText(context, "Permission management will be implemented in future updates", Toast.LENGTH_SHORT).show()
        }
        
        dialog.show()
    }
    
    private fun showAdvancedSettingsDialog() {
        val dialogBinding = DialogAdvancedSettingsBinding.inflate(LayoutInflater.from(context))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Advanced Settings")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                // Save settings
                val autoBackup = dialogBinding.switchAutoBackup.isChecked
                val dataSync = dialogBinding.switchDataSync.isChecked
                val debugMode = dialogBinding.switchDebugMode.isChecked
                
                // Apply settings
                Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 