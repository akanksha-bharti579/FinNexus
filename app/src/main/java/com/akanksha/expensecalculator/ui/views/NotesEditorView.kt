package com.akanksha.expensecalculator.ui.views

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.akanksha.expensecalculator.R
import com.akanksha.expensecalculator.databinding.ViewNotesEditorBinding
import android.graphics.Typeface

/**
 * Custom view for editing notes with rich text capabilities
 */
class NotesEditorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val binding: ViewNotesEditorBinding
    
    // Formatting states
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var isHighlighted = false
    
    init {
        orientation = VERTICAL
        
        val inflater = LayoutInflater.from(context)
        binding = ViewNotesEditorBinding.inflate(inflater, this, true)
        
        setupFormatButtons()
        setupTextChangeListener()
    }
    
    /**
     * Set up the formatting toolbar buttons
     */
    private fun setupFormatButtons() {
        binding.buttonBold.setOnClickListener {
            isBold = !isBold
            updateButtonStates()
            applyFormatting()
        }
        
        binding.buttonItalic.setOnClickListener {
            isItalic = !isItalic
            updateButtonStates()
            applyFormatting()
        }
        
        binding.buttonUnderline.setOnClickListener {
            isUnderline = !isUnderline
            updateButtonStates()
            applyFormatting()
        }
        
        binding.buttonHighlight.setOnClickListener {
            isHighlighted = !isHighlighted
            updateButtonStates()
            applyFormatting()
        }
        
        binding.buttonClearFormat.setOnClickListener {
            clearFormatting()
        }
    }
    
    /**
     * Update button states based on current formatting
     */
    private fun updateButtonStates() {
        binding.buttonBold.isSelected = isBold
        binding.buttonItalic.isSelected = isItalic
        binding.buttonUnderline.isSelected = isUnderline
        binding.buttonHighlight.isSelected = isHighlighted
    }
    
    /**
     * Set up text change listener
     */
    private fun setupTextChangeListener() {
        binding.editNotes.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    /**
     * Apply formatting to selected text
     */
    private fun applyFormatting() {
        val editText = binding.editNotes
        val selStart = editText.selectionStart
        val selEnd = editText.selectionEnd
        
        if (selStart == selEnd) return // No selection
        
        val editable = editText.text
        
        // Apply formatting based on current state
        if (isBold) {
            editable.setSpan(StyleSpan(Typeface.BOLD), selStart, selEnd, 0)
        }
        
        if (isItalic) {
            editable.setSpan(StyleSpan(Typeface.ITALIC), selStart, selEnd, 0)
        }
        
        if (isUnderline) {
            editable.setSpan(UnderlineSpan(), selStart, selEnd, 0)
        }
        
        if (isHighlighted) {
            val highlightColor = ContextCompat.getColor(context, R.color.colorHighlight)
            editable.setSpan(BackgroundColorSpan(highlightColor), selStart, selEnd, 0)
        }
    }
    
    /**
     * Clear formatting from selected text
     */
    private fun clearFormatting() {
        val editText = binding.editNotes
        val selStart = editText.selectionStart
        val selEnd = editText.selectionEnd
        
        if (selStart == selEnd) return // No selection
        
        val editable = editText.text
        
        // Remove all formatting spans from the selection
        val spans = editable.getSpans(selStart, selEnd, Any::class.java)
        for (span in spans) {
            if (span is StyleSpan || span is UnderlineSpan || span is BackgroundColorSpan || span is ForegroundColorSpan) {
                editable.removeSpan(span)
            }
        }
        
        // Reset formatting states
        isBold = false
        isItalic = false
        isUnderline = false
        isHighlighted = false
        updateButtonStates()
    }
    
    /**
     * Get the current note text with formatting
     */
    fun getNoteText(): CharSequence {
        return binding.editNotes.text
    }
    
    /**
     * Set the note text with formatting
     */
    fun setNoteText(text: CharSequence?) {
        binding.editNotes.setText("")
        if (text != null) {
            if (text is SpannableStringBuilder) {
                binding.editNotes.text = text
            } else {
                binding.editNotes.setText(text.toString())
            }
        }
    }
    
    /**
     * Show/hide the formatting toolbar
     */
    fun setFormattingEnabled(enabled: Boolean) {
        binding.layoutFormatting.visibility = if (enabled) View.VISIBLE else View.GONE
    }
} 