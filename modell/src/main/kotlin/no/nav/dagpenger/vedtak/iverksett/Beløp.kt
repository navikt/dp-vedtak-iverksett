package no.nav.dagpenger.vedtak.iverksett

data class Beløp(val verdi: Double) {
    init {
        require(verdi >= 0)
    }
}
