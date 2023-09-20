package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMessage
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseRepository
import no.nav.dagpenger.vedtak.iverksett.mottak.IverksattHendelseMessage
import no.nav.dagpenger.vedtak.iverksett.mottak.IverksettUtbetalingsvedtakLøstMottak
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksett.mottak.UtbetalingsvedtakFattetMottak
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val iverksettingMediator: IverksettingMediator,
    private val hendelseRepository: HendelseRepository,
) : IHendelseMediator {

    init {
        UtbetalingsvedtakFattetMottak(rapidsConnection, this)
        IverksettUtbetalingsvedtakLøstMottak(rapidsConnection, this)
    }

    override fun behandle(
        hendelse: IverksattHendelse,
        message: IverksattHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            iverksettingMediator.håndter(it)
        }
    }

    override fun behandle(
        hendelse: UtbetalingsvedtakFattetHendelse,
        message: UtbetalingsvedtakFattetHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            iverksettingMediator.håndter(hendelse)
        }
    }

    private fun <HENDELSE : Hendelse> behandle(
        hendelse: HENDELSE,
        message: HendelseMessage,
        håndter: (HENDELSE) -> Unit,
    ) {
        message.lagreMelding(hendelseRepository)
        håndter(hendelse) // @todo: feilhåndtering
        hendelseRepository.markerSomBehandlet(message.id)
    }
}

internal interface IHendelseMediator {

    fun behandle(
        hendelse: IverksattHendelse,
        message: IverksattHendelseMessage,
        context: MessageContext,
    )

    fun behandle(
        hendelse: UtbetalingsvedtakFattetHendelse,
        message: UtbetalingsvedtakFattetHendelseMessage,
        context: MessageContext,
    )
}
