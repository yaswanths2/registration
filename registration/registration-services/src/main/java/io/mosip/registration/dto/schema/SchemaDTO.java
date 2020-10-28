package io.mosip.registration.dto.schema;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaDTO {
	
	private List<Screen> screens;

}
