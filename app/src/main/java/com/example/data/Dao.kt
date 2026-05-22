package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LoveDao {
    // Profile
    @Query("SELECT * FROM relationship_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<RelationshipProfile?>

    @Query("SELECT * FROM relationship_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): RelationshipProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: RelationshipProfile)

    // Letters
    @Query("SELECT * FROM love_letters ORDER BY timestamp DESC")
    fun getAllLettersFlow(): Flow<List<LoveLetter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: LoveLetter)

    @Update
    suspend fun updateLetter(letter: LoveLetter)

    @Query("DELETE FROM love_letters WHERE id = :id")
    suspend fun deleteLetterById(id: Int)

    // Milestones
    @Query("SELECT * FROM relationship_milestones ORDER BY timestamp ASC")
    fun getAllMilestonesFlow(): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone)

    @Query("DELETE FROM relationship_milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: Int)

    // Songs
    @Query("SELECT * FROM love_songs ORDER BY timestamp DESC")
    fun getAllSongsFlow(): Flow<List<LoveSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: LoveSong)

    @Query("DELETE FROM love_songs WHERE id = :id")
    suspend fun deleteSongById(id: Int)

    // Reasons
    @Query("SELECT * FROM love_reasons ORDER BY timestamp DESC")
    fun getAllReasonsFlow(): Flow<List<LoveReason>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReason(reason: LoveReason)

    @Query("DELETE FROM love_reasons WHERE id = :id")
    suspend fun deleteReasonById(id: Int)
}
