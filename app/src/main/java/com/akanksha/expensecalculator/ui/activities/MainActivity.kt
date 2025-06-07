package com.akanksha.expensecalculator.ui.activities

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.ActivityMainBinding
import com.akanksha.expensecalculator.utils.BiometricUtils
import com.akanksha.expensecalculator.utils.ThemeUtils
import com.akanksha.expensecalculator.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var userViewModel: UserViewModel
    private lateinit var backgroundAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setting content view
        ThemeUtils.applyTheme(this)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        
        setSupportActionBar(binding.toolbar)
        setupNavigation()
        setupDrawerHeader()
        setupAnimatedBackground()
        setupBackPressedCallback()
        checkBiometricAuth()
    }
    
    private fun setupAnimatedBackground() {
        // Get the constraint layout from the binding
        val constraintLayout = binding.root.findViewById<ConstraintLayout>(R.id.constraint_layout)
        
        // Set the background animation
        backgroundAnimation = constraintLayout.background as AnimationDrawable
        
        // Start the animation when the window has focus
        binding.root.post {
            backgroundAnimation.start()
        }
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Define top level destinations
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.historyFragment,
            R.id.statsFragment,
            R.id.profileFragment,
            R.id.customersReportFragment,
            R.id.adminFragment
        )
        
        // Configure app bar with navigation drawer
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinations,
            binding.drawerLayout
        )
        
        // Setup toggle for drawer
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Setup action bar with navigation controller
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Connect navigation view with navigation controller
        binding.navView.setupWithNavController(navController)
        
        // Connect bottom navigation with navigation controller
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Update toolbar title
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Hide toolbar title for all fragments with their own title
            when (destination.id) {
                R.id.homeFragment, R.id.historyFragment, R.id.statsFragment, 
                R.id.profileFragment, R.id.helpFragment, R.id.customersReportFragment, 
                R.id.adminFragment -> {
                    binding.toolbarTitle.visibility = View.GONE
                }
                else -> {
                    binding.toolbarTitle.visibility = View.VISIBLE
                    binding.toolbarTitle.text = destination.label
                }
            }
            
            // Hide/show bottom navigation based on destination
            when(destination.id) {
                R.id.homeFragment, R.id.historyFragment, R.id.statsFragment, R.id.profileFragment -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
        }
        
        // Setup drawer menu item clicks for custom destinations
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment, R.id.historyFragment, R.id.statsFragment, R.id.profileFragment -> {
                    navController.navigate(menuItem.itemId)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.customersMenuItem -> {
                    navController.navigate(R.id.customersReportFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.reportMenuItem -> {
                    // Show a toast message for now
                    android.widget.Toast.makeText(this, "Reports feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.adminMenuItem -> {
                    navController.navigate(R.id.adminFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.helpMenuItem -> {
                    navController.navigate(R.id.helpFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupDrawerHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val headerTitle = headerView.findViewById<TextView>(R.id.header_title)
        val headerSubtitle = headerView.findViewById<TextView>(R.id.header_subtitle)
        
        // Update header with user information
        userViewModel.userProfile.observe(this) { profile ->
            if (profile != null && profile.name.isNotEmpty()) {
                headerTitle.text = profile.name
                if (profile.email.isNotEmpty()) {
                    headerSubtitle.text = profile.email
                } else {
                    headerSubtitle.text = getString(R.string.manage_your_finances)
                }
            } else {
                headerTitle.text = getString(R.string.app_name)
                headerSubtitle.text = getString(R.string.manage_your_finances)
            }
        }
    }
    
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                navController.navigate(R.id.profileFragment)
                true
            }
            R.id.action_help -> {
                navController.navigate(R.id.helpFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkBiometricAuth() {
        if (BiometricUtils.isBiometricEnabled(this)) {
            BiometricUtils.showBiometricPrompt(
                this,
                onSuccess = {
                    // Authentication successful, continue with app
                },
                onError = { errorCode, errorMessage ->
                    // Handle authentication error
                    finish()
                }
            )
        }
    }
} 