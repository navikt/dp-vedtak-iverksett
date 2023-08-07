package no.nav.dagpenger.vedtak.iverksett

import java.time.LocalDate
import java.util.UUID

fun behovOmIverksettingAvRammevedtak(ident: String = "12345123451", virkningsdato: LocalDate = 29 mai 2023) =
    // language=JSON
    """
            {
              "@event_name": "behov",
              "@behovId": "5456758f-d0cb-4266-8e2f-8dc3e963c8f4",
              "@behov": [
                "Iverksett"
              ],
              "iverksettingId": "ac4aee05-61fb-43f5-9fad-3e2fc5ec5aeb",
              "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
              "ident": "$ident",
              "tilstand": "Mottatt",
              "Iverksett": {
                "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
                "behandlingId": "2c930751-5c4e-4bbd-80cc-1b3ab36eabf5",
                "vedtakstidspunkt": "+999999999-12-31T23:59:59.999999999",
                "virkningsdato": "2019-08-24",
                "utfall": "Innvilget"
              },
              "behandlingId": "2c930751-5c4e-4bbd-80cc-1b3ab36eabf5",
              "vedtakstidspunkt": "+999999999-12-31T23:59:59.999999999",
              "virkningsdato": "$virkningsdato",
              "utfall": "Innvilget",
              "@id": "723784aa-d01f-4448-a96d-272d49a88775",
              "@opprettet": "2023-06-19T10:56:08.627225",
              "system_read_count": 0,
              "system_participating_services": [
                {
                  "id": "723784aa-d01f-4448-a96d-272d49a88775",
                  "time": "2023-06-19T10:56:08.627225"
                }
              ]
            }
    """.trimIndent()

fun behovOmIverksettingAvUtbetalingsvedtak(
    vedtakId: UUID = UUID.fromString("408f11d9-4be8-450a-8b7a-c2f3f9811859"),
    virkningsdato: LocalDate = 11 juni 2023,
) =
    //language=JSON
    """{
        "@event_name": "behov",
        "@behovId": "fe6fb8ee-cbc7-46bf-a5d7-fb9b57b279c4",
        "@behov": [
          "Iverksett"
        ],
        "ident": "12345678911",
        "iverksettingId": "0b853210-cc2b-45d8-9c35-72b39fa1d7f3",
        "vedtakId": "$vedtakId",
        "tilstand": "Mottatt",
        "Iverksett": {
          "vedtakId": "408f11d9-4be8-450a-8b7a-c2f3f9811859",
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
