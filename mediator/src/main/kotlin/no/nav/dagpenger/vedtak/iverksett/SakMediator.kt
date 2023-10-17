package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.client.mapper.IverksettDtoBuilder
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.SakRepository
import no.nav.helse.rapids_rivers.withMDC

internal class SakMediator(private val sakRepository: SakRepository, private val iverksettClient: IverksettClient) {
    fun håndter(hendelse: UtbetalingsvedtakFattetHendelse) =
        try {
            val sak = hentEllerOpprettSak(hendelse)
            sak.håndter(hendelse)
            iverksettClient.iverksett(iverksettDto = IverksettDtoBuilder(sak).bygg())
            sakRepository.lagre(sak)
        } catch (e: Exception) {
            logError(hendelse, e)
            throw e
        }

    private fun hentEllerOpprettSak(hendelse: UtbetalingsvedtakFattetHendelse) =
        sakRepository.hent(SakId(hendelse.sakId)) ?: Sak(
            ident = PersonIdentifikator(hendelse.ident()),
            sakId = SakId(hendelse.sakId),
            iverksettinger = mutableListOf(),
        )
}

private val logger = KotlinLogging.logger { }
private val sikkerLogger = KotlinLogging.logger("tjenestekall")

private fun logError(
    hendelse: UtbetalingsvedtakFattetHendelse,
    e: Exception,
) {
    withMDC(hendelse.toSpesifikkKontekst().kontekstMap) {
        logger.error("Alvorlig feil med hendelse. VedtakId: ${hendelse.vedtakId}. BehandlingId: ${hendelse.behandlingId}")
        sikkerLogger.error("Alvorlig feil: ${e.stackTrace}\n\t$e.message", e)
    }
}
