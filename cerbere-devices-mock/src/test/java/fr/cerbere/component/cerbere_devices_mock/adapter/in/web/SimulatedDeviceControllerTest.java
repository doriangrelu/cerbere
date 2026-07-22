package fr.cerbere.component.cerbere_devices_mock.adapter.in.web;

import fr.cerbere.component.cerbere_devices_mock.adapter.in.web.dto.RegisterSimulatedDeviceRequest;
import fr.cerbere.component.cerbere_devices_mock.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_mock.domain.model.SimulatedDevice;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.ListSimulatedDevicesUseCase;
import fr.cerbere.component.cerbere_devices_mock.domain.port.in.RegisterSimulatedDeviceUseCase;
import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de la tranche web du contrôleur de gestion des devices simulés,
 * use-cases mockés (aucune dépendance Mongo/Kafka).
 */
@WebMvcTest(SimulatedDeviceController.class)
@Import({PermitAllSecurityConfig.class, CommonJacksonConfig.class})
class SimulatedDeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper objectMapper;

	@MockitoBean
	private RegisterSimulatedDeviceUseCase registerSimulatedDeviceUseCase;

	@MockitoBean
	private ListSimulatedDevicesUseCase listSimulatedDevicesUseCase;

	@Test
	void registerShouldReturnCreatedDevice() throws Exception {
		final SimulatedDevice device = SimulatedDevice.register(DeviceType.CONTACT, "Porte d'entrée", null, false);
		given(this.registerSimulatedDeviceUseCase.register(any(), any(), any(), anyBoolean())).willReturn(device);

		final RegisterSimulatedDeviceRequest request = new RegisterSimulatedDeviceRequest("CONTACT", "Porte d'entrée", null, false);

		this.mockMvc.perform(post("/api/devices-mock")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.type").value("CONTACT"))
			.andExpect(jsonPath("$.currentState").value("CLOSED"));
	}

	@Test
	void listAllShouldReturnRegisteredDevices() throws Exception {
		final SimulatedDevice device = SimulatedDevice.register(DeviceType.MOTION, "Salon", null, true);
		given(this.listSimulatedDevicesUseCase.listAll()).willReturn(List.of(device));

		this.mockMvc.perform(get("/api/devices-mock"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].type").value("MOTION"));
	}
}
