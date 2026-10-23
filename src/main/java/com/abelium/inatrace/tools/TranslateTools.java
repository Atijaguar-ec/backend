package com.abelium.inatrace.tools;

import com.abelium.inatrace.types.Language;
import org.springframework.context.MessageSource;

import java.util.Locale;

public class TranslateTools {

	public static String getTranslatedValue(MessageSource messageSource, String messageId, Language language) {
		return messageSource.getMessage(messageId, null, Locale.forLanguageTag(language.name()));
	}

	/**
	 * The same, for messages with placeholders. With arguments the bundle value goes
	 * through {@link java.text.MessageFormat}, where a literal apostrophe has to be
	 * written twice.
	 */
	public static String getTranslatedValue(MessageSource messageSource, String messageId, Language language, Object... args) {
		return messageSource.getMessage(messageId, args, Locale.forLanguageTag(language.name()));
	}
}
