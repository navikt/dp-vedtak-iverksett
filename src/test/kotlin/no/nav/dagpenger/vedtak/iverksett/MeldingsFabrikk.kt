package no.nav.dagpenger.vedtak.iverksett

import java.time.LocalDate

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
                "utbetalingsdager": [],
                "utfall": "Innvilget"
              },
              "behandlingId": "2c930751-5c4e-4bbd-80cc-1b3ab36eabf5",
              "vedtakstidspunkt": "+999999999-12-31T23:59:59.999999999",
              "virkningsdato": "$virkningsdato",
              "utbetalingsdager": [],
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
