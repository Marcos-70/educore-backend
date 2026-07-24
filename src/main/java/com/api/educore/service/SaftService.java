package com.api.educore.service;

import com.api.educore.dto.SaftDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaftService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final ServicePriceRepository servicePriceRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public SaftDTO generateSaft(String startStr, String endStr) {
        School school = getCurrentSchool();
        if (school == null) {
            throw new RuntimeException("Sem escola associada");
        }

        LocalDateTime startDateTime = LocalDateTime.parse(startStr);
        LocalDateTime endDateTime = LocalDateTime.parse(endStr);
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate = endDateTime.toLocalDate();

        // Header
        SaftDTO.HeaderDTO header = SaftDTO.HeaderDTO.builder()
                .companyID(String.valueOf(school.getId()))
                .companyName(school.getName())
                .taxRegistrationNumber(school.getNif() != null ? school.getNif() : "")
                .address(school.getAddress() != null ? school.getAddress() : "")
                .city(school.getCity() != null ? school.getCity() : "")
                .country(school.getCountry() != null ? school.getCountry() : "Angola")
                .phone(school.getPhone() != null ? school.getPhone() : "")
                .email(school.getEmail() != null ? school.getEmail() : "")
                .website(school.getWebsite() != null ? school.getWebsite() : "")
                .fiscalYear(String.valueOf(startDate.getYear()))
                .startDate(startStr)
                .endDate(endStr)
                .dateGenerated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .softwareVersion("MAWA ERP v1.0")
                .build();

        // Clientes (alunos)
        List<Student> students = studentRepository.findBySchoolId(school.getId());
        List<SaftDTO.CustomerDTO> customers = students.stream()
                .map(s -> SaftDTO.CustomerDTO.builder()
                        .customerID("ALU-" + s.getId())
                        .companyName(s.getFullName())
                        .address(s.getAddress() != null ? s.getAddress() : "")
                        .city("")
                        .phone(s.getPhone() != null ? s.getPhone() : "")
                        .email(s.getEmail() != null ? s.getEmail() : "")
                        .taxRegistrationNumber(s.getNif() != null ? s.getNif() : "")
                        .build())
                .collect(Collectors.toList());

        // Produtos (servicos)
        List<SaftDTO.ProductDTO> products = List.of(
                SaftDTO.ProductDTO.builder().productID("SERV-001").productType("Service").description("Propina Mensal").productCode("PROPINA").build(),
                SaftDTO.ProductDTO.builder().productID("SERV-002").productType("Service").description("Matricula").productCode("MATRICULA").build(),
                SaftDTO.ProductDTO.builder().productID("SERV-003").productType("Service").description("Transporte Escolar").productCode("TRANSPORTE").build(),
                SaftDTO.ProductDTO.builder().productID("SERV-004").productType("Service").description("Biblioteca").productCode("BIBLIOTECA").build()
        );

        // Faturas
        List<Payment> payments = paymentRepository.findBySchoolId(school.getId());
        List<SaftDTO.InvoiceDTO> invoices = payments.stream()
                .filter(p -> p.getPaymentDate() != null &&
                        !p.getPaymentDate().isBefore(startDate) &&
                        !p.getPaymentDate().isAfter(endDate))
                .map(p -> SaftDTO.InvoiceDTO.builder()
                        .invoiceNo(p.getReceiptNumber() != null ? p.getReceiptNumber() : "FT-" + p.getId())
                        .invoiceDate(p.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .customerID(p.getStudent() != null ? "ALU-" + p.getStudent().getId() : "")
                        .customerName(p.getStudent() != null ? p.getStudent().getFullName() : "")
                        .serviceDescription(p.getPaymentType() != null ? p.getPaymentType().name() : "Servico")
                        .grossTotal(p.getAmount())
                        .vatAmount(0)
                        .netTotal(p.getAmount())
                        .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CASH")
                        .status(p.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        // Pagamentos
        List<SaftDTO.PaymentDTO> paymentDtos = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID &&
                        p.getPaymentDate() != null &&
                        !p.getPaymentDate().isBefore(startDate) &&
                        !p.getPaymentDate().isAfter(endDate))
                .map(p -> SaftDTO.PaymentDTO.builder()
                        .paymentRefNo("PG-" + p.getId())
                        .paymentDate(p.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .invoiceNo(p.getReceiptNumber() != null ? p.getReceiptNumber() : "FT-" + p.getId())
                        .customerID(p.getStudent() != null ? "ALU-" + p.getStudent().getId() : "")
                        .customerName(p.getStudent() != null ? p.getStudent().getFullName() : "")
                        .amount(p.getAmount())
                        .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CASH")
                        .build())
                .collect(Collectors.toList());

        return SaftDTO.builder()
                .header(header)
                .masterFiles(SaftDTO.MasterFilesDTO.builder()
                        .customers(customers)
                        .products(products)
                        .build())
                .sourceDocuments(SaftDTO.SourceDocumentsDTO.builder()
                        .salesInvoices(invoices)
                        .payments(paymentDtos)
                        .build())
                .build();
    }

    public String generateSaftXml(String startDate, String endDate) {
        SaftDTO saft = generateSaft(startDate, endDate);
        return convertToXml(saft);
    }

    private String convertToXml(SaftDTO saft) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<AuditFile xmlns=\"urn:StandardAuditFile-Tax:AO_1.0_01\">\n");

        // Header
        xml.append("  <Header>\n");
        xml.append("    <CompanyID>").append(esc(saft.getHeader().getCompanyID())).append("</CompanyID>\n");
        xml.append("    <CompanyName>").append(esc(saft.getHeader().getCompanyName())).append("</CompanyName>\n");
        xml.append("    <TaxRegistrationNumber>").append(esc(saft.getHeader().getTaxRegistrationNumber())).append("</TaxRegistrationNumber>\n");
        xml.append("    <Address>").append(esc(saft.getHeader().getAddress())).append("</Address>\n");
        xml.append("    <City>").append(esc(saft.getHeader().getCity())).append("</City>\n");
        xml.append("    <Country>").append(esc(saft.getHeader().getCountry())).append("</Country>\n");
        xml.append("    <Phone>").append(esc(saft.getHeader().getPhone())).append("</Phone>\n");
        xml.append("    <Email>").append(esc(saft.getHeader().getEmail())).append("</Email>\n");
        xml.append("    <Website>").append(esc(saft.getHeader().getWebsite())).append("</Website>\n");
        xml.append("    <FiscalYear>").append(saft.getHeader().getFiscalYear()).append("</FiscalYear>\n");
        xml.append("    <StartDate>").append(saft.getHeader().getStartDate()).append("</StartDate>\n");
        xml.append("    <EndDate>").append(saft.getHeader().getEndDate()).append("</EndDate>\n");
        xml.append("    <DateGenerated>").append(saft.getHeader().getDateGenerated()).append("</DateGenerated>\n");
        xml.append("    <SoftwareVersion>").append(esc(saft.getHeader().getSoftwareVersion())).append("</SoftwareVersion>\n");
        xml.append("  </Header>\n");

        // MasterFiles
        xml.append("  <MasterFiles>\n");

        xml.append("    <Customers>\n");
        for (SaftDTO.CustomerDTO c : saft.getMasterFiles().getCustomers()) {
            xml.append("      <Customer>\n");
            xml.append("        <CustomerID>").append(esc(c.getCustomerID())).append("</CustomerID>\n");
            xml.append("        <CompanyName>").append(esc(c.getCompanyName())).append("</CompanyName>\n");
            xml.append("        <Address>").append(esc(c.getAddress())).append("</Address>\n");
            xml.append("        <City>").append(esc(c.getCity())).append("</City>\n");
            xml.append("        <Phone>").append(esc(c.getPhone())).append("</Phone>\n");
            xml.append("        <Email>").append(esc(c.getEmail())).append("</Email>\n");
            xml.append("        <TaxRegistrationNumber>").append(esc(c.getTaxRegistrationNumber())).append("</TaxRegistrationNumber>\n");
            xml.append("      </Customer>\n");
        }
        xml.append("    </Customers>\n");

        xml.append("    <Products>\n");
        for (SaftDTO.ProductDTO p : saft.getMasterFiles().getProducts()) {
            xml.append("      <Product>\n");
            xml.append("        <ProductID>").append(esc(p.getProductID())).append("</ProductID>\n");
            xml.append("        <ProductType>").append(esc(p.getProductType())).append("</ProductType>\n");
            xml.append("        <Description>").append(esc(p.getDescription())).append("</Description>\n");
            xml.append("        <ProductCode>").append(esc(p.getProductCode())).append("</ProductCode>\n");
            xml.append("      </Product>\n");
        }
        xml.append("    </Products>\n");

        xml.append("  </MasterFiles>\n");

        // SourceDocuments
        xml.append("  <SourceDocuments>\n");

        xml.append("    <SalesInvoices>\n");
        for (SaftDTO.InvoiceDTO inv : saft.getSourceDocuments().getSalesInvoices()) {
            xml.append("      <Invoice>\n");
            xml.append("        <InvoiceNo>").append(esc(inv.getInvoiceNo())).append("</InvoiceNo>\n");
            xml.append("        <InvoiceDate>").append(inv.getInvoiceDate()).append("</InvoiceDate>\n");
            xml.append("        <CustomerID>").append(esc(inv.getCustomerID())).append("</CustomerID>\n");
            xml.append("        <CustomerName>").append(esc(inv.getCustomerName())).append("</CustomerName>\n");
            xml.append("        <ServiceDescription>").append(esc(inv.getServiceDescription())).append("</ServiceDescription>\n");
            xml.append("        <GrossTotal>").append(String.format("%.2f", inv.getGrossTotal())).append("</GrossTotal>\n");
            xml.append("        <VATAmount>").append(String.format("%.2f", inv.getVatAmount())).append("</VATAmount>\n");
            xml.append("        <NetTotal>").append(String.format("%.2f", inv.getNetTotal())).append("</NetTotal>\n");
            xml.append("        <PaymentMethod>").append(esc(inv.getPaymentMethod())).append("</PaymentMethod>\n");
            xml.append("        <Status>").append(esc(inv.getStatus())).append("</Status>\n");
            xml.append("      </Invoice>\n");
        }
        xml.append("    </SalesInvoices>\n");

        xml.append("    <Payments>\n");
        for (SaftDTO.PaymentDTO pay : saft.getSourceDocuments().getPayments()) {
            xml.append("      <Payment>\n");
            xml.append("        <PaymentRefNo>").append(esc(pay.getPaymentRefNo())).append("</PaymentRefNo>\n");
            xml.append("        <PaymentDate>").append(pay.getPaymentDate()).append("</PaymentDate>\n");
            xml.append("        <InvoiceNo>").append(esc(pay.getInvoiceNo())).append("</InvoiceNo>\n");
            xml.append("        <CustomerID>").append(esc(pay.getCustomerID())).append("</CustomerID>\n");
            xml.append("        <CustomerName>").append(esc(pay.getCustomerName())).append("</CustomerName>\n");
            xml.append("        <Amount>").append(String.format("%.2f", pay.getAmount())).append("</Amount>\n");
            xml.append("        <PaymentMethod>").append(esc(pay.getPaymentMethod())).append("</PaymentMethod>\n");
            xml.append("      </Payment>\n");
        }
        xml.append("    </Payments>\n");

        xml.append("  </SourceDocuments>\n");
        xml.append("</AuditFile>");

        return xml.toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
