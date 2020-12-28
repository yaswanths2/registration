package io.mosip.registration.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.dao.IdentitySchemaDao;
import io.mosip.registration.dto.Field;
import io.mosip.registration.dto.response.UiSchemaDTO;
import io.mosip.registration.dto.schema.Group;
import io.mosip.registration.dto.schema.SchemaDTO;
import io.mosip.registration.dto.schema.Screen;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.IdentitySchemaService;

@Service
public class IdentitySchemaServiceImpl implements IdentitySchemaService {
	
	private static final Logger LOGGER = AppConfig.getLogger(IdentitySchemaServiceImpl.class);

	@Autowired
	private IdentitySchemaDao identitySchemaDao;
	
	@Override
	public Double getLatestEffectiveSchemaVersion() throws RegBaseCheckedException {
		return identitySchemaDao.getLatestEffectiveSchemaVersion();
	}

	@Override
	public SchemaDTO getLatestEffectiveUISchema() throws RegBaseCheckedException {
		return identitySchemaDao.getLatestEffectiveUISchema();
	}

	@Override
	public String getLatestEffectiveIDSchema() throws RegBaseCheckedException {
		return identitySchemaDao.getLatestEffectiveIDSchema();
	}

	@Override
	public SchemaDTO getUISchema(double idVersion) throws RegBaseCheckedException {
		return identitySchemaDao.getUISchema(idVersion);
	}

	@Override
	public String getIDSchema(double idVersion) throws RegBaseCheckedException {
		return identitySchemaDao.getIDSchema(idVersion);
	}

	@Override
	public UiSchemaDTO getIdentitySchema(double idVersion) throws RegBaseCheckedException {
		return identitySchemaDao.getIdentitySchema(idVersion);
	}
	
	@Override
	public List<Field> getSchemaFields() throws RegBaseCheckedException {
		return getSchemaFields(getLatestEffectiveUISchema());
	}
	
	@Override
	public List<Field> getSchemaFields(SchemaDTO schema) throws RegBaseCheckedException {
		List<Field> fields = new ArrayList<>();
		if(schema != null) {
			List<Screen> screens = schema.getScreens();
			if (screens != null && !screens.isEmpty()) {

				for (Screen screen : screens) {				
					if (screen.isVisible() && screen.getGroups() != null && !screen.getGroups().isEmpty()) {
						for (Group group : screen.getGroups()) {
							if(group.getFields() != null && !group.getFields().isEmpty()) {
								fields.addAll(group.getFields());
							}
						}
					}
				}
			}
		}
		return fields;
	}

}
