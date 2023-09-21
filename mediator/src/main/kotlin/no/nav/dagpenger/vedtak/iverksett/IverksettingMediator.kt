package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.IverksettingRepository
import no.nav.helse.rapids_rivers.withMDC

internal class IverksettingMediator(
    private val iverksettingRepository: IverksettingRepository,
    private val aktivitetsloggMediator: AktivitetsloggMediator,
    private val behovMediator: BehovMediator,
) {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettingMediator")
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        håndter(utbetalingsvedtakFattetHendelse) { iverksetting ->
            iverksetting.håndter(utbetalingsvedtakFattetHendelse)
        }
    }

    private fun håndter(hendelse: Hendelse, håndter: (Iverksetting) -> Unit) = try {
        val iverksetting = hentEllerOpprettIverksetting(hendelse)
        håndter(iverksetting)
        iverksettingRepository.lagre(iverksetting)
        ferdigstill(hendelse)
    } catch (err: Aktivitetslogg.AktivitetException) {
        logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")
        withMDC(err.kontekst()) {
            sikkerLogger.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
        }
        throw err
    } catch (oie: OpprettIverksettingException) {
        sikkerLogger.error { oie }
    } catch (e: Exception) {
        errorHandler(e, e.message ?: "Ukjent feil")
        throw e
    }

    private fun hentEllerOpprettIverksetting(hendelse: Hendelse): Iverksetting {
        return when (hendelse) {
            is UtbetalingsvedtakFattetHendelse -> iverksettingRepository.hent(hendelse.vedtakId)
                ?: Iverksetting(hendelse.vedtakId, hendelse.ident(), hendelse.utbetalingsdager.map { IverksettingDag(dato = it.dato, beløp = Beløp.fra(it.beløp.toBigDecimal())) }.toMutableList())

            else -> {
                TODO("Støtter bare UtbetalingsvedtakFattetHendelse pt")
            }
        }
    }

    class OpprettIverksettingException(message: String) : RuntimeException(message)

    private fun ferdigstill(hendelse: Hendelse) {
        if (!hendelse.harAktiviteter()) return
        if (hendelse.harFunksjonelleFeilEllerVerre()) {
            logger.info("aktivitetslogg inneholder feil (se sikkerlogg)")
            sikkerLogger.error("aktivitetslogg inneholder feil:\n${hendelse.toLogString()}")
        } else {
            sikkerLogger.info("aktivitetslogg inneholder meldinger:\n${hendelse.toLogString()}")
        }
        sikkerLogger.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        behovMediator.håndter(hendelse)
        aktivitetsloggMediator.håndter(hendelse)
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }
}
