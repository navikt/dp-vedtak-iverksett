package no.nav.dagpenger.vedtak.iverksett.melding

import no.nav.dagpenger.vedtak.iverksett.SakMediator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetMottak
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val sakMediator: SakMediator,
    private val hendelseRepository: HendelseRepository,
) {

    init {
        UtbetalingsvedtakFattetMottak(rapidsConnection, this)
    }

    fun behandle(hendelse: UtbetalingsvedtakFattetHendelse, message: UtbetalingsvedtakFattetHendelseMessage) {
        message.lagreMelding(hendelseRepository)
        sakMediator.håndter(hendelse)
        hendelseRepository.markerSomBehandlet(message.id)
    }
}
