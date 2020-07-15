package com.ppdai.stargate.controller;

import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.service.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/zones")
public class ZoneController {
    @Autowired
    private ZoneService zoneService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<String>> fetchEnvZones(@RequestParam String env) {
        List<String> zones = zoneService.fetchZoneNamesByEnv(env);
        return Response.success(zones);
    }
}
