package com.worldofbooks.listingsreport.output;

import com.worldofbooks.listingsreport.database.DatabaseHandler;
import com.worldofbooks.listingsreport.api.ListingDataSet;
import com.worldofbooks.listingsreport.database.ListingRepository;
import com.worldofbooks.listingsreport.database.validation.ListingValidationResult;
import com.worldofbooks.listingsreport.database.validation.ListingValidator;
import com.worldofbooks.listingsreport.api.*;
import com.worldofbooks.listingsreport.database.validation.ViolationDataSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class ReportMaker {

    private DatabaseHandler databaseHandler;
    private ListingRepository listingRepository;
    private ApiHandler apiHandler;
    private ListingValidator listingValidator;
    private ReportProcessor reportProcessor;

    @Value(value = "${ftp.server}")
    private String ftpServer;
    @Value(value = "${ftp.port}")
    private String ftpPort;
    @Value(value = "${ftp.user}")
    private String ftpUser;
    @Value(value = "${ftp.password}")
    private String ftpPassword;

    @Autowired
    public ReportMaker(DatabaseHandler databaseHandler, ListingRepository listingRepository,
                       ApiHandler apiHandler, ListingValidator listingValidator, ReportProcessor reportProcessor) {
        this.databaseHandler = databaseHandler;
        this.listingRepository = listingRepository;
        this.apiHandler = apiHandler;
        this.listingValidator = listingValidator;
        this.reportProcessor = reportProcessor;
    }

    @Transactional
    public void generateListingReport(Path importLogPath, Path localReportPath, String ftpPath) {
        ListingDataSet listingDataSet = apiHandler.getListingDataSetFromApi();
        databaseHandler.saveReferences(listingDataSet.getReferenceDataSet());

        ListingValidationResult listingValidationResult = listingValidator.validateListings(listingDataSet);

        List<Listing> validatedListings = listingValidationResult.getValidatedListings();
        databaseHandler.saveEntities(validatedListings, listingRepository);

        try (ViolationWriterCsv violationWriterCsv = new ViolationWriterCsv(importLogPath);
             FtpClient ftpClient = new FtpClient(ftpServer, Integer.parseInt(ftpPort), ftpUser, ftpPassword)) {
            List<ViolationDataSet> violationDataSets = listingValidationResult.getViolationDataSets();
            violationWriterCsv.processViolations(violationDataSets);

            ReportDto reportDto = reportProcessor.collectReportData(validatedListings);
            FileHandlerJson fileHandlerJson = new FileHandlerJsonImpl(localReportPath);
            fileHandlerJson.handleReportData(reportDto);

            ftpClient.sendToFtp(localReportPath, ftpPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            Files.delete(localReportPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

}
