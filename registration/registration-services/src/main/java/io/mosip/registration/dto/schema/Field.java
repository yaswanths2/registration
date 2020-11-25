package io.mosip.registration.dto.schema;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.mosip.registration.dto.RequiredOnExpr;
import io.mosip.registration.dto.Validator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
	
	private String id;
	private String order;
	private boolean inputRequired;
	private String type;
	private int minimum;
	private int maximum;
	private String description;
	private HashMap<String, String> label;
	private String controlType;
	private String fieldType;
	private String format;
	private List<Validator> validators;
	private String fieldCategory;
	private String contactType;
	private String group;
	
	@JsonProperty("required")
	private boolean isRequired;
	
	private List<String> bioAttributes;
	private List<RequiredOnExpr> requiredOn;
	private String subType;

}
