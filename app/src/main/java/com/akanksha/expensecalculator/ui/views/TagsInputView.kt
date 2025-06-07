package com.akanksha.expensecalculator.ui.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.ViewTagsInputBinding
import com.akanksha.expensecalculator.utils.TagsManager
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Custom view for entering and managing tags with auto-suggestions
 */
class TagsInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    
    private val binding: ViewTagsInputBinding
    private val tagsManager: TagsManager = TagsManager.getInstance(context)
    private val tags = mutableListOf<String>()
    
    init {
        // Inflate the layout
        val inflater = LayoutInflater.from(context)
        binding = ViewTagsInputBinding.inflate(inflater, this, true)
        
        setupTagInput()
        setupSuggestions()
        setupPopularTags()
    }
    
    /**
     * Set up the tag input field
     */
    private fun setupTagInput() {
        binding.editTags.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s?.toString() ?: ""
                    
                    if (text.contains(",")) {
                        val tagText = text.substringBefore(",").trim()
                        if (tagText.isNotEmpty()) {
                            addTag(tagText)
                        }
                        setText("")
                    } else {
                        updateSuggestions(text)
                    }
                }
                
                override fun afterTextChanged(s: Editable?) {}
            })
            
            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    val tagText = text.toString().trim()
                    if (tagText.isNotEmpty()) {
                        addTag(tagText)
                        setText("")
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        }
    }
    
    /**
     * Set up the suggestions list
     */
    private fun setupSuggestions() {
        binding.recyclerSuggestions.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = SuggestionAdapter { suggestion ->
                addTag(suggestion)
                binding.editTags.setText("")
            }
        }
    }
    
    /**
     * Set up the popular tags section
     */
    private fun setupPopularTags() {
        val popularTags = tagsManager.getPopularTags(5)
        binding.chipGroupPopular.removeAllViews()
        
        if (popularTags.isEmpty()) {
            binding.textPopularTags.visibility = View.GONE
            binding.chipGroupPopular.visibility = View.GONE
        } else {
            binding.textPopularTags.visibility = View.VISIBLE
            binding.chipGroupPopular.visibility = View.VISIBLE
            
            popularTags.forEach { tag ->
                val chip = createChip(tag)
                chip.setOnClickListener {
                    addTag(tag)
                }
                binding.chipGroupPopular.addView(chip)
            }
        }
    }
    
    /**
     * Update suggestions based on user input
     */
    private fun updateSuggestions(text: String) {
        val suggestions = if (text.length >= 2) {
            tagsManager.getSuggestions(text)
        } else {
            emptyList()
        }
        
        (binding.recyclerSuggestions.adapter as? SuggestionAdapter)?.updateSuggestions(suggestions)
        binding.recyclerSuggestions.visibility = if (suggestions.isEmpty()) View.GONE else View.VISIBLE
    }
    
    /**
     * Add a tag to the view
     */
    private fun addTag(tag: String) {
        val normalizedTag = tag.trim()
        if (normalizedTag.isEmpty() || tags.contains(normalizedTag)) return
        
        tags.add(normalizedTag)
        
        val chip = createChip(normalizedTag)
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            removeTag(normalizedTag)
            binding.chipGroupTags.removeView(chip)
        }
        
        binding.chipGroupTags.addView(chip)
    }
    
    /**
     * Create a chip for a tag
     */
    private fun createChip(text: String): Chip {
        return Chip(context).apply {
            this.text = text
            setChipBackgroundColorResource(R.color.colorAccent)
            setTextColor(ContextCompat.getColor(context, R.color.white))
            isCheckable = false
        }
    }
    
    /**
     * Remove a tag
     */
    private fun removeTag(tag: String) {
        tags.remove(tag)
    }
    
    /**
     * Get the current list of tags
     */
    fun getTags(): List<String> = tags.toList()
    
    /**
     * Set tags from an existing list
     */
    fun setTags(newTags: List<String>) {
        tags.clear()
        binding.chipGroupTags.removeAllViews()
        
        newTags.forEach { tag ->
            addTag(tag)
        }
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val child = getChildAt(0)
        child.layout(0, 0, r - l, b - t)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val child = getChildAt(0)
        measureChild(child, widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            child.measuredWidth,
            child.measuredHeight
        )
    }
    
    /**
     * Adapter for tag suggestions
     */
    private class SuggestionAdapter(
        private val onSuggestionClick: (String) -> Unit
    ) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {
        
        private var suggestions = listOf<String>()
        
        fun updateSuggestions(newSuggestions: List<String>) {
            suggestions = newSuggestions
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tag_suggestion, parent, false)
            return SuggestionViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
            holder.bind(suggestions[position], onSuggestionClick)
        }
        
        override fun getItemCount(): Int = suggestions.size
        
        class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textSuggestion: TextView = itemView.findViewById(R.id.text_suggestion)
            
            fun bind(suggestion: String, onClick: (String) -> Unit) {
                textSuggestion.text = suggestion
                itemView.setOnClickListener { onClick(suggestion) }
            }
        }
    }
} 