package fr.cerbere.component.cerbere_devices_bridge.adapter.in.web;

import fr.cerbere.component.cerbere_devices_bridge.domain.model.DeviceType;
import fr.cerbere.component.cerbere_devices_bridge.domain.model.DiscoveredDevice;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.ListDiscoveredDevicesUseCase;
import fr.cerbere.component.cerbere_devices_bridge.domain.port.in.PairDiscoveredDeviceUseCase;
import fr.cerbere.shared.config.CommonJacksonConfig;
import fr.cerbere.shared.config.PermitAllSecurityConfig;
import fr.cerbere.shared.dto.devicebridge.PairDiscoveredDeviceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de la tranche web du contrôleur d'appairage, use-cases mockés (aucune
 * dépendance Mongo/MQTT) — même pattern que {@code SimulatedDeviceControllerTest}.
 */
@WebMvcTest(DiscoveredDeviceController.class)
@Import({PermitAllSecurityConfig.class, CommonJacksonConfig.class})
class DiscoveredDeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	@Qualifier("objectMapper")
	private ObjectMapper objectMapper;

	@MockitoBean
	private ListDiscoveredDevicesUseCase listDiscoveredDevicesUseCase;

	@MockitoBean
	private PairDiscoveredDeviceUseCase pairDiscoveredDeviceUseCase;

	@Test
	void listAllShouldReturnDiscoveredDevices() throws Exception {
		final DiscoveredDevice device = DiscoveredDevice.discover("0x00158d0001", DeviceType.CONTACT, Instant.now());
		given(this.listDiscoveredDevicesUseCase.listAll()).willReturn(List.of(device));

		this.mockMvc.perform(get("/api/devices-bridge/discovered-devices"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].friendlyName").value("0x00158d0001"))
			.andExpect(jsonPath("$[0].inferredType").value("CONTACT"));
	}

	@Test
	void pairShouldDelegateToTheUseCase() throws Exception {
		final UUID coreDeviceId = UUID.randomUUID();
		final PairDiscoveredDeviceRequest request = new PairDiscoveredDeviceRequest("0x00158d0001", coreDeviceId.toString());

		this.mockMvc.perform(post("/api/devices-bridge/discovered-devices/pair")
				.contentType("application/json")
				.content(this.objectMapper.writeValueAsBytes(request)))
			.andExpect(status().isAccepted());

		then(this.pairDiscoveredDeviceUseCase).should().pair("0x00158d0001", coreDeviceId);
	}
}
