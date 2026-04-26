package ru.razborka.marketplace.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(indexName = "marketplace_listings")
public class ListingSearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Double)
    private double price;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Long)
    private Long sellerId;

    @Field(type = FieldType.Keyword)
    private String sellerCity;

    @Field(type = FieldType.Keyword)
    private String partCondition;

    @Field(type = FieldType.Keyword)
    private String originalReplica;

    @Field(type = FieldType.Keyword)
    private String vin;

    @Field(type = FieldType.Keyword)
    private String catalogBlock;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword, index = false)
    private String firstPhotoUrl;

    @Field(type = FieldType.Integer)
    private Integer vehicleYear;

    @Field(type = FieldType.Integer)
    private Integer mileageKm;

    @Field(type = FieldType.Keyword)
    private List<String> brands = new ArrayList<>();

    @Field(type = FieldType.Keyword)
    private List<String> models = new ArrayList<>();

    @Field(type = FieldType.Keyword)
    private List<String> generations = new ArrayList<>();

    @Field(type = FieldType.Double)
    private List<Double> engineVolumes = new ArrayList<>();

    @Field(type = FieldType.Double)
    private Double compatEngineVolMin;

    @Field(type = FieldType.Double)
    private Double compatEngineVolMax;

    @Field(type = FieldType.Integer)
    private Integer compatYearFromMin;

    @Field(type = FieldType.Integer)
    private Integer compatYearToMax;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerCity() {
        return sellerCity;
    }

    public void setSellerCity(String sellerCity) {
        this.sellerCity = sellerCity;
    }

    public String getPartCondition() {
        return partCondition;
    }

    public void setPartCondition(String partCondition) {
        this.partCondition = partCondition;
    }

    public String getOriginalReplica() {
        return originalReplica;
    }

    public void setOriginalReplica(String originalReplica) {
        this.originalReplica = originalReplica;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getCatalogBlock() {
        return catalogBlock;
    }

    public void setCatalogBlock(String catalogBlock) {
        this.catalogBlock = catalogBlock;
    }

    public String getFirstPhotoUrl() {
        return firstPhotoUrl;
    }

    public void setFirstPhotoUrl(String firstPhotoUrl) {
        this.firstPhotoUrl = firstPhotoUrl;
    }

    public Integer getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public Integer getMileageKm() {
        return mileageKm;
    }

    public void setMileageKm(Integer mileageKm) {
        this.mileageKm = mileageKm;
    }

    public List<String> getBrands() {
        return brands;
    }

    public void setBrands(List<String> brands) {
        this.brands = brands;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public List<String> getGenerations() {
        return generations;
    }

    public void setGenerations(List<String> generations) {
        this.generations = generations;
    }

    public List<Double> getEngineVolumes() {
        return engineVolumes;
    }

    public void setEngineVolumes(List<Double> engineVolumes) {
        this.engineVolumes = engineVolumes;
    }

    public Double getCompatEngineVolMin() {
        return compatEngineVolMin;
    }

    public void setCompatEngineVolMin(Double compatEngineVolMin) {
        this.compatEngineVolMin = compatEngineVolMin;
    }

    public Double getCompatEngineVolMax() {
        return compatEngineVolMax;
    }

    public void setCompatEngineVolMax(Double compatEngineVolMax) {
        this.compatEngineVolMax = compatEngineVolMax;
    }

    public Integer getCompatYearFromMin() {
        return compatYearFromMin;
    }

    public void setCompatYearFromMin(Integer compatYearFromMin) {
        this.compatYearFromMin = compatYearFromMin;
    }

    public Integer getCompatYearToMax() {
        return compatYearToMax;
    }

    public void setCompatYearToMax(Integer compatYearToMax) {
        this.compatYearToMax = compatYearToMax;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static ListingSearchDocument fromListing(
            ru.razborka.marketplace.listing.domain.Listing l,
            String firstPhoto
    ) {
        ListingSearchDocument d = new ListingSearchDocument();
        d.setId(l.getId());
        d.setTitle(l.getTitle());
        d.setPrice(l.getPrice() == null ? 0 : l.getPrice().doubleValue());
        d.setCategoryId(l.getCategory().getId());
        d.setSellerId(l.getSeller().getId());
        d.setSellerCity(l.getSeller().getCity() == null ? "" : l.getSeller().getCity());
        d.setPartCondition(l.getCondition() == null ? "" : l.getCondition());
        d.setOriginalReplica(l.getOriginalReplica() == null ? "" : l.getOriginalReplica());
        d.setVin(l.getVin() == null ? "" : l.getVin());
        d.setCatalogBlock(l.getCatalogBlock() == null ? "" : l.getCatalogBlock().name());
        d.setStatus(l.getStatus().name());
        d.setFirstPhotoUrl(firstPhoto == null ? "" : firstPhoto);
        d.setVehicleYear(l.getVehicleYear());
        d.setMileageKm(l.getMileageKm());
        d.setCreatedAt(l.getCreatedAt());
        List<String> brands = new ArrayList<>();
        List<String> models = new ArrayList<>();
        List<String> generations = new ArrayList<>();
        List<Double> volumes = new ArrayList<>();
        Integer yMin = null;
        Integer yMax = null;
        for (var c : l.getCompatibility()) {
            if (c.getBrand() != null && !c.getBrand().isBlank()) {
                brands.add(c.getBrand());
            }
            if (c.getModel() != null && !c.getModel().isBlank()) {
                models.add(c.getModel());
            }
            if (c.getGeneration() != null && !c.getGeneration().isBlank()) {
                generations.add(c.getGeneration());
            }
            if (c.getEngineVolume() != null) {
                volumes.add(c.getEngineVolume().doubleValue());
            }
            if (c.getYearFrom() != null) {
                yMin = yMin == null ? c.getYearFrom() : Math.min(yMin, c.getYearFrom());
            }
            if (c.getYearTo() != null) {
                yMax = yMax == null ? c.getYearTo() : Math.max(yMax, c.getYearTo());
            }
        }
        if (yMin == null && l.getVehicleYear() != null) {
            yMin = l.getVehicleYear();
        }
        if (yMax == null && l.getVehicleYear() != null) {
            yMax = l.getVehicleYear();
        }
        d.setBrands(brands);
        d.setModels(models);
        d.setGenerations(generations);
        d.setEngineVolumes(volumes);
        d.setCompatYearFromMin(yMin);
        d.setCompatYearToMax(yMax);
        if (!volumes.isEmpty()) {
            double vmin = volumes.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double vmax = volumes.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            d.setCompatEngineVolMin(vmin);
            d.setCompatEngineVolMax(vmax);
        }
        return d;
    }
}
