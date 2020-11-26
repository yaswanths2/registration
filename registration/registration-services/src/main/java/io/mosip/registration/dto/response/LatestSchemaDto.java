package io.mosip.registration.dto.response;

import java.time.LocalDateTime;

import io.mosip.registration.dto.schema.SchemaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LatestSchemaDto {

	private String id;
	private double idVersion;
	private SchemaDTO schema;
	private String schemaJson;
	private LocalDateTime effectiveFrom;
	
}
