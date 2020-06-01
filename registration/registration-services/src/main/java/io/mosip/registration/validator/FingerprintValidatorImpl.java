package io.mosip.registration.validator;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_FINGERPRINT_FACADE;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import io.mosip.kernel.bioapi.impl.BioApiImpl;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BDBInfo.BDBInfoBuilder;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIR.BIRBuilder;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;
import io.mosip.kernel.biometrics.model.Response;
import io.mosip.kernel.biometrics.spi.IBioApi;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.packetmanager.dto.BiometricsDto;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.LoggerConstants;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.dao.UserDetailDAO;
import io.mosip.registration.dto.AuthTokenDTO;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.dto.biometric.FingerprintDetailsDTO;
import io.mosip.registration.entity.UserBiometric;
import io.mosip.registration.service.security.impl.AuthenticationServiceImpl;

/**
 * This class is for validating Fingerprint Authentication
 * 
 * @author SaravanaKumar G
 *
 */
@Service("fingerprintValidator")
public class FingerprintValidatorImpl extends AuthenticationBaseValidator {

	@Autowired
	private UserDetailDAO userDetailDAO;

	@Autowired
	@Qualifier("finger")
	IBioApi ibioApi;

	/**
	 * Instance of LOGGER
	 */
	private static final Logger LOGGER = AppConfig.getLogger(AuthenticationServiceImpl.class);

	/**
	 * Validate the Fingerprint with the AuthenticationValidatorDTO as input
	 */
	@Override
	public boolean validate(AuthenticationValidatorDTO authenticationValidatorDTO) {
		LOGGER.info(LoggerConstants.FINGER_PRINT_AUTHENTICATION, APPLICATION_NAME, APPLICATION_ID,
				"Validating Scanned Finger");
		if (ibioApi instanceof BioApiImpl) {
			ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG,
					RegistrationConstants.DISABLE);
		}
		if (!authenticationValidatorDTO.isAuthValidationFlag()) {
			if ((String
					.valueOf(ApplicationContext.map().get(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG)))
							.equalsIgnoreCase(RegistrationConstants.DISABLE))
				return false;
		}

