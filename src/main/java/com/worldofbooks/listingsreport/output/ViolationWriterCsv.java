package com.worldofbooks.listingsreport.output;

import com.worldofbooks.listingsreport.api.Listing;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.validation.ConstraintViolation;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ViolationWriterCsv implements ViolationProcessor, Closeable {

    private static final String SAMPLE_CSV_FILE = "importLog.csv";
    private final BufferedWriter writer;
    private final CSVPrinter csvPrinter;

    public ViolationWriterCsv() throws IOException {
        this.writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));

        this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
            .withHeader("ListingId", "MarketplaceName", "InvalidField"));
    }

    @Override
    public void processViolations(Set<ConstraintViolation<Listing>> violations, List<String> referenceViolations, Listing listing) {
        List<ViolationDto> violationDtos = getViolationDtosForCSV(violations, referenceViolations, listing);

        try {
            writeDtosToCSV(violationDtos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeDtosToCSV(List<ViolationDto> violationDtos) throws IOException {
        for (ViolationDto violationDto : violationDtos) {
            csvPrinter.printRecord(violationDto.listingId, String.valueOf(violationDto.marketplaceName), violationDto.fieldName);
        }
    }


    private List<ViolationDto> getViolationDtosForCSV(Set<ConstraintViolation<Listing>> violations, List<String> referenceViolations, Listing listing) {
        List<ViolationDto> violationDtos = new ArrayList<>();
        String id = listing.getId();
        int marketplace = listing.getMarketplace();

        violations.forEach(violation -> {
            ViolationDto violationDto = new ViolationDto(
                id,
                marketplace,
                violation.getPropertyPath().toString());

            violationDtos.add(violationDto);
        });

        referenceViolations.forEach(violation -> {
            ViolationDto violationDto = new ViolationDto(
                id,
                marketplace,
                violation);

            violationDtos.add(violationDto);
        });
        return violationDtos;
    }

    @Override
    public void close() throws IOException {
        writer.close();
        csvPrinter.close();
    }


    private static final class ViolationDto {
        private final String listingId;
        private final int marketplaceName;
        private final String fieldName;

        public ViolationDto(String listingId, int marketplaceName, String fieldName) {
            this.listingId = listingId;
            this.marketplaceName = marketplaceName;
            this.fieldName = fieldName;
        }

        public String getListingId() {
            return listingId;
        }

        public int getMarketplaceName() {
            return marketplaceName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}