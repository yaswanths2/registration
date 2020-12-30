package io.mosip.registration.controller.reg;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idgenerator.spi.RidGenerator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.ProcessNames;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.auth.AuthenticationController;
import io.mosip.registration.controller.device.BiometricsController;
import io.mosip.registration.dto.Field;
import io.mosip.registration.dto.OSIDataDTO;
import io.mosip.registration.dto.RegistrationDTO;
import io.mosip.registration.dto.RegistrationMetaDataDTO;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.service.IdentitySchemaService;
import io.mosip.registration.service.sync.MasterSyncService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * {@code RegistrationController} for Registration Page Controller
 * 
 * @author Taleev.Aalam
 * @since 1.0.0
 */

@Controller
public class RegistrationController extends BaseController {

	/**
	 * Instance of {@link Logger}
	 */
	private static final Logger LOGGER = AppConfig.getLogger(RegistrationController.class);

	@Autowired
	private DocumentScanController documentScanController;
	@FXML
	private GridPane documentScan;
	@FXML
	private GridPane registrationId;
	@Autowired
	private Validations validation;
	@Autowired
	private MasterSyncService masterSync;
	@Autowired
	private DemographicDetailController demographicDetailController;
	@FXML
	private GridPane demographicDetail;

	@FXML
	private GridPane biometric;
	@FXML
	private GridPane operatorAuthenticationPane;
	@FXML
	public ImageView biometricTracker;
	@FXML
	private GridPane registrationPreview;
	@Autowired
	private AuthenticationController authenticationController;
	@Autowired
	private RidGenerator<String> ridGeneratorImpl;

	@Autowired
	private IdentitySchemaService identitySchemaService;

