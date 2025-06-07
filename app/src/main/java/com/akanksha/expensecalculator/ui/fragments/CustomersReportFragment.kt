package com.akanksha.expensecalculator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.model.Customer
import com.akanksha.expensecalculator.databinding.FragmentCustomersReportBinding
import com.akanksha.expensecalculator.ui.adapters.CustomersAdapter
import com.akanksha.expensecalculator.viewmodel.CustomerViewModel
import com.google.android.material.snackbar.Snackbar

class CustomersReportFragment : Fragment() {
    private var _binding: FragmentCustomersReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CustomerViewModel
    private lateinit var adapter: CustomersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity()).get(CustomerViewModel::class.java)
        
        // Setup UI components
        setupRecyclerView()
        setupSearchView()
        setupAddButton()
        
        // Observe data
        observeData()
    }
    
    private fun setupRecyclerView() {
        adapter = CustomersAdapter(
            onCustomerClick = { customer ->
                // Show customer details
                Snackbar.make(
                    binding.root,
                    getString(R.string.customer_details, customer.name),
                    Snackbar.LENGTH_SHORT
                ).show()
            },
            onCustomerOptionsClick = { customer, view ->
                showCustomerOptionsMenu(customer, view)
            }
        )
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupSearchView() {
        binding.searchEditText.doAfterTextChanged { text ->
            if (text != null && text.length >= 2) {
                viewModel.searchCustomers(text.toString())
            } else if (text != null && text.isEmpty()) {
                viewModel.loadCustomers()
            }
        }
    }
    
    private fun setupAddButton() {
        binding.addCustomerButton.setOnClickListener {
            // Show add customer dialog or navigate to add customer screen
            Snackbar.make(
                binding.root,
                getString(R.string.add_customer),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showCustomerOptionsMenu(customer: Customer, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.customer_options_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    // Edit customer action
                    Snackbar.make(
                        binding.root,
                        getString(R.string.edit) + " " + customer.name,
                        Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }
                R.id.action_delete -> {
                    // Delete customer action
                    viewModel.deleteCustomer(customer.id)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun observeData() {
        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            if (customers.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(customers)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 