package io.bincloud.common.domain.model.logging.event;

import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "log_details")
public class EventAuditParameter {
	@Id
	@EqualsAndHashCode.Include
	private Long id;
	
	@Column(name = "detail_key")
	private String key;
	
	@Column(name = "detail_value", length = 1024)
	private String value;
	
	public EventAuditParameter(SequentialGenerator<Long> idGenerator, Entry<String, String> parameterEntry) {
		super();
		this.id = idGenerator.nextValue();
		this.key = parameterEntry.getKey();
		this.value = parameterEntry.getValue();
	}
}
