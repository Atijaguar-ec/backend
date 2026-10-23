package com.abelium.inatrace.components.company.plotimport;

import com.abelium.inatrace.components.company.types.PlotGeoJsonImportIssueType;
import com.abelium.inatrace.tools.TranslateTools;
import com.abelium.inatrace.types.Language;
import org.springframework.context.MessageSource;

/**
 * The GeoJSON plot import messages, in the language of the request.
 *
 * A problem with the file as a whole is reported to the operator as an
 * {@link com.abelium.inatrace.api.errors.ApiException}, which carries plain text and no
 * key, and the frontend shows that text verbatim. So the text has to be translated here;
 * the per-feature issues travel to the frontend as {@link PlotGeoJsonImportIssueType}
 * and are translated there instead.
 *
 * Keys live in {@code i18n/messages_*.properties} under {@code plotImport.}.
 */
public final class PlotImportMessages {

	private final MessageSource messageSource;

	private final Language language;

	public PlotImportMessages(MessageSource messageSource, Language language) {
		this.messageSource = messageSource;
		this.language = language == null ? Language.EN : language;
	}

	public String get(String key, Object... args) {
		return TranslateTools.getTranslatedValue(messageSource, key, language, args);
	}

	/** The reason a feature cannot become a plot, for the messages that list them. */
	public String issue(PlotGeoJsonImportIssueType issue) {
		return get("plotImport.issue." + issue.name());
	}

}
