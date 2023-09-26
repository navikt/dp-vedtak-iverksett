package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.SakRepository

class SakMediator(private val sakRepository: SakRepository) {
    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        håndter(utbetalingsvedtakFattetHendelse) { sak ->
            sak.håndter(utbetalingsvedtakFattetHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, håndter: (Sak) -> Unit) = try {
        val sak = hentEllerOpprettSak(hendelse)
        håndter(sak)
        sakRepository.lagre(sak)
    } catch (err: Aktivitetslogg.AktivitetException) {
        println("Oups aktivitetException! Feil ved håndtering av hendelse")
//        SakMediator.logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")
//        withMDC(err.kontekst()) {
//            IverksettingMediator.sikkerLogger.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
//        }
        throw err
    } catch (e: Exception) {
//        errorHandler(e, e.message ?: "Ukjent feil")
        println("Oups exception! Feil ved håndtering av hendelse")
        throw e
    }

    private fun hentEllerOpprettSak(hendelse: Hendelse): Sak {
        return when (hendelse) {
            is UtbetalingsvedtakFattetHendelse -> sakRepository.hent(SakId(hendelse.sakId))
                ?: Sak(
                    ident = PersonIdentifikator(hendelse.ident()),
                    sakId = SakId(hendelse.sakId),
                    iverksettinger = mutableListOf(),
                )

            else -> {
                TODO("Støtter bare UtbetalingsvedtakFattetHendelse pt")
            }
        }
    }

//    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
//        SakMediator.logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
//        withMDC(context) { SakMediator.sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
//    }
}
