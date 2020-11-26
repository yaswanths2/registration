package io.mosip.registration.dto.response;


import java.time.LocalDateTime;
import java.util.List;

import io.mosip.registration.dto.Field;
import io.mosip.registration.dto.schema.SchemaDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UiSchemaDTO {

	private String id;
	private double idVersion;
	private SchemaDTO schema;
	private String schemaJson;
	private LocalDateTime effectiveFrom;
}
