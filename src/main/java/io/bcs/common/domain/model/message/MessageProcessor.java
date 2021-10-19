package io.bcs.common.domain.model.message;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MessageProcessor {
	private final List<MessageTransformer> transformers = new ArrayList<MessageTransformer>();
	
	private MessageProcessor(List<MessageTransformer> transformers) {
		super();
		this.transformers.addAll(transformers);
	}
	
	public MessageTemplate process(MessageTemplate messageTemplate) {
		MessageTemplate result = messageTemplate;
		for (MessageTransformer transformer : this.transformers) {
			result = transformer.transformMessage(result);
		}
		return result;
	}
	
	public String interpolate(MessageTemplate messageTemplte) {
		return process(messageTemplte).getText();
	}
	
	public Configurer configure() {
		return new Configurer(transformers);
	}
	
	public static class Configurer {
		private final List<MessageTransformer> transformers = new ArrayList<MessageTransformer>();
		
		public Configurer(List<MessageTransformer> transformers) {
			super();
			this.transformers.addAll(transformers);
		}
		/**
		 * Append transformer to I18n component
		 *  
		 * @param transformer The message transformer
		 * @return 
		 */
		public Configurer withTransformation(MessageTransformer transformer) {
			this.transformers.add(transformer);
			return this;
		}
		/**
		 * Apply transformations
		 * 
		 * @return 
		 */
		public MessageProcessor apply() {
			return new MessageProcessor(this.transformers);
		}
	}
}
