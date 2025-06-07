package com.akanksha.expensecalculator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class to manage tags in the expense calculator app.
 * Provides functionality for tag suggestions, frequency tracking, and persistence.
 */
class TagsManager private constructor(private val context: Context) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    private val _allTags = MutableStateFlow<Set<String>>(emptySet())
    val allTags: StateFlow<Set<String>> = _allTags
    
    private val _popularTags = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val popularTags: StateFlow<List<Pair<String, Int>>> = _popularTags
    
    // Map to track tag usage frequency
    private val tagFrequency = ConcurrentHashMap<String, Int>()
    
    init {
        loadTags()
    }
    
    /**
     * Load saved tags from SharedPreferences
     */
    private fun loadTags() {
        val savedTags = prefs.getStringSet(KEY_ALL_TAGS, emptySet()) ?: emptySet()
        _allTags.value = savedTags
        
        // Load tag frequencies
        savedTags.forEach { tag ->
            val frequency = prefs.getInt("$KEY_TAG_FREQUENCY$tag", 0)
            if (frequency > 0) {
                tagFrequency[tag] = frequency
            }
        }
        
        updatePopularTags()
    }
    
    /**
     * Update the list of popular tags based on frequency
     */
    private fun updatePopularTags() {
        _popularTags.value = tagFrequency.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }
    }
    
    /**
     * Add a new tag or update an existing tag's frequency
     */
    fun addTag(tag: String) {
        val normalizedTag = tag.trim().lowercase()
        if (normalizedTag.isEmpty()) return
        
        val currentTags = _allTags.value.toMutableSet()
        currentTags.add(normalizedTag)
        _allTags.value = currentTags
        
        // Update frequency
        val currentFrequency = tagFrequency[normalizedTag] ?: 0
        tagFrequency[normalizedTag] = currentFrequency + 1
        
        // Save changes
        saveTags()
        updatePopularTags()
    }
    
    /**
     * Add multiple tags at once
     */
    fun addTags(tags: List<String>) {
        tags.forEach { addTag(it) }
    }
    
    /**
     * Remove a tag
     */
    fun removeTag(tag: String) {
        val normalizedTag = tag.trim().lowercase()
        val currentTags = _allTags.value.toMutableSet()
        currentTags.remove(normalizedTag)
        _allTags.value = currentTags
        
        tagFrequency.remove(normalizedTag)
        
        // Save changes
        saveTags()
        updatePopularTags()
    }
    
    /**
     * Get tag suggestions based on prefix
     */
    fun getSuggestions(prefix: String, limit: Int = 5): List<String> {
        if (prefix.length < 2) return emptyList()
        
        val normalizedPrefix = prefix.trim().lowercase()
        return _allTags.value
            .filter { it.startsWith(normalizedPrefix) }
            .sortedByDescending { tagFrequency[it] ?: 0 }
            .take(limit)
    }
    
    /**
     * Save tags to SharedPreferences
     */
    private fun saveTags() {
        prefs.edit().apply {
            putStringSet(KEY_ALL_TAGS, _allTags.value)
            
            // Save frequencies
            tagFrequency.forEach { (tag, frequency) ->
                putInt("$KEY_TAG_FREQUENCY$tag", frequency)
            }
        }.apply()
    }
    
    /**
     * Get the most frequently used tags
     */
    fun getPopularTags(limit: Int = 10): List<String> {
        return _popularTags.value.take(limit).map { it.first }
    }
    
    companion object {
        private const val PREF_NAME = "tags_preferences"
        private const val KEY_ALL_TAGS = "all_tags"
        private const val KEY_TAG_FREQUENCY = "tag_frequency_"
        
        @Volatile
        private var INSTANCE: TagsManager? = null
        
        fun getInstance(context: Context): TagsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TagsManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
} 