package com.example.data.repository

import com.example.data.dao.SignalDao
import com.example.data.model.SignalEntity
import kotlinx.coroutines.flow.Flow

class SignalRepository(private val signalDao: SignalDao) {
    val allSignals: Flow<List<SignalEntity>> = signalDao.getAllSignals()

    suspend fun insert(signal: SignalEntity): Long {
        return signalDao.insertSignal(signal)
    }

    suspend fun deleteById(id: Int) {
        signalDao.deleteSignalById(id)
    }

    suspend fun updateOutcome(id: Int, outcome: String) {
        signalDao.updateSignalOutcome(id, outcome)
    }

    suspend fun clearAll() {
        signalDao.clearAllSignals()
    }
}
