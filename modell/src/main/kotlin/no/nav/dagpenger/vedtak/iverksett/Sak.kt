package no.nav.dagpenger.vedtak.iverksett

class Sak(
    private val ident: PersonIdentifikator,
    private val sakId: String,
    private val iverksettingHistorikk: IverksettingHistorikk,
)

class IverksettingHistorikk(private val iverksettinger: MutableList<Iverksetting>)
