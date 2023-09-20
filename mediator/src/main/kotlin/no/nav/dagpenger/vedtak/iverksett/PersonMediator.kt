package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.persistens.PersonRepository
import no.nav.helse.rapids_rivers.withMDC

internal class PersonMediator(
    private val personRepository: PersonRepository,
    private val aktivitetsloggMediator: AktivitetsloggMediator,
    private val personObservers: List<PersonObserver> = emptyList(),
) {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall")
    }

    private fun lagre(person: Person) {
        personRepository.lagre(person)
    }

    private fun hentEllerOpprettPerson(hendelse: Hendelse): Person {
        val person = personRepository.hent(hendelse.ident().tilPersonIdentfikator())
        return when (hendelse) {
            is UtbetalingsvedtakFattetHendelse -> person ?: Person(hendelse.ident().tilPersonIdentfikator())
            else -> person ?: Person(PersonIdentifikator("12345123451"))
                .also { logger.error { "Oppretter default person 👨🏽" } } // TODO: Fjern når vi har database
        }
    }

    private fun ferdigstill(hendelse: Hendelse) {
        if (!hendelse.harAktiviteter()) return
        if (hendelse.harFunksjonelleFeilEllerVerre()) {
            logger.info("aktivitetslogg inneholder feil (se sikkerlogg)")
            sikkerLogger.error("aktivitetslogg inneholder feil:\n${hendelse.toLogString()}")
        } else {
            sikkerLogger.info("aktivitetslogg inneholder meldinger:\n${hendelse.toLogString()}")
        }
        sikkerLogger.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
        aktivitetsloggMediator.håndter(hendelse)
    }

    private fun errorHandler(err: Exception, message: String, context: Map<String, String> = emptyMap()) {
        logger.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerLogger.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }

    private fun behandle(hendelse: Hendelse, håndter: (Person) -> Unit) = try {
        val person = hentEllerOpprettPerson(hendelse)
        val delegatedObserver = DelegatedObserver(personObservers)
        person.addObserver(delegatedObserver)
        håndter(person)
        lagre(person)
        ferdigstill(hendelse).also {
            delegatedObserver.ferdigstill()
        }
    } catch (err: Aktivitetslogg.AktivitetException) {
        logger.error("alvorlig feil i aktivitetslogg (se sikkerlogg for detaljer)")

        withMDC(err.kontekst()) {
            sikkerLogger.error("alvorlig feil i aktivitetslogg: ${err.message}", err)
        }
        throw err
    } catch (e: Exception) {
        errorHandler(e, e.message ?: "Ukjent feil")
        throw e
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        behandle(utbetalingsvedtakFattetHendelse) { person ->
            person.håndter(utbetalingsvedtakFattetHendelse)
        }
    }
}

private class DelegatedObserver(private val observers: List<PersonObserver>) : PersonObserver {

    private val iverksettingDelegate = mutableListOf<Pair<String, IverksettingObserver.UtbetalingsvedtakIverksatt>>()

    override fun utbetalingsvedtakIverksatt(
        ident: String,
        utbetalingsvedtakIverksatt: IverksettingObserver.UtbetalingsvedtakIverksatt,
    ) {
        iverksettingDelegate.add(Pair(ident, utbetalingsvedtakIverksatt))
    }

    fun ferdigstill() {
        iverksettingDelegate.forEach { (ident, utbetalingsvedtakIverksatt) ->
            observers.forEach {
                it.utbetalingsvedtakIverksatt(ident, utbetalingsvedtakIverksatt)
            }
        }
    }
}
