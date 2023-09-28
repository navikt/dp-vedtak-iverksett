package no.nav.dagpenger.vedtak.iverksett

import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.SakRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class SakMediator(private val sakRepository: SakRepository, private val iverksettClient: IverksettClient) {
    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        håndter(
            hendelse = utbetalingsvedtakFattetHendelse,
            håndter = håndterIverksettingAv(utbetalingsvedtakFattetHendelse),
        )
    }

    private fun håndterIverksettingAv(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) = { sak: Sak ->
        sak.håndter(utbetalingsvedtakFattetHendelse) // oppdater modell basert på hendelse
        val sakInspektør = SakInspektør(sak)
        val iverksettDto = sakInspektør.byggIverksettDto()
        runBlocking {
            iverksettClient.iverksett(iverksettDto = iverksettDto)
        }
    }

    private fun håndter(hendelse: Hendelse, håndter: (Sak) -> Unit) {
        try {
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

    private fun byggIverksettDto(vedtakIdFilter: UUID, sakInspektør: SakInspektør): IverksettDto {
        println("Bygger IverksettDto for iverksetting av vedtakId $vedtakIdFilter - behandlingId ${sakInspektør.behandlingId}")
        val iverksettDto = IverksettDto(
            saksreferanse = sakInspektør.sakId.sakId,
            behandlingId = sakInspektør.behandlingId,
            personIdent = "12345678901", // TODO
            vedtak = VedtaksdetaljerDto(
                vedtakstype = VedtakType.UTBETALINGSVEDTAK,
                vedtakstidspunkt = LocalDateTime.now(), // TODO: Må hentes ut med visitor
                resultat = Vedtaksresultat.INNVILGET, // TODO: Må hentes ut med visitor
                utbetalinger = finnUtbetalingsdager(sakInspektør),
                saksbehandlerId = "DIGIDAG",
                beslutterId = "DIGIDAG",
                vedtaksperioder = listOf(
                    VedtaksperiodeDto(
                        fraOgMedDato = sakInspektør.virkningsdato,
                    ),
                ),
            ),
        )
        println("IverksettDto: " + iverksettDto)
        return iverksettDto
    }

    private fun finnUtbetalingsdager(sakInspektør: SakInspektør): List<UtbetalingDto> {
        val utbetalingerMutable = mutableListOf<UtbetalingDto>()
        val alleUtbetalingsdagerMap = mutableMapOf<LocalDate, Double>()

        for (i in 0 until sakInspektør.iverksettingsdager.size) {
            alleUtbetalingsdagerMap.put(
                sakInspektør.iverksettingsdager[i].dato,
                sakInspektør.iverksettingsdager[i].beløp.verdi,
            )
        }

        alleUtbetalingsdagerMap.forEach { entry ->
            utbetalingerMutable.add(
                UtbetalingDto(
                    belopPerDag = entry.value.toInt(),
                    fraOgMedDato = entry.key,
                    tilOgMedDato = entry.key,
                ),
            )
        }

        val utbetalinger: List<UtbetalingDto> = utbetalingerMutable

        utbetalinger.forEach { utbetaling -> println("fom=${utbetaling.fraOgMedDato} beløp=${utbetaling.belopPerDag}") }
        return utbetalinger
    }

//    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
//        SakMediator.logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
//        withMDC(context) { SakMediator.sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
//    }
}
