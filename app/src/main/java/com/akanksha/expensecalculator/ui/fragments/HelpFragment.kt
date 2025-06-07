package com.akanksha.expensecalculator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.FragmentHelpBinding
import com.akanksha.expensecalculator.ui.adapters.HelpTopicAdapter
import com.akanksha.expensecalculator.data.model.HelpTopic

class HelpFragment : Fragment() {
    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: HelpTopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupContactButtons()
    }
    
    private fun setupRecyclerView() {
        adapter = HelpTopicAdapter()
        binding.helpTopicsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.helpTopicsRecyclerView.adapter = adapter
        
        // Load help topics
        val helpTopics = loadHelpTopics()
        adapter.submitList(helpTopics)
    }
    
    private fun setupContactButtons() {
        binding.emailSupportButton.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:akankshabharti12379@gmail.com")
                putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.help_request_subject))
                putExtra(android.content.Intent.EXTRA_TEXT, "Hello, I need help with the Expense Calculator app.")
            }
            startActivity(android.content.Intent.createChooser(intent, getString(R.string.send_email)))
        }
        
        binding.visitWebsiteButton.setOnClickListener {
            showWebsiteOptionsDialog()
        }
    }
    
    private fun showWebsiteOptionsDialog() {
        val options = arrayOf("Time Sheet Template", "Excel Spreadsheet Template", "User Manual")
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Resource")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openUrl("https://www.expensecalculator.com/timesheet")
                    1 -> openUrl("https://www.expensecalculator.com/spreadsheet")
                    2 -> openUrl("https://www.expensecalculator.com/manual")
                }
            }
            .show()
    }
    
    private fun openUrl(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        startActivity(intent)
    }
    
    private fun loadHelpTopics(): List<HelpTopic> {
        return listOf(
            HelpTopic(
                getString(R.string.help_topic_getting_started),
                getString(R.string.help_content_getting_started)
            ),
            HelpTopic(
                getString(R.string.help_topic_adding_expenses),
                getString(R.string.help_content_adding_expenses)
            ),
            HelpTopic(
                getString(R.string.help_topic_budget_management),
                getString(R.string.help_content_budget_management)
            ),
            HelpTopic(
                getString(R.string.help_topic_reports),
                getString(R.string.help_content_reports)
            ),
            HelpTopic(
                getString(R.string.help_topic_customers),
                getString(R.string.help_content_customers)
            ),
            HelpTopic(
                getString(R.string.help_topic_data_backup),
                getString(R.string.help_content_data_backup)
            ),
            HelpTopic(
                getString(R.string.help_topic_privacy),
                getString(R.string.help_content_privacy)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 