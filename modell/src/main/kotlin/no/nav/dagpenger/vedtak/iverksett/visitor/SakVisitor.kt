package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.vedtak.iverksett.SakId

interface SakVisitor {
    fun visitSak(sakId: SakId) {}
}
