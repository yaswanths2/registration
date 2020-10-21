package io.mosip.registration.processor.printing.api.controller.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.core.constant.IdType;
import io.mosip.registration.processor.core.spi.print.service.PrintService;
import io.mosip.registration.processor.print.service.exception.RegPrintAppException;
import io.mosip.registration.processor.printing.api.controller.PrintApiController;
import io.mosip.registration.processor.printing.api.dto.PrintRequest;
import io.mosip.registration.processor.printing.api.dto.RequestDTO;
import io.mosip.registration.processor.printing.api.util.PrintServiceRequestValidator;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = PrintServiceConfigTest.class)
@TestPropertySource(locations = "classpath:application.properties")
public class PrintApiControllerTest {

	@InjectMocks
	private PrintApiController printapicontroller = new PrintApiController();

	@MockBean
	private PrintService<Map<String, byte[]>> printservice;

	@Mock
	private Environment env;

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private PrintServiceRequestValidator validator;

	@Autowired
	private RidValidator<String> ridValidator;
	@Mock
	private Errors errors;

	private Map<String, byte[]> map = new HashMap<>();

	private String json;

	@Before
	public void setup() throws RegPrintAppException {
		when(env.getProperty("mosip.registration.processor.print.service.id")).thenReturn("mosip.registration.print");
		when(env.getProperty("mosip.registration.processor.datetime.pattern"))
				.thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		when(env.getProperty("mosip.registration.processor.application.version")).thenReturn("1.0");

		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		RequestDTO dto = new RequestDTO();
		dto.setIdtype(IdType.RID);
		dto.setIdValue("10003100030000720190416061449");
		dto.setCardType("UIN");
		request.setRequest(dto);
		String time = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		request.setRequesttime(time);
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);

		byte[] pdfbyte = "pdf bytes".getBytes();
		map.put("uinPdf", pdfbyte);
		Mockito.when(ridValidator.validateId(any())).thenReturn(Boolean.TRUE);
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testpdfSuccess() throws Exception {
		Mockito.when(
				printservice.getDocuments(any(IdType.class), any(String.class), any(String.class), any(Boolean.class)))
				.thenReturn(map);

		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testPdfFailure() throws Exception {
		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isBadRequest());
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testPdfIdTypeMissing() throws Exception {
		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		RequestDTO dto = new RequestDTO();
		dto.setIdValue("10003100030000720190416061449");
		request.setRequest(dto);
		String time = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		request.setRequesttime(time);
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);
		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testPdfIdValueMissing() throws Exception {
		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		RequestDTO dto = new RequestDTO();
		dto.setIdtype(IdType.RID);
		request.setRequest(dto);
		String time = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		request.setRequesttime(time);
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);
		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testPdfSuccessUIN() throws Exception {
		Mockito.when(
				printservice.getDocuments(any(IdType.class), any(String.class), any(String.class), any(Boolean.class)))
				.thenReturn(map);
		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		RequestDTO dto = new RequestDTO();
		dto.setIdtype(IdType.UIN);
		dto.setIdValue("2812936908");
		request.setRequest(dto);
		String time = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		request.setRequesttime(time);
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);
		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
				.andExpect(status().isOk());
	}

	@WithUserDetails("reg-admin")
	@Test
	public void testPdfRequestMissing() throws Exception {
		PrintRequest request = new PrintRequest();
		request.setId("mosip.registration.print");
		String time = DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		request.setRequesttime(time);
		request.setVersion("1.0");
		Gson gson = new GsonBuilder().serializeNulls().create();
		json = gson.toJson(request);
		this.mockMvc.perform(post("/uincard").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
				.andExpect(status().isOk());
	}

}
