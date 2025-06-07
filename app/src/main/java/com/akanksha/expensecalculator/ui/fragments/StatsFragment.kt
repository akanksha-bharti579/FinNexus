package com.akanksha.expensecalculator.ui.fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.data.models.Expense
import com.akanksha.expensecalculator.databinding.FragmentStatsBinding
import com.akanksha.expensecalculator.ui.adapters.TagExpenseSummaryAdapter
import com.akanksha.expensecalculator.ui.adapters.TagExpenseSummaryAdapter.TagExpenseSummary
import com.akanksha.expensecalculator.utils.CurrencyUtils
import com.akanksha.expensecalculator.utils.DateUtils
import com.akanksha.expensecalculator.utils.TagsManager
import com.akanksha.expensecalculator.viewmodel.ExpenseViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by viewModels()
    private lateinit var tagsManager: TagsManager
    
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart
    
    private var startDate: Date = DateUtils.getStartOfMonth()
    private var endDate: Date = DateUtils.getEndOfMonth()
    
    private var selectedPeriod: Int = PERIOD_MONTH
    private var selectedTagFilter: String? = null
    
    companion object {
        private const val PERIOD_WEEK = 0
        private const val PERIOD_MONTH = 1
        private const val PERIOD_QUARTER = 2
        private const val PERIOD_YEAR = 3
        private const val PERIOD_CUSTOM = 4
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagsManager = TagsManager.getInstance(requireContext())
        setupUI()
        setupDateRangePickers()
        setupPeriodSpinner()
        setupTagFilter()
        observeData()
    }

    private fun setupUI() {
        // Setup bar chart for monthly trends
        barChart = binding.chartMonthlyTrend
        setupBarChart()
        
        // Setup pie chart for category distribution
        pieChart = binding.chartCategoryDistribution
        setupPieChart()
        
        // Setup refresh button
        binding.buttonRefresh.setOnClickListener {
            refreshData()
        }
    }
    
    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            
            val xAxis = xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setCenterAxisLabels(false)
            xAxis.setDrawGridLines(false)
            
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(true)
            
            animateY(1500, Easing.EaseInOutQuad)
        }
    }
    
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            
            holeRadius = 58f
            transparentCircleRadius = 61f
            
            setDrawCenterText(true)
            centerText = "Categories"
            
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            legend.orientation = Legend.LegendOrientation.VERTICAL
            legend.setDrawInside(false)
            
            animateY(1400, Easing.EaseInOutQuad)
        }
    }
    
    private fun setupDateRangePickers() {
        // Format and display current date range
        updateDateRangeDisplay()
        
        // Start date picker
        binding.buttonStartDate.setOnClickListener {
            showDatePicker(true)
        }
        
        // End date picker
        binding.buttonEndDate.setOnClickListener {
            showDatePicker(false)
        }
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        calendar.time = if (isStartDate) startDate else endDate
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                if (isStartDate) {
                    startDate = calendar.time
                    if (startDate.after(endDate)) {
                        endDate = calendar.time
                    }
                } else {
                    endDate = calendar.time
                    if (endDate.before(startDate)) {
                        startDate = calendar.time
                    }
                }
                updateDateRangeDisplay()
                selectedPeriod = PERIOD_CUSTOM
                binding.spinnerPeriod.setSelection(PERIOD_CUSTOM)
                refreshData()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun updateDateRangeDisplay() {
        binding.textStartDate.text = DateUtils.formatDate(startDate)
        binding.textEndDate.text = DateUtils.formatDate(endDate)
    }
    
    private fun setupPeriodSpinner() {
        val periods = arrayOf("This Week", "This Month", "This Quarter", "This Year", "Custom")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periods
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.spinnerPeriod.adapter = adapter
        binding.spinnerPeriod.setSelection(PERIOD_MONTH) // Default to monthly view
        
        binding.spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != selectedPeriod) {
                    selectedPeriod = position
                    updateDateRangeBasedOnPeriod()
                    refreshData()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun updateDateRangeBasedOnPeriod() {
        val calendar = Calendar.getInstance()
        
        when (selectedPeriod) {
            PERIOD_WEEK -> {
                startDate = DateUtils.getStartOfWeek()
                endDate = DateUtils.getEndOfWeek()
            }
            PERIOD_MONTH -> {
                startDate = DateUtils.getStartOfMonth()
                endDate = DateUtils.getEndOfMonth()
            }
            PERIOD_QUARTER -> {
                // Get start of current quarter
                val month = calendar.get(Calendar.MONTH)
                val quarterStartMonth = (month / 3) * 3 // 0, 3, 6, or 9
                calendar.set(Calendar.MONTH, quarterStartMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                DateUtils.setStartOfDay(calendar)
                startDate = calendar.time
                
                // Get end of current quarter
                calendar.set(Calendar.MONTH, quarterStartMonth + 2)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                DateUtils.setEndOfDay(calendar)
                endDate = calendar.time
            }
            PERIOD_YEAR -> {
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                DateUtils.setStartOfDay(calendar)
                startDate = calendar.time
                
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                DateUtils.setEndOfDay(calendar)
                endDate = calendar.time
            }
            // For CUSTOM, we don't change the dates
        }
        
        updateDateRangeDisplay()
    }
    
    private fun setupTagFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            val popularTags = tagsManager.getPopularTags(10)
            val tagOptions = mutableListOf("All Tags")
            tagOptions.addAll(popularTags)
            
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tagOptions
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            
            binding.spinnerTag.adapter = adapter
            
            binding.spinnerTag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedTagFilter = if (position == 0) null else tagOptions[position]
                    refreshData()
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun observeData() {
        refreshData()
    }
    
    private fun refreshData() {
        loadCategoryDistribution()
        loadMonthlyTrends()
        loadExpenseSummary()
        loadTopExpensesForTags()
    }
    
    private fun loadCategoryDistribution() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get all expenses for the selected period
            val expenses = viewModel.getAllExpenses().first().filter {
                it.date in startDate..endDate && (selectedTagFilter == null || it.tags.contains(selectedTagFilter))
            }
            
            // Group by category
            val categoryExpenses = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { expense -> expense.amount } }
                .filter { it.value > 0 }
                .toList()
                .sortedByDescending { it.second }
            
            updatePieChart(categoryExpenses)
        }
    }
    
    private fun updatePieChart(categoryExpenses: List<Pair<String, Double>>) {
        if (categoryExpenses.isEmpty()) {
            pieChart.setNoDataText("No expense data available")
            pieChart.invalidate()
            return
        }
        
        val entries = categoryExpenses.map { 
            PieEntry(it.second.toFloat(), it.first) 
        }
        
        val dataSet = PieDataSet(entries, "Categories")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.WHITE)
        
        pieChart.data = data
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }
    
    private fun loadMonthlyTrends() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Determine the appropriate time intervals based on selected period
            val (intervals, labels) = when (selectedPeriod) {
                PERIOD_WEEK -> getWeeklyIntervals()
                PERIOD_MONTH -> getDailyIntervals()
                PERIOD_QUARTER -> getMonthlyIntervals(3)
                PERIOD_YEAR -> getMonthlyIntervals(12)
                else -> getCustomIntervals() // For custom date range
            }
            
            // Get all expenses for the selected period
            val expenses = viewModel.getAllExpenses().first().filter {
                it.date in startDate..endDate && (selectedTagFilter == null || it.tags.contains(selectedTagFilter))
            }
            
            // Group expenses by interval
            val intervalExpenses = intervals.mapIndexed { index, interval ->
                val intervalStart = interval.first
                val intervalEnd = interval.second
                
                val total = expenses.filter { 
                    it.date in intervalStart..intervalEnd 
                }.sumOf { it.amount }
                
                BarEntry(index.toFloat(), total.toFloat())
            }
            
            updateBarChart(intervalExpenses, labels)
        }
    }
    
    private fun getWeeklyIntervals(): Pair<List<Pair<Date, Date>>, List<String>> {
        val intervals = mutableListOf<Pair<Date, Date>>()
        val labels = mutableListOf<String>()
        
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        // Create intervals for each day of the week
        for (i in 0..6) {
            val dayStart = calendar.clone() as Calendar
            dayStart.add(Calendar.DAY_OF_WEEK, i)
            DateUtils.setStartOfDay(dayStart)
            
            val dayEnd = dayStart.clone() as Calendar
            DateUtils.setEndOfDay(dayEnd)
            
            intervals.add(dayStart.time to dayEnd.time)
            labels.add(SimpleDateFormat("EEE", Locale.getDefault()).format(dayStart.time))
        }
        
        return intervals to labels
    }
    
    private fun getDailyIntervals(): Pair<List<Pair<Date, Date>>, List<String>> {
        val intervals = mutableListOf<Pair<Date, Date>>()
        val labels = mutableListOf<String>()
        
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val endCalendar = Calendar.getInstance()
        endCalendar.time = endDate
        
        // Create intervals for each day of the month
        while (!calendar.after(endCalendar)) {
            val dayStart = calendar.clone() as Calendar
            DateUtils.setStartOfDay(dayStart)
            
            val dayEnd = dayStart.clone() as Calendar
            DateUtils.setEndOfDay(dayEnd)
            
            intervals.add(dayStart.time to dayEnd.time)
            labels.add(dayStart.get(Calendar.DAY_OF_MONTH).toString())
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return intervals to labels
    }
    
    private fun getMonthlyIntervals(numMonths: Int): Pair<List<Pair<Date, Date>>, List<String>> {
        val intervals = mutableListOf<Pair<Date, Date>>()
        val labels = mutableListOf<String>()
        
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        // Create intervals for each month
        for (i in 0 until numMonths) {
            val monthStart = calendar.clone() as Calendar
            monthStart.add(Calendar.MONTH, i)
            monthStart.set(Calendar.DAY_OF_MONTH, 1)
            DateUtils.setStartOfDay(monthStart)
            
            val monthEnd = monthStart.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
            DateUtils.setEndOfDay(monthEnd)
            
            intervals.add(monthStart.time to monthEnd.time)
            labels.add(SimpleDateFormat("MMM", Locale.getDefault()).format(monthStart.time))
        }
        
        return intervals to labels
    }
    
    private fun getCustomIntervals(): Pair<List<Pair<Date, Date>>, List<String>> {
        // For custom date ranges, create appropriate intervals based on the range length
        val daysDiff = ((endDate.time - startDate.time) / (24 * 60 * 60 * 1000)).toInt()
        
        return when {
            daysDiff <= 7 -> getWeeklyIntervals()
            daysDiff <= 31 -> getDailyIntervals()
            daysDiff <= 120 -> getMonthlyIntervals(4) // For ~4 months
            else -> getMonthlyIntervals(12) // For longer periods
        }
    }
    
    private fun updateBarChart(entries: List<BarEntry>, labels: List<String>) {
        if (entries.isEmpty()) {
            barChart.setNoDataText("No expense data available")
            barChart.invalidate()
            return
        }
        
        val dataSet = BarDataSet(entries, "Expenses")
        dataSet.setColors(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        
        val data = BarData(dataSet)
        data.setValueTextSize(10f)
        data.setValueTextColor(Color.BLACK)
        data.barWidth = 0.9f
        
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.data = data
        barChart.setFitBars(true)
        barChart.invalidate()
    }
    
    private fun loadExpenseSummary() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get all expenses for the selected period
            val expenses = viewModel.getAllExpenses().first().filter {
                it.date in startDate..endDate && (selectedTagFilter == null || it.tags.contains(selectedTagFilter))
            }
            
            if (expenses.isEmpty()) {
                binding.textHighestExpense.text = CurrencyUtils.formatAmount(0.0)
                binding.textAverageDaily.text = CurrencyUtils.formatAmount(0.0)
                binding.textTopCategory.text = "N/A"
                binding.textTotalExpense.text = CurrencyUtils.formatAmount(0.0)
                return@launch
            }
            
            // Find highest expense
            val highestExpense = expenses.maxByOrNull { it.amount }
            binding.textHighestExpense.text = CurrencyUtils.formatAmount(highestExpense?.amount ?: 0.0)
            
            // Calculate average daily expense
            val daysDiff = ((endDate.time - startDate.time) / (24 * 60 * 60 * 1000)).toInt() + 1
            val totalExpense = expenses.sumOf { it.amount }
            val averageDaily = if (daysDiff > 0) totalExpense / daysDiff else 0.0
            binding.textAverageDaily.text = CurrencyUtils.formatAmount(averageDaily)
            
            // Find top category
            val categoryExpenses = expenses.groupBy { it.category }
                .mapValues { it.value.sumOf { expense -> expense.amount } }
            val topCategory = categoryExpenses.maxByOrNull { it.value }
            binding.textTopCategory.text = topCategory?.key ?: "N/A"
            
            // Set total expense
            binding.textTotalExpense.text = CurrencyUtils.formatAmount(totalExpense)
        }
    }
    
    private fun loadTopExpensesForTags() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Get all expenses for the selected period
            val expenses = viewModel.getAllExpenses().first().filter {
                it.date in startDate..endDate
            }
            
            if (expenses.isEmpty()) {
                binding.recyclerTopTagExpenses.visibility = View.GONE
                binding.textNoTagData.visibility = View.VISIBLE
                return@launch
            }
            
            // Get expenses with tags
            val expensesWithTags = expenses.filter { it.tags.isNotEmpty() }
            
            if (expensesWithTags.isEmpty()) {
                binding.recyclerTopTagExpenses.visibility = View.GONE
                binding.textNoTagData.visibility = View.VISIBLE
                return@launch
            }
            
            // Group expenses by tag
            val tagExpenses = mutableMapOf<String, Double>()
            expensesWithTags.forEach { expense ->
                expense.tags.forEach { tag ->
                    tagExpenses[tag] = (tagExpenses[tag] ?: 0.0) + expense.amount
                }
            }
            
            // Sort and get top 5 tags
            val topTags = tagExpenses.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { TagExpenseSummary(it.key, it.value) }
            
            // Update adapter
            binding.recyclerTopTagExpenses.visibility = View.VISIBLE
            binding.textNoTagData.visibility = View.GONE
            val adapter = TagExpenseSummaryAdapter(topTags)
            binding.recyclerTopTagExpenses.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 