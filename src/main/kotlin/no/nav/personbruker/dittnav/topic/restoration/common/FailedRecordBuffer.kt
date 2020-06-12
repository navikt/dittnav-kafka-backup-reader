package no.nav.personbruker.dittnav.topic.restoration.common

import java.util.concurrent.locks.ReentrantLock


class FailedRecordBuffer <T> {
    private val buffer = ArrayList<RecordKeyValueWrapper<T>>()
    private val lock = ReentrantLock()

    fun addRecord(record: RecordKeyValueWrapper<T>) {
        lock.lock()
        buffer.add(record)
        lock.unlock()
    }

    fun hasRecords(): Boolean {
        lock.lock()
        val hasRecords = buffer.isNotEmpty()
        lock.unlock()
        return hasRecords
    }

    fun getAndFlushRecords(): List<RecordKeyValueWrapper<T>> {
        lock.lock()
        val records = buffer.copy()
        buffer.clear()
        lock.unlock()
        return records
    }

    private fun List<RecordKeyValueWrapper<T>>.copy() = toList()
}