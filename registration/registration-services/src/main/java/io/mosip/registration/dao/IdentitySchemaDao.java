package io.mosip.registration.dao;

import java.io.IOException;

import io.mosip.registration.dto.response.UiSchemaDTO;
import io.mosip.registration.dto.schema.SchemaDTO;
import io.mosip.registration.entity.IdentitySchema;
import io.mosip.registration.exception.RegBaseCheckedException;

public interface IdentitySchemaDao {
	
	public Double getLatestEffectiveSchemaVersion() throws RegBaseCheckedException;
	
	public IdentitySchema getLatestEffectiveIdentitySchema();
	
	public SchemaDTO getLatestEffectiveUISchema() throws RegBaseCheckedException;
	
	public String getLatestEffectiveIDSchema() throws RegBaseCheckedException;
	
	public SchemaDTO getUISchema(double idVersion) throws RegBaseCheckedException;
	
	public String getIDSchema(double idVersion) throws RegBaseCheckedException;
	
	public void createIdentitySchema(UiSchemaDTO uiSchemaDTO) throws IOException;
	
	public UiSchemaDTO getIdentitySchema(double idVersion) throws RegBaseCheckedException;

}
