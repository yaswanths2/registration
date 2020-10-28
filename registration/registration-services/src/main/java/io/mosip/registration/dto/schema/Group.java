package io.mosip.registration.dto.schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.mosip.registration.dto.Validator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
	
	private String name;
	private String layout;
	
	@JsonProperty("css-class")
	private String cssClass;
	
	private Validator visible;
	private boolean enabled;
	private int order;
	private List<Field> fields;

}
