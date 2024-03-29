package com.worldofbooks.listingsreport.database.validation;

import com.worldofbooks.listingsreport.api.Listing;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

public class ViolationDataSet {
    private Listing listing;
    private Set<ConstraintViolation<Listing>> violations;
    private List<String> referenceViolations;

    public ViolationDataSet() {
    }

    public ViolationDataSet(Listing listing, Set<ConstraintViolation<Listing>> violations, List<String> referenceViolations) {
        this.listing = listing;
        this.violations = violations;
        this.referenceViolations = referenceViolations;
    }

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public Set<ConstraintViolation<Listing>> getViolations() {
        return violations;
    }

    public void setViolations(Set<ConstraintViolation<Listing>> violations) {
        this.violations = violations;
    }

    public List<String> getReferenceViolations() {
        return referenceViolations;
    }

    public void setReferenceViolations(List<String> referenceViolations) {
        this.referenceViolations = referenceViolations;
    }
}
