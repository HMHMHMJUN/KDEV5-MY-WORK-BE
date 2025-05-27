package kr.mywork.common.rdb.id;

import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

import java.util.EnumSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import com.fasterxml.uuid.Generators;

public class UnixEpochTimeOrderedIdGenerator implements BeforeExecutionGenerator {
	@Override
	public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
		EventType eventType) {
		return Generators.timeBasedEpochGenerator().generate();
	}

	@Override
	public EnumSet<EventType> getEventTypes() {
		return INSERT_ONLY;
	}
}
