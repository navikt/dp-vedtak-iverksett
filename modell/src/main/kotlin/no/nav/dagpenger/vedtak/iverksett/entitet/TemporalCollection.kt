package no.nav.dagpenger.vedtak.iverksett.entitet

import java.time.LocalDate
import java.time.LocalDateTime

// Temporal object pattern from https://martinfowler.com/eaaDev/TemporalObject.html
internal open class TemporalCollection<R> {
    private val contents = mutableMapOf<LocalDateTime, R>()

    private val milestones get() = contents.keys.toList().reversed()

    fun get(date: LocalDateTime): R = milestones
        .firstOrNull { it.isBefore(date) || it.isEqual(date) }?.let {
            contents[it]
        } ?: throw IllegalArgumentException("No records that early. Asked for date $date. Milestones=$milestones")

    fun get(date: LocalDate): R = get(date.atStartOfDay())

    fun put(at: LocalDateTime, item: R) {
        contents[at] = item
    }

    fun put(at: LocalDate, item: R) {
        put(at.atStartOfDay(), item)
    }

    protected fun historiskeVerdier(til: LocalDate): Collection<R> {
        return contents.filter { it.key.isBefore(til.atStartOfDay()) || it.key == til.atStartOfDay() }.values
    }

    fun harHistorikk() = contents.isNotEmpty()
    override fun toString(): String {
        return "TemporalCollection(contents=$contents)"
    }
}
