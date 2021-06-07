package io.bincloud.common.domain.model.message;

@FunctionalInterface
public interface MessageTransformer {
	/**
	 * Process message before interpolation
	 * 
	 * @param messageToTransform Source message which will be transformed
	 * @return The new message instance after transform or source message if
	 *         transformer can't to transform this message type
	 */
	public MessageTemplate transformMessage(MessageTemplate messageToTransform);
}
