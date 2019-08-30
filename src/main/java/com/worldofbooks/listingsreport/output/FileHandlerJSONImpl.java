package com.worldofbooks.listingsreport.output;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import com.worldofbooks.listingsreport.output.ReportDataCollector.ReportDto;

import java.io.FileWriter;

@Component
public class FileHandlerJSONImpl implements FileHandlerJSON {

    private String fileName = "report.json";

    @Override
    public void handleReportData(ReportDto reportDto) {
        try {
            FileWriter file = new FileWriter(fileName);
            file.write(new Gson().toJson(reportDto));
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
