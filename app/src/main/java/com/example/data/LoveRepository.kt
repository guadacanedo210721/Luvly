package com.example.data

import kotlinx.coroutines.flow.Flow

class LoveRepository(private val loveDao: LoveDao) {

    val profile: Flow<RelationshipProfile?> = loveDao.getProfileFlow()

    suspend fun getProfileDirect(): RelationshipProfile? = loveDao.getProfileDirect()

    suspend fun saveProfile(profile: RelationshipProfile) {
        loveDao.insertOrUpdateProfile(profile)
    }

    val allLetters: Flow<List<LoveLetter>> = loveDao.getAllLettersFlow()

    suspend fun saveLetter(letter: LoveLetter) {
        loveDao.insertLetter(letter)
    }

    suspend fun updateLetter(letter: LoveLetter) {
        loveDao.updateLetter(letter)
    }

    suspend fun deleteLetterById(id: Int) {
        loveDao.deleteLetterById(id)
    }

    val allMilestones: Flow<List<Milestone>> = loveDao.getAllMilestonesFlow()

    suspend fun saveMilestone(milestone: Milestone) {
        loveDao.insertMilestone(milestone)
    }

    suspend fun deleteMilestoneById(id: Int) {
        loveDao.deleteMilestoneById(id)
    }

    val allSongs: Flow<List<LoveSong>> = loveDao.getAllSongsFlow()

    suspend fun saveSong(song: LoveSong) {
        loveDao.insertSong(song)
    }

    suspend fun deleteSongById(id: Int) {
        loveDao.deleteSongById(id)
    }

    val allReasons: Flow<List<LoveReason>> = loveDao.getAllReasonsFlow()

    suspend fun saveReason(reason: LoveReason) {
        loveDao.insertReason(reason)
    }

    suspend fun deleteReasonById(id: Int) {
        loveDao.deleteReasonById(id)
    }
}
