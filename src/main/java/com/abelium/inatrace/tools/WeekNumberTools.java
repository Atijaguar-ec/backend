package com.abelium.inatrace.tools;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Map;

/**
 * Numeracion de semanas por empresa.
 *
 * El esquema se lee de la columna jsonb {@code company.configuration}, clave
 * {@code weekNumberingScheme}. Sin la clave se usa ISO-8601, que es el comportamiento
 * historico y el de todas las empresas menos Fortaleza.
 *
 * FIRST_MONDAY es el calendario de Fortaleza: la semana 1 empieza el primer lunes de
 * enero (5 de enero en 2026) y las semanas corren de lunes a viernes. Sabado y domingo
 * no se trabaja, por lo que no tienen semana asignada y el metodo devuelve null: quien
 * registre una entrega ese dia escribe el numero a mano.
 */
public final class WeekNumberTools {

	public static final String CONFIG_KEY = "weekNumberingScheme";

	public static final String SCHEME_ISO = "ISO";

	public static final String SCHEME_FIRST_MONDAY = "FIRST_MONDAY";

	public static final String COLOR_CONFIG_KEY = "weekColorCodes";

	/**
	 * Ciclo de colores con que se marca cada saco en bodega. Depende solo del numero de
	 * semana, por eso cada año vuelve a empezar en ROJO. Espejo de WEEK_COLORS en
	 * week-number.util.ts.
	 */
	private static final String[] WEEK_COLORS = { "ROJO", "AZUL", "BLANCO", "VERDE", "AMARILLO" };

	private WeekNumberTools() {
	}

	/**
	 * Si la empresa marca sus entregas con el color de la semana.
	 */
	public static boolean weekColorCodesEnabled(Map<String, Object> companyConfiguration) {
		return companyConfiguration != null && Boolean.TRUE.equals(companyConfiguration.get(COLOR_CONFIG_KEY));
	}

	/**
	 * Color que corresponde a la semana, o {@code null} si no hay numero de semana.
	 */
	public static String weekColorName(Integer weekNumber) {

		if (weekNumber == null || weekNumber < 1) {
			return null;
		}

		return WEEK_COLORS[(weekNumber - 1) % WEEK_COLORS.length];
	}

	/**
	 * Esquema configurado para la empresa. Cualquier valor desconocido cae en ISO.
	 */
	public static String schemeOf(Map<String, Object> companyConfiguration) {

		if (companyConfiguration == null) {
			return SCHEME_ISO;
		}

		return SCHEME_FIRST_MONDAY.equals(companyConfiguration.get(CONFIG_KEY))
				? SCHEME_FIRST_MONDAY
				: SCHEME_ISO;
	}

	/**
	 * Numero de semana de la fecha segun el esquema, o {@code null} si el esquema no le
	 * asigna ninguno (fin de semana con FIRST_MONDAY).
	 */
	public static Integer weekNumber(LocalDate date, String scheme) {

		if (date == null) {
			return null;
		}

		if (SCHEME_FIRST_MONDAY.equals(scheme)) {
			return firstMondayWeekNumber(date);
		}

		return date.get(WeekFields.ISO.weekOfWeekBasedYear());
	}

	private static Integer firstMondayWeekNumber(LocalDate date) {

		if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
			return null;
		}

		// Los dias anteriores al primer lunes del año pertenecen a la ultima semana del
		// año anterior: el 1 de enero de 2027 (viernes) es la semana 52 de 2026.
		LocalDate anchor = firstMondayOfYear(date.getYear());
		if (date.isBefore(anchor)) {
			anchor = firstMondayOfYear(date.getYear() - 1);
		}

		return (int) (ChronoUnit.DAYS.between(anchor, date) / 7) + 1;
	}

	private static LocalDate firstMondayOfYear(int year) {
		return LocalDate.of(year, 1, 1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
	}
}
