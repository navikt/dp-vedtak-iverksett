package no.nav.dagpenger.vedtak.iverksett.entitet

data class Beløp(val verdi: Double) {
    init {
        require(verdi >= 0)
    }
}
