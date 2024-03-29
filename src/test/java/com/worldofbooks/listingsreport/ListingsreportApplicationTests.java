package com.worldofbooks.listingsreport;

import com.worldofbooks.listingsreport.output.ReportMaker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ListingsreportApplication.class, webEnvironment = RANDOM_PORT)
public class ListingsreportApplicationTests {

    @Autowired
    private ReportMaker reportMaker;

    @Test
    public void contextLoads() {
    }

}
