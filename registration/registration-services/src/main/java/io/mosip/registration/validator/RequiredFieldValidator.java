package io.mosip.registration.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dto.Field;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RequiredOnExpr;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.IdentitySchemaService;

@Component
public class RequiredFieldValidator {

	private static final String APPLICANT_SUBTYPE = "applicant";

	@Autowired
	private IdentitySchemaService identitySchemaService;

	public boolean isRequiredField(String fieldId, RegistrationDTO registrationDTO) throws RegBaseCheckedException {
		Optional<Field> schemaField = identitySchemaService.getSchemaFields().stream()
				.filter(field -> field.getId().equals(fieldId)).findFirst();
		if (!schemaField.isPresent())
			return false;

		return isRequiredField(schemaField.get(), registrationDTO);
	}

	public boolean isRequiredField(Field schemaField, RegistrationDTO registrationDTO) throws RegBaseCheckedException {
		boolean required = false;
		if (schemaField == null)
			return required;
		required = schemaField.isRequired();
		if (schemaField.getRequiredOn() != null && !schemaField.getRequiredOn().isEmpty()) {
			required = isRequiredField(schemaField.getRequiredOn(), registrationDTO);
		}
		return required;
	}

	@SuppressWarnings("unchecked")
	public boolean isRequiredField(List<RequiredOnExpr> expressions, RegistrationDTO registrationDTO)
			throws RegBaseCheckedException {
		boolean required = false;
		if (expressions == null || expressions.isEmpty())
			return required;

		Optional<RequiredOnExpr> expression = expressions.stream()
				.filter(field -> "MVEL".equalsIgnoreCase(field.getEngine()) && field.getExpr() != null).findFirst();

		if (expression.isPresent()) {
			@SuppressWarnings("rawtypes")
			Map context = new HashMap();
			context.put("identity", registrationDTO.getMVELDataContext());
			VariableResolverFactory resolverFactory = new MapVariableResolverFactory(context);
			required = MVEL.evalToBoolean(expression.get().getExpr(), resolverFactory);
		}
		return required;
	}

	public List<String> isRequiredBiometricField(String subType, RegistrationDTO registrationDTO)
			throws RegBaseCheckedException {
		List<Field> fields = identitySchemaService.getSchemaFields().stream()
				.filter(field -> field.getType() != null
						&& PacketManagerConstants.BIOMETRICS_DATATYPE.equals(field.getType())
						&& field.getSubType() != null && field.getSubType().equals(subType))
				.collect(Collectors.toList());

		return getRequiredAttributes(subType, fields, registrationDTO);
	}

	public List<String> getRequiredAttributes(String subType, List<Field> fields, RegistrationDTO registrationDTO)
			throws RegBaseCheckedException {
		List<String> requiredAttributes = new ArrayList<String>();

		if (fields != null && !fields.isEmpty()) {
			for (Field schemaField : fields) {
				if (isRequiredField(schemaField, registrationDTO) && schemaField.getBioAttributes() != null)
					requiredAttributes.addAll(schemaField.getBioAttributes());
			}

			if ((registrationDTO.isChild() && APPLICANT_SUBTYPE.equals(subType) && requiredAttributes.contains("face"))
					|| (registrationDTO.getRegistrationCategory()
							.equalsIgnoreCase(RegistrationConstants.PACKET_TYPE_UPDATE)
							&& registrationDTO.getUpdatableFieldGroups().contains("GuardianDetails")
							&& APPLICANT_SUBTYPE.equals(subType) && requiredAttributes.contains("face"))) {
				return Arrays.asList("face"); // Only capture face
			}
		}

		return requiredAttributes;
	}
}
