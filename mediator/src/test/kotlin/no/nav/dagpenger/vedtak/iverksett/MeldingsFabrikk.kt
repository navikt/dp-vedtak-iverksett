package no.nav.dagpenger.vedtak.iverksett

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun behovOmIverksettingAvUtbetalingsvedtak(
    vedtakId: UUID = UUID.fromString("408f11d9-4be8-450a-8b7a-c2f3f9811859"),
    virkningsdato: LocalDate = 11 juni 2023,
) =
    //language=JSON
    """{
        "@event_name": "behov",
        "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
        "@behov": [
          "IverksettUtbetaling"
        ],
        "ident": "12345678911",
        "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
        "vedtakId": "$vedtakId",
        "tilstand": "Mottatt",
        "IverksettUtbetaling": {
          "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
          "sakId": "${UUID.randomUUID()}",
          "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
          "vedtakstidspunkt": "2019-08-24T14:15:22",
          "virkningsdato": "$virkningsdato",
          "utbetalingsdager": [
            {"dato": "${virkningsdato.minusDays(13)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(12)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(11)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(10)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(9)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(6)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(5)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(4)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(3)}", "beløp": "800"},
            {"dato": "${virkningsdato.minusDays(2)}", "beløp": "800"}
          ],
          "utfall": "Innvilget"
        },
        "behandlingId": "0aaa66b9-35c2-4398-aca0-d1d0a9465292",
        "vedtakstidspunkt": "2019-08-24T14:15:22",
        "virkningsdato": "2019-08-24",
        "utfall": "Innvilget",
        "@id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
        "@opprettet": "2023-05-11T10:02:10.0279828",
        "system_read_count": 0,
        "system_participating_services": [
          {
            "id": "2a49bcc2-2101-435d-83cc-2cc7905041b9",
            "time": "2023-05-11T10:02:10.027982800"
          }
        ]
      }"""

internal fun utbetalingsvedtakFattet(ident: String, vedtakId: UUID, behandlingId: UUID, sakId: String) =
    //language=JSON
    """
        {
          "@event_name": "utbetaling_vedtak_fattet",
          "ident": "$ident",
          "behandlingId": "$behandlingId",
          "sakId": "$sakId",
          "vedtakId": "$vedtakId",
          "vedtaktidspunkt": "${LocalDateTime.MAX}",
          "virkningsdato": "${11 juni 2023}",
          "utbetalingsdager": [
            {
              "dato": "2023-05-29",
              "beløp": "0.0"
            },
            {
              "dato": "2023-05-30",
              "beløp": "0.0"
            },
            {
              "dato": "2023-05-31",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-01",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-02",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-05",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-06",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-07",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-08",
              "beløp": "0.0"
            },
            {
              "dato": "2023-06-09",
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
