package io.bce.text;

import javax.validation.constraints.NotNull;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Text {
	private TextProcessor textProcessor = TextProcessor.create();
	
	/**
	 * Get the text processor
	 * 
	 * @return text processor
	 */
	public final TextProcessor processor() {
		return textProcessor;
	}
	
	/**
	 * Interpolate text template
	 * 
	 * @param textTemplate The text template
	 * @return The interpolated text
	 */
	public final String interpolate(TextTemplate textTemplate) {
		return processor().interpolate(textTemplate);
	}
	
	/**
	 * Configure the processor
	 * 
	 * @param textProcessor The text processor
	 */
	public final void configureProcessor(@NotNull TextProcessor textProcessor) {
		Text.textProcessor = textProcessor;
	}
}