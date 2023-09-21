package no.nav.dagpenger.vedtak.iverksett.melding

import no.nav.dagpenger.vedtak.iverksett.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetMottak
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val iverksettingMediator: IverksettingMediator,
    private val hendelseRepository: HendelseRepository,
) {

    init {
        UtbetalingsvedtakFattetMottak(rapidsConnection, this)
    }

    fun behandle(hendelse: UtbetalingsvedtakFattetHendelse, message: UtbetalingsvedtakFattetHendelseMessage) {
        message.lagreMelding(hendelseRepository)
        iverksettingMediator.håndter(hendelse)
        hendelseRepository.markerSomBehandlet(message.id)
    }
}
