package com.abelium.inatrace.tools;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Las fechas esperadas salen del calendario que entregó Fortaleza, no de la norma ISO.
 * El frontend repite este mismo cálculo en week-number.util.ts: si cambia una regla acá,
 * cambia allá también.
 */
class WeekNumberToolsTest {

	@Test
	void shouldStartTheYearOnTheFirstMondayOfJanuary() {

		// 2026 arranca en jueves; el primer lunes es el 5 de enero
		assertEquals(1, week(LocalDate.of(2026, 1, 5)));
		assertEquals(1, week(LocalDate.of(2026, 1, 9)));
		assertEquals(2, week(LocalDate.of(2026, 1, 12)));
	}

	@Test
	void shouldNumberTheWeekReportedAsWrongByTheClient() {

		// El caso concreto del reporte: con ISO daba 36, el calendario de Fortaleza
		// pone el 2 de septiembre en la semana del 31 de agosto al 4 de septiembre.
		assertEquals(35, week(LocalDate.of(2026, 9, 2)));
		assertEquals(36, week(LocalDate.of(2026, 9, 7)));

		// La misma fecha bajo ISO sigue devolviendo 36 para el resto de las empresas
		assertEquals(36, WeekNumberTools.weekNumber(LocalDate.of(2026, 9, 2), WeekNumberTools.SCHEME_ISO));
	}

	@Test
	void shouldKeepTheDaysBeforeTheFirstMondayInThePreviousYearLastWeek() {

		// La tabla del cliente termina la semana 52 el 1 de enero de 2027
		assertEquals(52, week(LocalDate.of(2026, 12, 28)));
		assertEquals(52, week(LocalDate.of(2027, 1, 1)));
	}

	@Test
	void shouldNotAssignAWeekOnWeekends() {

		// Sábado y domingo no se trabaja: sin número, se escribe a mano
		assertNull(week(LocalDate.of(2026, 9, 5)));
		assertNull(week(LocalDate.of(2026, 9, 6)));
	}

	@Test
	void shouldFallBackToIsoWhenTheCompanyHasNoSchemeConfigured() {

		assertEquals(WeekNumberTools.SCHEME_ISO, WeekNumberTools.schemeOf(null));
		assertEquals(WeekNumberTools.SCHEME_ISO, WeekNumberTools.schemeOf(new HashMap<>()));

		Map<String, Object> unknown = new HashMap<>();
		unknown.put(WeekNumberTools.CONFIG_KEY, "CUALQUIER_COSA");
		assertEquals(WeekNumberTools.SCHEME_ISO, WeekNumberTools.schemeOf(unknown));

		Map<String, Object> fortaleza = new HashMap<>();
		fortaleza.put(WeekNumberTools.CONFIG_KEY, "FIRST_MONDAY");
		assertEquals(WeekNumberTools.SCHEME_FIRST_MONDAY, WeekNumberTools.schemeOf(fortaleza));
	}

	@Test
	void shouldReturnNullWithoutADate() {

		assertNull(WeekNumberTools.weekNumber(null, WeekNumberTools.SCHEME_FIRST_MONDAY));
		assertNull(WeekNumberTools.weekNumber(null, WeekNumberTools.SCHEME_ISO));
	}

	private Integer week(LocalDate date) {
		return WeekNumberTools.weekNumber(date, WeekNumberTools.SCHEME_FIRST_MONDAY);
	}
}
