package io.mosip.registration.dto.schema;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Screen {

	private String name;
	private HashMap<String, String> title;
	private int order;

	@JsonProperty("css-class")
	private String cssClass;

	private boolean visible;
	private boolean enabled;
	private String layout;
	private List<Group> groups;

}
