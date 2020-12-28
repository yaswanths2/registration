package io.mosip.registration.service;


import java.util.List;

import io.mosip.registration.dto.Field;
import io.mosip.registration.dto.response.UiSchemaDTO;
import io.mosip.registration.dto.schema.SchemaDTO;
import io.mosip.registration.exception.RegBaseCheckedException;

public interface IdentitySchemaService {
	
	public Double getLatestEffectiveSchemaVersion() throws RegBaseCheckedException;
		
	public SchemaDTO getLatestEffectiveUISchema() throws RegBaseCheckedException;
	
	public String getLatestEffectiveIDSchema() throws RegBaseCheckedException;
	
	public SchemaDTO getUISchema(double idVersion) throws RegBaseCheckedException;
	
	public String getIDSchema(double idVersion) throws RegBaseCheckedException;
	
	public UiSchemaDTO getIdentitySchema(double idVersion) throws RegBaseCheckedException;

	public List<Field> getSchemaFields() throws RegBaseCheckedException;

	public List<Field> getSchemaFields(SchemaDTO schema) throws RegBaseCheckedException;

}
