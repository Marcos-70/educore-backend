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
    private AuditFileDTO auditFile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditFileDTO {
        private HeaderDTO header;
        private MasterFilesDTO masterFiles;
        private SourceDocumentsDTO sourceDocuments;
    }

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
        private String postalCode;
        private String phone;
        private String email;
        private String website;
        private String fiscalYear;
        private String startDate;
        private String endDate;
        private String dateCreated;
        private String dateGenerated;
        private String taxEntity;
        private String taxCompany;
        private String fiscalZone;
        private String auditVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MasterFilesDTO {
        private List<CustomerDTO> customers;
        private List<SupplierDTO> suppliers;
        private List<ProductDTO> products;
        private List<TaxTableDTO> taxTable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerDTO {
        private String customerID;
        private String accountID;
        private String companyName;
        private String contactName;
        private String billingAddress;
        private String postalCode;
        private String city;
        private String country;
        private String phone;
        private String email;
        private String fax;
        private String website;
        private String taxRegistrationNumber;
        private String taxCountry;
        private String taxType;
        private String taxNumber;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierDTO {
        private String supplierID;
        private String accountID;
        private String companyName;
        private String contactName;
        private String billingAddress;
        private String postalCode;
        private String city;
        private String country;
        private String phone;
        private String email;
        private String fax;
        private String website;
        private String taxRegistrationNumber;
        private String taxCountry;
        private String taxType;
        private String taxNumber;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDTO {
        private String productType;
        private String productCode;
        private String productDescription;
        private String productGroup;
        private String numberType;
        private String quantityType;
        private String unitOfMeasure;
        private String unitPrice;
        private String taxType;
        private String taxPercentage;
        private String olympoCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxTableDTO {
        private String taxType;
        private String taxCountryRegion;
        private String taxCode;
        private String description;
        private String taxPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDocumentsDTO {
        private SalesInvoicesDTO salesInvoices;
        private PaymentsDTO payments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesInvoicesDTO {
        private List<InvoiceDTO> invoice;
        private NumberOfEntries number;
        private TotalDebit totalDebit;
        private TotalCredit totalCredit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDTO {
        private InvoiceHeaderDTO invoiceHeader;
        private List<InvoiceLineDTO> invoiceLines;
        private InvoiceTotalsDTO invoiceTotals;
        private InvoiceDocumentStatusDTO documentStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceHeaderDTO {
        private String invoiceNo;
        private String atCudCode;
        private String invoiceDate;
        private String invoiceType;
        private String sourceID;
        private String eACCode;
        private String systemID;
        private String customerID;
        private String customerTaxID;
        private String customerName;
        private String billingAddress;
        private String postalCode;
        private String city;
        private String country;
        private String customerEmail;
        private String shipTo;
        private String shipFrom;
        private String dueDate;
        private String invoiceStatus;
        private String invoiceStatusDate;
        private String hash;
        private String hashControl;
        private String period;
        private String paymentStatus;
        private String paymentStatusDate;
        private String sourceBilling;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineDTO {
        private String lineNumber;
        private String productCode;
        private String productDescription;
        private String quantity;
        private String unitOfMeasure;
        private String unitPrice;
        private String taxPointDate;
        private String description;
        private InvoiceLineAmountsDTO debitAmount;
        private InvoiceLineAmountsDTO creditAmount;
        private String taxType;
        private String taxCode;
        private InvoiceLineTaxDTO tax;
        private String settlementAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineAmountsDTO {
        private String amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceLineTaxDTO {
        private String taxType;
        private String taxCode;
        private String taxPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceTotalsDTO {
        private InvoiceLineAmountsDTO taxPayable;
        private InvoiceLineAmountsDTO netTotal;
        private InvoiceLineAmountsDTO grossTotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDocumentStatusDTO {
        private String invoiceStatus;
        private String invoiceStatusDate;
        private String reason;
        private String dateChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentsDTO {
        private List<PaymentDTO> payment;
        private NumberOfEntries number;
        private TotalDebit totalDebit;
        private TotalCredit totalCredit;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDTO {
        private PaymentHeaderDTO paymentHeader;
        private List<PaymentLineDTO> paymentLines;
        private PaymentDocumentStatusDTO documentStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHeaderDTO {
        private String paymentRefNo;
        private String atCudCode;
        private String period;
        private String transactionDate;
        private String paymentType;
        private String description;
        private String sourceID;
        private String systemID;
        private String customerID;
        private String customerTaxID;
        private String customerName;
        private String paymentMethod;
        private String paymentStatus;
        private String paymentStatusDate;
        private String sourcePayment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentLineDTO {
        private String lineNumber;
        private String sourceDocumentID;
        private String sourceDocumentType;
        private String sourceDocumentDate;
        private String description;
        private InvoiceLineAmountsDTO debitAmount;
        private InvoiceLineAmountsDTO creditAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDocumentStatusDTO {
        private String paymentStatus;
        private String paymentStatusDate;
        private String reason;
        private String dateChanged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NumberOfEntries {
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalDebit {
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TotalCredit {
        private String value;
    }
}
