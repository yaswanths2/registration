package io.mosip.registration.test.authentication;

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.context.SessionContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApplicationContext.class,SessionContext.class})
public class IrisValidatorTest {

	/*@InjectMocks
	private IrisValidatorImpl irsiValidatorImpl;
	
	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock
	private BioService bioService;
	
	@Mock
	private UserDetailDAO userDetailDAO;
	
	@Mock
	private IBioApi ibioApi;
	
	private AuthenticationValidatorDTO authenticationValidatorDTO = new AuthenticationValidatorDTO();

	@Before
	public void initialize() {
		authenticationValidatorDTO.setUserId("mosip");
		
		List<IrisDetailsDTO> irisDetailsDTOs = new ArrayList<>();
		IrisDetailsDTO irisDetailsDTO = new IrisDetailsDTO();


		byte[] bytes = new byte[100];
		Arrays.fill(bytes, (byte) 1);

		irisDetailsDTO.setIris(bytes);
		irisDetailsDTO.setIrisType("leftIris");
		irisDetailsDTO.setForceCaptured(false);
		irisDetailsDTO.setQualityScore(90.1);
		irisDetailsDTOs.add(irisDetailsDTO);
		authenticationValidatorDTO.setIrisDetails(irisDetailsDTOs);;
	}
	
	@Test
	public void validateTest() throws BiometricException {
		
		UserBiometric userBiometric=new UserBiometric();
		userBiometric.setQualityScore(91);
		List<UserBiometric> user=new ArrayList<UserBiometric>();
		user.add(userBiometric);
		Map<String, Object> applicationMap = new HashMap<>();	
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		authenticationValidatorDTO.setAuthValidationType(RegistrationConstants.SINGLE);
		ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG, RegistrationConstants.DISABLE);
		Mockito.when(userDetailDAO.getUserSpecificBioDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(user);
		Score[] score = new Score[1];
		Score score2 = new Score();
		score2.setScaleScore(30);
		score[0] = score2;
		Mockito.when(ibioApi.match(Mockito.any(), Mockito.any(), (KeyValuePair[]) Mockito.isNull())).thenReturn(score);
		assertThat(irsiValidatorImpl.validate(authenticationValidatorDTO), is(false));
	}
	
	@Test
	public void validateTestGoodQuality() throws BiometricException {
		
		UserBiometric userBiometric=new UserBiometric();
		userBiometric.setQualityScore(91);
		List<UserBiometric> user=new ArrayList<UserBiometric>();
		user.add(userBiometric);
		Map<String, Object> applicationMap = new HashMap<>();	
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		authenticationValidatorDTO.setAuthValidationType(RegistrationConstants.SINGLE);
		ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG, RegistrationConstants.DISABLE);
		Mockito.when(userDetailDAO.getUserSpecificBioDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(user);
		Score[] score = new Score[1];
		Score score2 = new Score();
		score2.setScaleScore(80);
		score[0] = score2;
		Mockito.when(ibioApi.match(Mockito.any(), Mockito.any(), (KeyValuePair[]) Mockito.isNull())).thenReturn(score);
		assertThat(irsiValidatorImpl.validate(authenticationValidatorDTO), is(true));
	}
	
	@Test
	public void validateTestGoodQualityException() throws BiometricException {
		
		UserBiometric userBiometric=new UserBiometric();
		userBiometric.setQualityScore(91);
		List<UserBiometric> user=new ArrayList<UserBiometric>();
		user.add(userBiometric);
		Map<String, Object> applicationMap = new HashMap<>();	
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		authenticationValidatorDTO.setAuthValidationType(RegistrationConstants.SINGLE);
		ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG, RegistrationConstants.DISABLE);
		Mockito.when(userDetailDAO.getUserSpecificBioDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(user);
		Score[] score = new Score[1];
		Score score2 = new Score();
		score2.setScaleScore(80);
		score[0] = score2;
		Mockito.when(ibioApi.match(Mockito.any(), Mockito.any(), (KeyValuePair[]) Mockito.isNull())).thenThrow(BiometricException.class);
		assertThat(irsiValidatorImpl.validate(authenticationValidatorDTO), is(false));
	}
	
	@Test
	public void validateManyTestGoodQuality() throws BiometricException {
		
		UserBiometric userBiometric=new UserBiometric();
		userBiometric.setQualityScore(91);
		List<UserBiometric> user=new ArrayList<UserBiometric>();
		user.add(userBiometric);
		PowerMockito.mockStatic(SessionContext.class);
		//SessionContext.map().put(RegistrationConstants.DUPLICATE_IRIS, fingerprintDetailsDTO);
		Map<String, Object> applicationMap = new HashMap<>();	
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		authenticationValidatorDTO.setAuthValidationType(RegistrationConstants.MULTIPLE);
		ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG, RegistrationConstants.DISABLE);
		Mockito.when(userDetailDAO.getUserSpecificBioDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(user);
		Score[] score = new Score[1];
		Score score2 = new Score();
		score2.setScaleScore(80);
		score[0] = score2;
		Mockito.when(ibioApi.match(Mockito.any(), Mockito.any(), (KeyValuePair[]) Mockito.isNull())).thenReturn(score);
		assertThat(irsiValidatorImpl.validate(authenticationValidatorDTO), is(true));
	}
	
	@Test
	public void validateUserTest() {
		authenticationValidatorDTO.setUserId("");
		Map<String, Object> applicationMap = new HashMap<>();	
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		ApplicationContext.map().put(RegistrationConstants.DEDUPLICATION_FINGERPRINT_ENABLE_FLAG, RegistrationConstants.DISABLE);
		irsiValidatorImpl.validate(authenticationValidatorDTO);
	}
	
	@Test
	public void validateAuthTest() {
		assertNull(irsiValidatorImpl.validate("mosip","123", true));
	}*/
}
