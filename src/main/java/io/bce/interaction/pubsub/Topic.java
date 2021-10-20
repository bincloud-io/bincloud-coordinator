package io.bce.interaction.pubsub;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class Topic {
	private static final Pattern TOPIC_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.]+$");
	
	private final String name;

	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Create the topic object
	 * @param name The topic name string value
	 * @return The topic
	 */
	public static final Topic ofName(@NonNull String name) {
		checkThatTopicIsMatchedToPattern(name);
		return new Topic(name);
	}
	
	private static final void checkThatTopicIsMatchedToPattern(String name) {
		if (!TOPIC_PATTERN.matcher(name).matches()) {
			throw new WrongTopicNameFormatException(name);
		}
	}
	
	public static class WrongTopicNameFormatException extends RuntimeException {
		private static final long serialVersionUID = -3671386257899005216L;

		public WrongTopicNameFormatException(String topicName) {
			super(String.format("The topic name \"%s\" has wrong format", topicName));
		}		
	}
}
