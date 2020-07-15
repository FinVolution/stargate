package com.ppdai.stargate.k8s;

import com.ppdai.atlas.client.model.ZoneDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class ControllerFactoryServiceTest {

    @Autowired
    private ControllerFactoryService controllerFactoryService;

    @Test
    public void testCreatePodHaController() throws InterruptedException {
        ZoneDto zoneDto = new ZoneDto();
        zoneDto.setName("万国");
        zoneDto.setEnvName("dev");
        zoneDto.setK8s("http://127.0.0.1:8080");
        PodHaController podHaController = controllerFactoryService.createPodHaController(zoneDto);
        assertNotNull(podHaController);

        podHaController.startEventTrigger();

        Thread.sleep(Integer.MAX_VALUE);
    }
}
