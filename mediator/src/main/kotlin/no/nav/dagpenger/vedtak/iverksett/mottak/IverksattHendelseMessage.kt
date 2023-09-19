package no.nav.dagpenger.vedtak.iverksett.mottak

import no.nav.dagpenger.vedtak.iverksett.IHendelseMediator
import no.nav.dagpenger.vedtak.iverksett.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMessage
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class IverksattHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    override val ident: String = packet["ident"].asText()

    private val hendelse
        get() = IverksattHendelse(
            meldingsreferanseId = id,
            ident = ident,
            iverksettingId = packet["iverksettingId"].asUUID(),
            vedtakId = packet["vedtakId"].asUUID(),
        )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