		if (RegistrationConstants.SINGLE.equals(authenticationValidatorDTO.getAuthValidationType())) {
			return validateOneToManyFP(authenticationValidatorDTO.getUserId(),
					authenticationValidatorDTO.getFingerPrintDetails().get(0));
		} else if (RegistrationConstants.MULTIPLE.equals(authenticationValidatorDTO.getAuthValidationType())) {
			return validateManyToManyFP(authenticationValidatorDTO.getUserId(),
					authenticationValidatorDTO.getFingerPrintDetails());
		}
		return false;
	}

	/**
	 * Validate one finger print values with all the fingerprints from the table.
	 * 
	 * @param userId
	 * @param capturedFingerPrintDto
	 * @return
	 */
	private boolean validateOneToManyFP(String userId, FingerprintDetailsDTO capturedFingerPrintDto) {
		List<UserBiometric> userFingerprintDetails = userDetailDAO.getUserSpecificBioDetails(userId,
				RegistrationConstants.FIN);
		List<FingerprintDetailsDTO> fingerList = new ArrayList<FingerprintDetailsDTO>();
		fingerList.add(capturedFingerPrintDto);
		return validateFpWithBioApi(fingerList, userFingerprintDetails);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.bio.BioService#validateFpWithBioApi(io.mosip.
	 * registration.dto.biometric.FingerprintDetailsDTO, java.util.List)
	 */
	private boolean validateFpWithBioApi(List<FingerprintDetailsDTO> capturedFingerPrintDto,
			List<UserBiometric> userFingerprintDetails) {
		boolean flag = false;

		for (FingerprintDetailsDTO biometricDTO : capturedFingerPrintDto) {
			BIR capturedBir = new BIRBuilder().withBdb(biometricDTO.getFingerPrintISOImage())
					.withBdbInfo(
							new BDBInfo.BDBInfoBuilder().withType(Collections.singletonList(SingleType.FINGER)).build())
					.build();

			BIR[] registeredBir = new BIR[userFingerprintDetails.size()];
			ApplicationContext.map().remove("IDENTY_SDK");
			int i = 0;
			for (UserBiometric userBiometric : userFingerprintDetails) {
				registeredBir[i] = new BIRBuilder().withBdb(userBiometric.getBioIsoImage()).withBdbInfo(
						new BDBInfo.BDBInfoBuilder().withType(Collections.singletonList(SingleType.FINGER)).build())
						.build();
				i++;
			}
			try {
				Response<MatchDecision[]> scores = ibioApi.match(capturedBir, registeredBir, null);
				MatchDecision[] match = null;
				List<MatchDecision> bioMatchList = null;

				if (scores.getStatusCode() == 200) {
					match = scores.getResponse();
					bioMatchList = new ArrayList<>(Arrays.asList(match));
					flag = !bioMatchList.isEmpty() ? bioMatchList.stream().anyMatch(MatchDecision::isMatch) : false;
					LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							"Bio validator completed...");
				} else {
					LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							"Bio validator with error code other than 200");
					LOGGER.error(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							String.valueOf(scores.getStatusCode() + "========>" + scores.getStatusMessage()));
					return false;
				}

			} catch (Exception exception) {
				LOGGER.error(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
						String.format("Exception while validating the iris with bio api: %s caused by %s",
								exception.getMessage(), exception.getCause()));
				ApplicationContext.map().put("IDENTY_SDK", "FAILED");
				return false;

			}

		}
		return flag;
	}

	/**
	 * Validate all the user input finger details with all the finger details form
	 * the DB.
	 * 
	 * @param capturedFingerPrintDetails
	 * @return
	 */
	private boolean validateManyToManyFP(String userId, List<FingerprintDetailsDTO> capturedFingerPrintDetails) {
		Boolean isMatchFound = false;
			isMatchFound = validateFpWithBioApi(capturedFingerPrintDetails,
					userDetailDAO.getUserSpecificBioDetails(userId, RegistrationConstants.FIN));
				SessionContext.map().put(RegistrationConstants.DUPLICATE_FINGER, "Duplicate found");
		return isMatchFound;

	}

	@Override
	public AuthTokenDTO validate(String userId, String otp, boolean haveToSaveAuthToken) {
		return null;
	}

	@Override
	public boolean bioMerticsValidator(List<BiometricsDto> listOfBiometrics) {

		LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID, "Entering into bio validator");
		List<BIR> bioRecordSampleList=new ArrayList<>();
		List<BIR> bioRecordGalleryList=new ArrayList<>();
		BiometricRecord bioRecordSample=new BiometricRecord();
		List<UserBiometric> userDetailsRecorded = userDetailDAO
				.getUserSpecificBioDetails(SessionContext.userContext().getUserId(), RegistrationConstants.FIN);
		boolean flag = false;
		for (BiometricsDto biometricDTO : listOfBiometrics) {
			
			BIR birBuildSample = new BIRBuilder().withBdb(biometricDTO.getAttributeISO()).withBdbInfo(
					new BDBInfo.BDBInfoBuilder().withType(Collections.singletonList(BiometricType.FINGER)).build())
					.build();
			bioRecordSampleList.add(birBuildSample);
		}
			
		bioRecordSample.setSegments(bioRecordSampleList);
			
		BiometricRecord[] bioRecordGallery=new BiometricRecord[userDetailsRecorded.size()];
		
			BIR[] birBuildGallery = new BIR[userDetailsRecorded.size()];
			ApplicationContext.map().remove("IDENTY_SDK");
			int i = 0;
			for (UserBiometric userBiometric : userDetailsRecorded) {
				birBuildGallery[i] = new BIRBuilder().withBdb(userBiometric.getBioIsoImage()).withBdbInfo(
						new BDBInfo.BDBInfoBuilder().withType(Collections.singletonList(BiometricType.FINGER)).build())
						.build();
				
				bioRecordGalleryList.add(birBuildGallery[i]);
				bioRecordGallery[i].setSegments(bioRecordGalleryList);
				
				i++;
				
				
			}
			
			
			try {
				Response<MatchDecision[] > scores = ibioApi.match(bioRecordSample, bioRecordGallery,Collections.singletonList(BiometricType.FINGER), null);
				MatchDecision[] match = null;
				List<MatchDecision> bioMatchList = null;

				if (scores.getStatusCode() == 200) {
					match = scores.getResponse();
					bioMatchList = new ArrayList<>(Arrays.asList(match));
					bioMatchList.get(0).getDecisions();
					/*flag = !bioMatchList.isEmpty() ? bioMatchList.stream().anyMatch(MatchDecision::isMatch) : false;
					LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							"Bio validator completed...");*/
				} else {
					LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							"Bio validator with error code other than 200");
					LOGGER.error(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
							String.valueOf(scores.getStatusCode() + "========>" + scores.getStatusMessage()));
					return true;
				}

			} catch (Exception exception) {
				LOGGER.error(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
						String.format("Exception while validating the iris with bio api: %s caused by %s",
								exception.getMessage(), exception.getCause()));
				ApplicationContext.map().put("IDENTY_SDK", "FAILED");
				return true;

			}
		}
		LOGGER.info(LOG_REG_FINGERPRINT_FACADE, APPLICATION_NAME, APPLICATION_ID,
				"leaving into bio validator with out exception");
		return flag;

	}
}
