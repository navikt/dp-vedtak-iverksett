package no.nav.dagpenger.vedtak.iverksett

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal fun utbetalingsvedtakFattet(ident: String, virkningsdato: LocalDate, dagsbeløp: Double, sakId: SakId) =
    //language=JSON
    """
        {
          "@event_name": "utbetaling_vedtak_fattet",
          "ident": "$ident",
          "behandlingId": "${UUID.randomUUID()}",
          "sakId": "$sakId",
          "vedtakId": "${UUID.randomUUID()}",
          "vedtaktidspunkt": "${LocalDateTime.now()}",
          "virkningsdato": "$virkningsdato",
          "utbetalingsdager": [
            {
              "dato": "${virkningsdato.minusDays(13)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(12)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(11)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(10)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(9)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(8)}",
              "beløp": "0.0"
            },
            {
              "dato": "${virkningsdato.minusDays(7)}",
              "beløp": "0.0"
            },
            {
              "dato": "${virkningsdato.minusDays(6)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(5)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(4)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(3)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(2)}",
              "beløp": "$dagsbeløp"
            },
            {
              "dato": "${virkningsdato.minusDays(1)}",
              "beløp": "0.0"
            },
            {
              "dato": "$virkningsdato",
              "beløp": "0.0"
            }
          ],
          "utfall": "Innvilget",
          "@id": "418a136f-196b-45fe-8c45-76730d88ebd5",
          "@opprettet": "2023-06-15T19:24:58.050467",
          "system_read_count": 0,
          "system_participating_services": [
            {
              "id": "418a136f-196b-45fe-8c45-76730d88ebd5",
              "time": "2023-06-15T19:24:58.050467"
            }
          ]
        }
    """.trimIndent()
