package com.ppdai.stargate.k8s;

import com.ppdai.atlas.client.model.ZoneDto;
import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class ControllerFactoryService {

    @Autowired
    private AppService appService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private InstanceJobService instanceJobService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private JsonHttpClient hcHttpClient;

    @Value("${stargate.job.token:}")
    private String token;

    @Value("${stargate.podHa.eventCacheSecs}")
    private long eventCacheSecs;

    @Value("${stargate.podHa.eventPollingSecs}")
    private long podHaEventPollingSecs;

    @Autowired
    private Environment environment;

    public PodHaController createPodHaController(ZoneDto zoneDto) {
        return new PodHaController(token, zoneDto, hcHttpClient, eventCacheSecs, podHaEventPollingSecs,
                appService, instanceService, instanceJobService, containerService, taskManager, environment);

    }
}
