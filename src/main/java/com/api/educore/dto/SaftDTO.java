package com.api.educore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaftDTO {
    private HeaderDTO header;
    private MasterFilesDTO masterFiles;
    private SourceDocumentsDTO sourceDocuments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeaderDTO {
        private String companyID;
        private String companyName;
        private String taxRegistrationNumber;
        private String address;
        private String city;
        private String country;
        private String phone;
        private String email;
        private String website;
        private String fiscalYear;
        private String startDate;
        private String endDate;
        private String dateGenerated;
        private String softwareVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MasterFilesDTO {
        private List<CustomerDTO> customers;
        private List<ProductDTO> products;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDTO {
        private String customerID;
        private String companyName;
        private String address;
        private String city;
        private String phone;
        private String email;
        private String taxRegistrationNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private String productID;
        private String productType;
        private String description;
        private String productCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDocumentsDTO {
        private List<InvoiceDTO> salesInvoices;
        private List<PaymentDTO> payments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDTO {
        private String invoiceNo;
        private String invoiceDate;
        private String customerID;
        private String customerName;
        private String serviceDescription;
        private double grossTotal;
        private double vatAmount;
        private double netTotal;
        private String paymentMethod;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDTO {
        private String paymentRefNo;
        private String paymentDate;
        private String invoiceNo;
        private String customerID;
        private String customerName;
        private double amount;
        private String paymentMethod;
    }
}
