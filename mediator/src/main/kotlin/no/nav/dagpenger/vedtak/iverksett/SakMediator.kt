package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.client.mapper.IverksettDtoBuilder
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.SakRepository

internal class SakMediator(private val sakRepository: SakRepository, private val iverksettClient: IverksettClient) {
    fun håndter(hendelse: UtbetalingsvedtakFattetHendelse) {
        try {
            val sak = hentEllerOpprettSak(hendelse)
            sak.håndter(hendelse)
            iverksettClient.iverksett(iverksettDto = IverksettDtoBuilder(sak).bygg())
            sakRepository.lagre(sak)
        } catch (err: Aktivitetslogg.AktivitetException) {
            println("Oups aktivitetException! Feil ved håndtering av hendelse")
            //        SakMediator.logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")
            //        withMDC(err.kontekst()) { }
            throw err
        } catch (e: Exception) {
            //        errorHandler(e, e.message ?: "Ukjent feil")
            println("Oups exception! Feil ved håndtering av hendelse")
            throw e
        }
    }

    private fun hentEllerOpprettSak(hendelse: UtbetalingsvedtakFattetHendelse) =
        sakRepository.hent(SakId(hendelse.sakId)) ?: Sak(
            ident = PersonIdentifikator(hendelse.ident()),
            sakId = SakId(hendelse.sakId),
            iverksettinger = mutableListOf(),
        )
}

//    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
//        SakMediator.logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
//        withMDC(context) { SakMediator.sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
//    }
