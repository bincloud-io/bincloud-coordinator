package io.bincloud.common.domain.model.logging.event;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.domain.model.logging.Level;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class EventAuditRecord {
	private Long id;
	private Level auditLevel;
	private LocalDateTime auditDate;
	private String eventCode;
	private String errorCode;
	private String message;
	@Builder.Default
	private Set<EventAuditParameter> parameters = new HashSet<EventAuditParameter>();

	public EventAuditRecord(@NonNull SequentialGenerator<Long> idGenerator, @NonNull EventDetails auditDetails) {
		this();
		this.id = idGenerator.nextValue();
		this.auditLevel = auditDetails.getAuditLevel();
		this.auditDate = auditDetails.getAuditDate();
		this.eventCode = auditDetails.getEventCode();
		this.errorCode = auditDetails.getErrorCode();
		this.message = auditDetails.getAuditMessage();
		this.parameters = createAuditParameters(idGenerator, auditDetails.getAuditParameters());
	}

	public interface EventDetails {
		public Level getAuditLevel();
		public LocalDateTime getAuditDate();
		public String getAuditMessage();
		public String getEventCode();
		public String getErrorCode();
		public Map<String, String> getAuditParameters();
	}

	private Set<EventAuditParameter> createAuditParameters(final SequentialGenerator<Long> idGenerator,
			final Map<String, String> parameters) {
		return parameters.entrySet().stream().map(entry -> new EventAuditParameter(idGenerator, entry))
				.collect(Collectors.toSet());
	}
}
