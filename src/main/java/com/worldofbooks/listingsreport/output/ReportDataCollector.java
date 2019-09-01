package com.worldofbooks.listingsreport.output;

import com.worldofbooks.listingsreport.api.Listing;
import com.worldofbooks.listingsreport.api.Marketplace;
import com.worldofbooks.listingsreport.database.MarketplaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReportDataCollector implements ReportProcessor {

    private MarketplaceRepository marketplaceRepository;
    private FileHandlerJson fileHandlerJSON;

    @Value(value = "${worldofbooks.ebay.name}")
    private String ebayName;
    @Value(value = "${worldofbooks.amazon.name}")
    private String amazonName;

    @Autowired
    public ReportDataCollector(MarketplaceRepository marketplaceRepository, FileHandlerJson fileHandlerJSON) {
        this.marketplaceRepository = marketplaceRepository;
        this.fileHandlerJSON = fileHandlerJSON;
    }

    @Override
    public void collectReportData(List<Listing> listings) {
        Marketplace ebay = marketplaceRepository.findByMarketplaceName(ebayName);
        Marketplace amazon = marketplaceRepository.findByMarketplaceName(amazonName);
        int ebayId = ebay.getId();
        int amazonId = amazon.getId();

        ReportDto reportDto = makeReportDto(listings, ebayId, amazonId);

        fileHandlerJSON.handleReportData(reportDto);
    }

    private ReportDto makeReportDto(List<Listing> listings, int ebayId, int amazonId) {
        int listingCount = listings.size();

        ReportDto reportDto = new ReportDto();
        reportDto.setListingCount(listingCount);
        reportDto.updateMarketPlaceDataWithListing(listings, ebayId, amazonId);

        List<MonthlyReport> monthlyReports = getMonthlyReports(listings, ebayId, amazonId);
        reportDto.setMonthlyReports(monthlyReports);

        return reportDto;
    }

    private List<MonthlyReport> getMonthlyReports(List<Listing> listings, int ebayId, int amazonId) {
        List<Listing> listingsWithUploadTime = listings.stream()
                .filter(listing -> listing.getUploadTime() != null)
                .sorted(new SortByUploadTime())
                .collect(Collectors.toList());

        List<MonthlyReport> monthlyReports = new ArrayList<>();

        List<Listing> listingsOfCurrentMonth = new ArrayList<>();
        int lastYear = -1;
        int lastMonth = -1;

        for (Listing listing : listingsWithUploadTime) {
            LocalDate uploadTime = listing.getUploadTime();
            if (uploadTime != null) {
                int currentYear = uploadTime.getYear();
                int currentMonth = uploadTime.getMonthValue();

                if (lastYear == -1) {
                    lastYear = currentYear;
                    lastMonth = currentMonth;
                }

                if (lastYear != currentYear || lastMonth != currentMonth) {
                    MonthlyReport monthlyReport = new MonthlyReport(lastYear + "/" + lastMonth);
                    monthlyReport.updateMarketPlaceDataWithListing(listingsOfCurrentMonth, ebayId, amazonId);
                    monthlyReports.add(monthlyReport);

                    listingsOfCurrentMonth.clear();
                }
                listingsOfCurrentMonth.add(listing);
                lastYear = currentYear;
                lastMonth = currentMonth;
            }
        }
        MonthlyReport monthlyReport = new MonthlyReport(lastYear + "/" + lastMonth);
        monthlyReport.updateMarketPlaceDataWithListing(listingsOfCurrentMonth, ebayId, amazonId);
        monthlyReports.add(monthlyReport);

        return monthlyReports;
    }

    public static final class SortByUploadTime implements Comparator<Listing> {
        @Override
        public int compare(Listing o1, Listing o2) {
            LocalDate uploadTime = o1.getUploadTime();
            LocalDate uploadTime2 = o2.getUploadTime();

            if (uploadTime == null || uploadTime2 == null) {

            }

            if (uploadTime.compareTo(uploadTime2) > 0) {
                return 1;
            } else if (uploadTime.compareTo(uploadTime2) < 0) {
                return -1;
            }
            return 0;
        }
    }
}
