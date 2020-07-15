package com.ppdai.stargate.k8s;

import com.ppdai.stargate.service.JobService;
import com.ppdai.stargate.vo.ZoneInstanceCountVO;
import io.kubernetes.client.ApiException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeneralTest {

    @Autowired
    private JobService jobService;

    @Test
    public void testNodeConditionDateformat() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date = dateFormat.parse("2019-03-11T06:46:21+0000");
        System.out.println(date);
        System.out.println(new Date());
    }

    @Test
    public void sortZone() {
        List<ZoneInstanceCountVO> zoneInstanceCountVOList = new ArrayList<>();

        ZoneInstanceCountVO zoneInstanceCountVO1 = new ZoneInstanceCountVO();
        zoneInstanceCountVO1.setZone("万国一");
        zoneInstanceCountVO1.setUsedCount(1);
        zoneInstanceCountVO1.setFreeCount(5);

        ZoneInstanceCountVO zoneInstanceCountVO2 = new ZoneInstanceCountVO();
        zoneInstanceCountVO2.setZone("万国二");
        zoneInstanceCountVO2.setUsedCount(0);
        zoneInstanceCountVO2.setFreeCount(10);

        ZoneInstanceCountVO zoneInstanceCountVO3 = new ZoneInstanceCountVO();
        zoneInstanceCountVO3.setZone("万国三");
        zoneInstanceCountVO3.setUsedCount(0);
        zoneInstanceCountVO3.setFreeCount(15);

        ZoneInstanceCountVO zoneInstanceCountVO4 = new ZoneInstanceCountVO();
        zoneInstanceCountVO4.setZone("万国四");
        zoneInstanceCountVO4.setUsedCount(2);
        zoneInstanceCountVO4.setFreeCount(10);

        zoneInstanceCountVOList.add(zoneInstanceCountVO1);
        zoneInstanceCountVOList.add(zoneInstanceCountVO2);
        zoneInstanceCountVOList.add(zoneInstanceCountVO3);
        zoneInstanceCountVOList.add(zoneInstanceCountVO4);

        zoneInstanceCountVOList.sort((o1, o2) -> {
            int i = o1.getUsedCount().compareTo(o2.getUsedCount());
            if (i != 0) {
                return i;
            } else {
                return o2.getFreeCount().compareTo(o1.getFreeCount());
            }
        });

        for (ZoneInstanceCountVO zoneInstanceCountVO : zoneInstanceCountVOList) {
            System.out.println(zoneInstanceCountVO.getZone() + " used:" + zoneInstanceCountVO.getUsedCount() + " free:" + zoneInstanceCountVO.getFreeCount());
        }
    }
}
