package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "relationship_profile")
data class RelationshipProfile(
    @PrimaryKey val id: Int = 1,
    val userName: String,
    val partnerName: String,
    val anniversaryTimestamp: Long, // timestamp when they started dating
    val profileImageUri: String? = null // optional path/uri
)

@Entity(tableName = "love_letters")
data class LoveLetter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val paperStyle: String = "romance_pink", // romance_pink, vintage_beige, sweet_lavender, midnight_dark, warm_peach
    val waxSeal: String = "heart",         // heart, rose, forever, butterfly
    val photoUri: String? = null,           // option to attach photo
    val timestamp: Long = System.currentTimeMillis(),
    val isOpened: Boolean = false
)

@Entity(tableName = "relationship_milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateText: String,             // text representation like "14 Oct 2023"
    val description: String,
    val iconName: String = "favorite", // favorite, flight, cafe, explore, event
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "love_songs")
data class LoveSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artist: String,
    val spotifyUrl: String? = null,
    val thoughts: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "love_reasons")
data class LoveReason(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reasonText: String,
    val timestamp: Long = System.currentTimeMillis()
)
