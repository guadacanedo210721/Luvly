package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CountdownState(
    val days: Long = 0,
    val hours: Long = 0,
    val minutes: Long = 0,
    val seconds: Long = 0,
    val totalSeconds: Long = 0
)

class LoveViewModel(private val repository: LoveRepository) : ViewModel() {

    // Streams from DB
    val profile: StateFlow<RelationshipProfile?> = repository.profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val letters: StateFlow<List<LoveLetter>> = repository.allLetters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<Milestone>> = repository.allMilestones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val songs: StateFlow<List<LoveSong>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reasons: StateFlow<List<LoveReason>> = repository.allReasons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live countdown stream
    private val _countdownState = MutableStateFlow(CountdownState())
    val countdownState: StateFlow<CountdownState> = _countdownState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        // Start live count action based on current profile timestamp
        viewModelScope.launch {
            profile.collect { prof ->
                tickerJob?.cancel()
                if (prof != null) {
                    startCountdownTicker(prof.anniversaryTimestamp)
                }
            }
        }

        // Programmatically insert the user's requested music if not present in existing DB
        viewModelScope.launch {
            songs.filter { it.isNotEmpty() }.firstOrNull()?.let { list ->
                val hasOurTrack = list.any { it.spotifyUrl == "https://www.youtube.com/shorts/1wxU_uPls9U" }
                if (!hasOurTrack) {
                    addSong(
                        title = "Nuestra Canción Especial ✨",
                        artist = "Anónimo / Love Theme",
                        spotifyUrl = "https://www.youtube.com/shorts/1wxU_uPls9U",
                        thoughts = "¡La música especial que me pediste poner! Siempre me recuerda a nosotros y a los tiernos momentos que compartimos. 💕"
                    )
                }
            }
        }
    }

    private fun startCountdownTicker(anniversaryTimestamp: Long) {
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = now - anniversaryTimestamp
                if (diff > 0) {
                    val seconds = diff / 1000 % 60
                    val minutes = diff / (1000 * 60) % 60
                    val hours = diff / (1000 * 60 * 60) % 24
                    val days = diff / (1000 * 60 * 60 * 24)
                    _countdownState.value = CountdownState(
                        days = days,
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds,
                        totalSeconds = diff / 1000
                    )
                } else {
                    // Future date countdown support
                    val absDiff = kotlin.math.abs(diff)
                    val seconds = absDiff / 1000 % 60
                    val minutes = absDiff / (1000 * 60) % 60
                    val hours = absDiff / (1000 * 60 * 60) % 24
                    val days = absDiff / (1000 * 60 * 60 * 24)
                    _countdownState.value = CountdownState(
                        days = -days,
                        hours = -hours,
                        minutes = -minutes,
                        seconds = -seconds,
                        totalSeconds = diff / 1000
                    )
                }
                delay(1000L)
            }
        }
    }

    // PROFILE ACTIONS
    fun updateProfile(userName: String, partnerName: String, anniversaryTimestamp: Long) {
        viewModelScope.launch {
            val current = repository.getProfileDirect()
            val updated = current?.copy(
                userName = userName,
                partnerName = partnerName,
                anniversaryTimestamp = anniversaryTimestamp
            ) ?: RelationshipProfile(
                id = 1,
                userName = userName,
                partnerName = partnerName,
                anniversaryTimestamp = anniversaryTimestamp
            )
            repository.saveProfile(updated)
        }
    }

    // LETTERS ACTIONS
    fun addLetter(title: String, content: String, paperStyle: String, waxSeal: String, photoUri: String? = null) {
        viewModelScope.launch {
            val letter = LoveLetter(
                title = title,
                content = content,
                paperStyle = paperStyle,
                waxSeal = waxSeal,
                photoUri = photoUri,
                timestamp = System.currentTimeMillis(),
                isOpened = false
            )
            repository.saveLetter(letter)
        }
    }

    fun toggleLetterOpened(letter: LoveLetter) {
        viewModelScope.launch {
            repository.updateLetter(letter.copy(isOpened = !letter.isOpened))
        }
    }

    fun deleteLetter(id: Int) {
        viewModelScope.launch {
            repository.deleteLetterById(id)
        }
    }

    // MILESTONES ACTIONS
    fun addMilestone(title: String, dateText: String, description: String, iconName: String, timestamp: Long) {
        viewModelScope.launch {
            val milestone = Milestone(
                title = title,
                dateText = dateText,
                description = description,
                iconName = iconName,
                timestamp = timestamp
            )
            repository.saveMilestone(milestone)
        }
    }

    fun deleteMilestone(id: Int) {
        viewModelScope.launch {
            repository.deleteMilestoneById(id)
        }
    }

    // SONGS ACTIONS
    fun addSong(title: String, artist: String, spotifyUrl: String?, thoughts: String?) {
        viewModelScope.launch {
            val song = LoveSong(
                title = title,
                artist = artist,
                spotifyUrl = spotifyUrl,
                thoughts = thoughts,
                timestamp = System.currentTimeMillis()
            )
            repository.saveSong(song)
        }
    }

    fun deleteSong(id: Int) {
        viewModelScope.launch {
            repository.deleteSongById(id)
        }
    }

    // REASONS ACTIONS
    fun addReason(reasonText: String) {
        viewModelScope.launch {
            val reason = LoveReason(
                reasonText = reasonText,
                timestamp = System.currentTimeMillis()
            )
            repository.saveReason(reason)
        }
    }

    fun deleteReason(id: Int) {
        viewModelScope.launch {
            repository.deleteReasonById(id)
        }
    }
}

// ViewModelFactory
class LoveViewModelFactory(private val repository: LoveRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoveViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