	@Autowired
	private BiometricsController biometricsController;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javafx.fxml.Initializable#initialize()
	 */
	@FXML
	private void initialize() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Entering the Registration Controller");
		try {
			if (isEditPage() && getRegistrationDTOFromSession() != null) {
				prepareEditPageContent();
			}
			uinUpdate();

			pageFlow.resetPageFlow();

			showCurrentPage(null, pageFlow.getNextScreenName());

		} catch (RuntimeException runtimeException) {
			LOGGER.error("REGISTRATION - CONTROLLER", APPLICATION_NAME, RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.UNABLE_LOAD_REG_PAGE);
		}
	}

	/**
	 * This method is prepare the screen for uin update
	 */
	private void uinUpdate() {
		if (getRegistrationDTOFromSession().getUpdatableFields() != null) {
			demographicDetailController.uinUpdate();
		}
	}

	public void init(String UIN, HashMap<String, Object> selectionListDTO, Map<String, Field> selectedFields,
			List<String> selectedFieldGroups) {
		validation.updateAsLostUIN(false);
		createRegistrationDTOObject(RegistrationConstants.PACKET_TYPE_UPDATE);
		RegistrationDTO registrationDTO = getRegistrationDTOFromSession();
		registrationDTO.setSelectionListDTO(selectionListDTO);
		List<String> fieldIds = new ArrayList<String>(selectedFields.keySet());
		registrationDTO.setUpdatableFields(fieldIds);
		registrationDTO.addDemographicField("UIN", UIN);
		registrationDTO.setUpdatableFieldGroups(selectedFieldGroups);
		registrationDTO.setBiometricMarkedForUpdate(
				selectedFieldGroups.contains(RegistrationConstants.BIOMETRICS_GROUP) ? true : false);
	}

	protected void initializeLostUIN() {
		validation.updateAsLostUIN(true);
		createRegistrationDTOObject(RegistrationConstants.PACKET_TYPE_LOST);
	}

	/**
	 * This method is to prepopulate all the values for edit operation
	 */
	private void prepareEditPageContent() {
		try {
			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Preparing the Edit page content");
			// demographicDetailController.prepareEditPageContent();
			documentScanController.prepareEditPageContent();
			SessionContext.map().put(RegistrationConstants.REGISTRATION_ISEDIT, false);
		} catch (RuntimeException runtimeException) {
			LOGGER.error(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					runtimeException.getMessage() + ExceptionUtils.getStackTrace(runtimeException));
		}

	}

	/**
	 * This method is to go to the operator authentication page
	 */
	public void goToAuthenticationPage() {
		try {
			authenticationController.initData(ProcessNames.PACKET.getType());
		} catch (RegBaseCheckedException ioException) {
			LOGGER.error("REGISTRATION - REGSITRATION_OPERATOR_AUTHENTICATION_PAGE_LOADING_FAILED", APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID,
					ioException.getMessage() + ExceptionUtils.getStackTrace(ioException));
		}
	}

	/**
	 * This method is to determine if it is edit page
	 */
	private Boolean isEditPage() {
		if (SessionContext.map().get(RegistrationConstants.REGISTRATION_ISEDIT) != null)
			return (Boolean) SessionContext.map().get(RegistrationConstants.REGISTRATION_ISEDIT);
		return false;
	}

	/**
	 * This method will create registration DTO object
	 */
	protected void createRegistrationDTOObject(String registrationCategory) {
		RegistrationDTO registrationDTO = new RegistrationDTO();

		// set id-schema version to be followed for this registration
		try {
			registrationDTO.setIdSchemaVersion(identitySchemaService.getLatestEffectiveSchemaVersion());
		} catch (RegBaseCheckedException e) {
			generateAlert(RegistrationConstants.ERROR, "Published Identity Schema is required"); // TODO
		}

		// Create object for OSIData DTO
		registrationDTO.setOsiDataDTO(new OSIDataDTO());
		registrationDTO.setRegistrationCategory(registrationCategory);

		// Create RegistrationMetaData DTO & set default values in it
		RegistrationMetaDataDTO registrationMetaDataDTO = new RegistrationMetaDataDTO();
		registrationMetaDataDTO.setRegistrationCategory(registrationCategory); // TODO - remove its usage

		registrationDTO.setRegistrationMetaDataDTO(registrationMetaDataDTO);

		// Set RID
		String registrationID = ridGeneratorImpl.generateId(
				(String) ApplicationContext.map().get(RegistrationConstants.USER_CENTER_ID),
				(String) ApplicationContext.map().get(RegistrationConstants.USER_STATION_ID));
		registrationDTO.setRegistrationId(registrationID);

		LOGGER.info(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID,
				"Registration Started for RID  : [ " + registrationDTO.getRegistrationId() + " ] ");

		List<String> defaultFields = new ArrayList<String>();
		List<String> defaultFieldGroups = new ArrayList<String>();

		defaultFieldGroups.add(RegistrationConstants.UI_SCHEMA_GROUP_FULL_NAME);
		for (Field field : fetchByGroup(RegistrationConstants.UI_SCHEMA_GROUP_FULL_NAME)) {
			defaultFields.add(field.getId());
		}
		// Used to update printing name as default
		registrationDTO.setDefaultUpdatableFieldGroups(defaultFieldGroups);
		registrationDTO.setDefaultUpdatableFields(defaultFields);

		// Put the RegistrationDTO object to SessionContext Map
		SessionContext.map().put(RegistrationConstants.REGISTRATION_DATA, registrationDTO);
	}

	/**
	 * This method will show uin update current page
	 */
	public void showUINUpdateCurrentPage() {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Setting Visibility for demo,doc,biometric,preview,auth");
		demographicDetail.setVisible(getVisiblity(RegistrationConstants.UIN_UPDATE_DEMOGRAPHICDETAIL));
		documentScan.setVisible(getVisiblity(RegistrationConstants.UIN_UPDATE_DOCUMENTSCAN));

		biometric.setVisible(getVisiblity(RegistrationConstants.UIN_UPDATE_PARENTGUARDIAN_DETAILS));
		registrationPreview.setVisible(getVisiblity(RegistrationConstants.UIN_UPDATE_REGISTRATIONPREVIEW));
		operatorAuthenticationPane
				.setVisible(getVisiblity(RegistrationConstants.UIN_UPDATE_OPERATORAUTHENTICATIONPANE));
	}

	/**
	 * This method will determine the visibility of the page
	 */
	private boolean getVisiblity(String page) {
		if (SessionContext.map().get(page) != null) {
			return (boolean) SessionContext.map().get(page);
		}
		return false;
	}

	/**
	 * This method will determine the current page
	 */
	public void showCurrentPage(String notTosShow, String show) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Navigating to next page based on the current page");

		getCurrentPage(registrationId, notTosShow, show);

		refresh(show);
		Node previousNode = getNode(notTosShow, pageFlow.getPreviousScreenNumber());

		if (previousNode != null) {
			previousNode.setVisible(false);
			previousNode.setManaged(false);
		}
		Node currentNode = getNode(show, pageFlow.getCurrentScreenNumber());
		if (currentNode != null) {
			currentNode.setVisible(true);
			currentNode.setManaged(true);
		} else if (!RegistrationConstants.REGISTRATION_PREVIEW.equals(show)) {

			String prevScreen = pageFlow.getCurrentScreenName();

			pageFlow.updateNext();

			if (pageFlow.getNextScreenName() != null) {

				showCurrentPage(prevScreen, pageFlow.getNextScreenName());
			} else {
				pageFlow.updatePrevious();
			}

		}
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Navigated to next page based on the current page");
	}

	public void showPreviousPage(String notTosShow, String show) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Navigating to previous page based on the current page");

		getCurrentPage(registrationId, notTosShow, show);

		refresh(show);
		Node currentNode = getNode(notTosShow, pageFlow.getCurrentScreenNumber());
		if (currentNode != null) {
			currentNode.setVisible(false);
			currentNode.setManaged(false);
		}
		Node previousNode = getNode(show, pageFlow.getPreviousScreenNumber());
		if (previousNode != null) {
			previousNode.setVisible(true);
			previousNode.setManaged(true);
		} else {
			String currentScreen = pageFlow.getCurrentScreenName();

			pageFlow.updatePrevious();

			if (pageFlow.getPreviousScreenName() != null) {

				showPreviousPage(currentScreen, pageFlow.getPreviousScreenName());
			} else {
				pageFlow.updateNext();
			}
		}

		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Navigated to previous page based on the current page");
	}

	/**
	 * 
	 * Validates the fields of demographic pane1
	 * 
	 */
	public boolean validateDemographicPane(Pane paneToValidate) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validating the fields in demographic pane");

		boolean gotoNext = true;
		List<String> excludedIds = RegistrationConstants.fieldsToExclude();
		/*
		 * if (getRegistrationDTOFromSession().getSelectionListDTO() != null) {
		 * excludedIds.remove("cniOrPinNumber");
		 * excludedIds.remove("cniOrPinNumberLocalLanguage"); }
		 */

		if (getRegistrationDTOFromSession().getUpdatableFields() != null
				&& !getRegistrationDTOFromSession().getUpdatableFields().isEmpty()) {
			if (getRegistrationDTOFromSession().isChild()
					&& !getRegistrationDTOFromSession().getUpdatableFieldGroups().contains("GuardianDetails")) {
				gotoNext = false;
				generateAlert(RegistrationConstants.ERROR, "Parent or Guardian should have been selected");
			}
		}

		validation.setValidationMessage();
		gotoNext = validation.validate(paneToValidate, excludedIds, gotoNext, masterSync);
		displayValidationMessage(validation.getValidationMessage().toString());
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Validated the fields");

		return gotoNext;
	}

	/**
	 * Display the validation failure messages
	 */
	public void displayValidationMessage(String validationMessage) {
		LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
				RegistrationConstants.APPLICATION_ID, "Showing the validation message");
		if (validationMessage.length() > 0) {
			TextArea view = new TextArea(validationMessage);
			view.setEditable(false);
			Scene scene = new Scene(new StackPane(view), 300, 200);
			Stage primaryStage = new Stage();
			primaryStage.setTitle("Invalid input");
			primaryStage.setScene(scene);
			primaryStage.sizeToScene();
			primaryStage.initModality(Modality.WINDOW_MODAL);
			primaryStage.initOwner(fXComponents.getStage());
			primaryStage.show();

			LOGGER.debug(RegistrationConstants.REGISTRATION_CONTROLLER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, "Validation message shown successfully");
		}
	}

	private void refresh(String screenName) {

		switch (screenName) {
		case RegistrationConstants.GUARDIAN_BIOMETRIC:
			biometricsController.populateBiometricPage(false, false);
			biometricsController.setSubType(pageFlow.getCurrentScreenNumber());
			biometricsController.setScreenNumber(pageFlow.getCurrentScreenNumber());
			biometricsController.enableUI();

			break;
		case RegistrationConstants.DOCUMENT_SCAN:
			demographicDetailController.prepareEditPageContent();
		case RegistrationConstants.REGISTRATION_PREVIEW:
			registrationPreviewController.setUpPreviewContent();
		}
	}
}
