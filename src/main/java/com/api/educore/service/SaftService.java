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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaftService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public SaftDTO generateSaft(String startStr, String endStr) {
        School school = getCurrentSchool();
        if (school == null) throw new RuntimeException("Sem escola associada");

        LocalDateTime startDT = LocalDateTime.parse(startStr);
        LocalDateTime endDT = LocalDateTime.parse(endStr);
        LocalDate startDate = startDT.toLocalDate();
        LocalDate endDate = endDT.toLocalDate();

        // Header
        SaftDTO.HeaderDTO header = SaftDTO.HeaderDTO.builder()
                .companyID(String.valueOf(school.getId()))
                .companyName(school.getName())
                .taxRegistrationNumber(nvl(school.getNif()))
                .address(nvl(school.getAddress()))
                .city(nvl(school.getCity()))
                .country(nvl(school.getCountry(), "Angola"))
                .postalCode("")
                .phone(nvl(school.getPhone()))
                .email(nvl(school.getEmail()))
                .website(nvl(school.getWebsite()))
                .fiscalYear(String.valueOf(startDate.getYear()))
                .startDate(startStr)
                .endDate(endStr)
                .dateCreated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .dateGenerated(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .taxEntity(school.getName())
                .taxCompany(school.getName())
                .fiscalZone("AO")
                .auditVersion("1.0_01")
                .build();

        // MasterFiles
        List<Student> students = studentRepository.findBySchoolId(school.getId());
        List<Payment> allPayments = paymentRepository.findBySchoolId(school.getId());

        // Clientes
        List<SaftDTO.CustomerDTO> customers = new ArrayList<>();
        for (Student s : students) {
            customers.add(SaftDTO.CustomerDTO.builder()
                    .customerID("ALU-" + s.getId())
                    .accountID("411")
                    .companyName(s.getFullName())
                    .contactName(s.getGuardianName() != null ? s.getGuardianName() : s.getFullName())
                    .billingAddress(nvl(s.getAddress()))
                    .postalCode("")
                    .city("")
                    .country("AO")
                    .phone(nvl(s.getPhone()))
                    .email(nvl(s.getEmail()))
                    .fax("")
                    .website("")
                    .taxRegistrationNumber(nvl(s.getNif()))
                    .taxCountry("AO")
                    .taxType("NIF")
                    .taxNumber(nvl(s.getNif()))
                    .notes("")
                    .build());
        }

        // Produtos/Servicos
        List<SaftDTO.ProductDTO> products = List.of(
                buildProduct("PROPINA", "Propina Mensal", "S", "1"),
                buildProduct("MATRICULA", "Matricula Anual", "S", "1"),
                buildProduct("TRANSPORTE", "Transporte Escolar", "S", "1"),
                buildProduct("BIBLIOTECA", "Servico de Biblioteca", "S", "1")
        );

        // Tabela de impostos
        List<SaftDTO.TaxTableDTO> taxTable = List.of(
                SaftDTO.TaxTableDTO.builder().taxType("IVA").taxCountryRegion("AO").taxCode("NOR").description("Taxa Normal").taxPercentage("14").build(),
                SaftDTO.TaxTableDTO.builder().taxType("IVA").taxCountryRegion("AO").taxCode("RED").description("Taxa Reduzida").taxPercentage("7").build(),
                SaftDTO.TaxTableDTO.builder().taxType("IVA").taxCountryRegion("AO").taxCode("ISE").description("Isento").taxPercentage("0").build()
        );

        // Faturas no periodo
        List<Payment> periodPayments = allPayments.stream()
                .filter(p -> p.getPaymentDate() != null
                        && !p.getPaymentDate().isBefore(startDate)
                        && !p.getPaymentDate().isAfter(endDate))
                .toList();

        List<SaftDTO.InvoiceDTO> invoices = new ArrayList<>();
        for (Payment p : periodPayments) {
            String receiptNo = p.getReceiptNumber() != null ? p.getReceiptNumber() : "FT " + startDate.getYear() + "/" + String.format("%06d", p.getId());
            Student student = p.getStudent();
            double amount = p.getFinalAmount() > 0 ? p.getFinalAmount() : p.getAmount();
            double vatAmount = amount * 0.0;  // IVA isento em propinas escolares
            String invoiceStatus = p.isCancelled() ? "A" : "N";  // A=Anulada, N=Normal

            SaftDTO.InvoiceDTO inv = SaftDTO.InvoiceDTO.builder()
                    .invoiceHeader(SaftDTO.InvoiceHeaderDTO.builder()
                            .invoiceNo(receiptNo)
                            .atCudCode("")
                            .invoiceDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .invoiceType("FT")  // Fatura
                            .sourceID("MANUAL")
                            .eACCode("")
                            .systemID("MAWA-ERP")
                            .customerID(student != null ? "ALU-" + student.getId() : "")
                            .customerTaxID(student != null ? nvl(student.getNif()) : "")
                            .customerName(student != null ? student.getFullName() : "")
                            .billingAddress(student != null ? nvl(student.getAddress()) : "")
                            .postalCode("")
                            .city("")
                            .country("AO")
                            .customerEmail(student != null ? nvl(student.getEmail()) : "")
                            .shipTo("")
                            .shipFrom("")
                            .dueDate(p.getPaymentDate().plusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .invoiceStatus(invoiceStatus)
                            .invoiceStatusDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .hash("")
                            .hashControl("")
                            .period(String.valueOf(startDate.getMonthValue()))
                            .paymentStatus(p.getStatus() == PaymentStatus.PAID ? "P" : "N")  // P=Pago, N=Nao Pago
                            .paymentStatusDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .sourceBilling("P")  // P=Primeira vez, R=Repeticao
                            .build())
                    .invoiceLines(List.of(
                            SaftDTO.InvoiceLineDTO.builder()
                                    .lineNumber("1")
                                    .productCode(p.getPaymentType() != null ? p.getPaymentType().name() : "PROPINA")
                                    .productDescription(p.getDescription() != null ? p.getDescription() : getPaymentDescription(p.getPaymentType()))
                                    .quantity("1")
                                    .unitOfMeasure("Un")
                                    .unitPrice(String.format("%.2f", amount))
                                    .taxPointDate(p.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    .description(p.getDescription() != null ? p.getDescription() : getPaymentDescription(p.getPaymentType()))
                                    .debitAmount(SaftDTO.InvoiceLineAmountsDTO.builder().amount(String.format("%.2f", amount)).build())
                                    .creditAmount(SaftDTO.InvoiceLineAmountsDTO.builder().amount("0.00").build())
                                    .taxType("IVA")
                                    .taxCode("ISE")
                                    .tax(SaftDTO.InvoiceLineTaxDTO.builder().taxType("IVA").taxCode("ISE").taxPercentage("0").build())
                                    .settlementAmount("0.00")
                                    .build()
                    ))
                    .invoiceTotals(SaftDTO.InvoiceTotalsDTO.builder()
                            .taxPayable(SaftDTO.InvoiceLineAmountsDTO.builder().amount(String.format("%.2f", vatAmount)).build())
                            .netTotal(SaftDTO.InvoiceLineAmountsDTO.builder().amount(String.format("%.2f", amount)).build())
                            .grossTotal(SaftDTO.InvoiceLineAmountsDTO.builder().amount(String.format("%.2f", amount)).build())
                            .build())
                    .documentStatus(SaftDTO.InvoiceDocumentStatusDTO.builder()
                            .invoiceStatus(invoiceStatus)
                            .invoiceStatusDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .reason(p.getCancellationReason() != null ? p.getCancellationReason() : "")
                            .dateChanged(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build())
                    .build();
            invoices.add(inv);
        }

        // Pagamentos no periodo
        List<Payment> paidPayments = periodPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .toList();

        List<SaftDTO.PaymentDTO> payments = new ArrayList<>();
        for (Payment p : paidPayments) {
            Student student = p.getStudent();
            String receiptNo = p.getReceiptNumber() != null ? p.getReceiptNumber() : "REC-" + String.format("%06d", p.getId());
            double amount = p.getFinalAmount() > 0 ? p.getFinalAmount() : p.getAmount();

            payments.add(SaftDTO.PaymentDTO.builder()
                    .paymentHeader(SaftDTO.PaymentHeaderDTO.builder()
                            .paymentRefNo(receiptNo)
                            .atCudCode("")
                            .period(String.valueOf(startDate.getMonthValue()))
                            .transactionDate(p.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .paymentType("CC" + getPaymentMethodCode(p.getPaymentMethod()))  // CC=Meio Pagamento
                            .description(p.getDescription() != null ? p.getDescription() : "Pagamento " + getPaymentDescription(p.getPaymentType()))
                            .sourceID("MANUAL")
                            .systemID("MAWA-ERP")
                            .customerID(student != null ? "ALU-" + student.getId() : "")
                            .customerTaxID(student != null ? nvl(student.getNif()) : "")
                            .customerName(student != null ? student.getFullName() : "")
                            .paymentMethod(getPaymentMethodName(p.getPaymentMethod()))
                            .paymentStatus("P")
                            .paymentStatusDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .sourcePayment("P")
                            .build())
                    .paymentLines(List.of(
                            SaftDTO.PaymentLineDTO.builder()
                                    .lineNumber("1")
                                    .sourceDocumentID(receiptNo)
                                    .sourceDocumentType("FT")
                                    .sourceDocumentDate(p.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    .description(getPaymentDescription(p.getPaymentType()))
                                    .debitAmount(SaftDTO.InvoiceLineAmountsDTO.builder().amount("0.00").build())
                                    .creditAmount(SaftDTO.InvoiceLineAmountsDTO.builder().amount(String.format("%.2f", amount)).build())
                                    .build()
                    ))
                    .documentStatus(SaftDTO.PaymentDocumentStatusDTO.builder()
                            .paymentStatus("P")
                            .paymentStatusDate(p.getPaymentDate().atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE))
                            .reason("")
                            .dateChanged(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                            .build())
                    .build());
        }

        double totalDebit = invoices.stream().mapToDouble(inv -> {
            try { return Double.parseDouble(inv.getInvoiceTotals().getGrossTotal().getAmount()); } catch (Exception e) { return 0; }
        }).sum();
        double totalCredit = payments.stream().mapToDouble(pay -> {
            try { return Double.parseDouble(pay.getPaymentLines().get(0).getCreditAmount().getAmount()); } catch (Exception e) { return 0; }
        }).sum();

        return SaftDTO.builder()
                .auditFile(SaftDTO.AuditFileDTO.builder()
                        .header(header)
                        .masterFiles(SaftDTO.MasterFilesDTO.builder()
                                .customers(customers)
                                .suppliers(List.of())
                                .products(products)
                                .taxTable(taxTable)
                                .build())
                        .sourceDocuments(SaftDTO.SourceDocumentsDTO.builder()
                                .salesInvoices(SaftDTO.SalesInvoicesDTO.builder()
                                        .invoice(invoices)
                                        .number(SaftDTO.NumberOfEntries.builder().value(String.valueOf(invoices.size())).build())
                                        .totalDebit(SaftDTO.TotalDebit.builder().value(String.format("%.2f", totalDebit)).build())
                                        .totalCredit(SaftDTO.TotalCredit.builder().value(String.format("%.2f", totalDebit)).build())
                                        .build())
                                .payments(SaftDTO.PaymentsDTO.builder()
                                        .payment(payments)
                                        .number(SaftDTO.NumberOfEntries.builder().value(String.valueOf(payments.size())).build())
                                        .totalDebit(SaftDTO.TotalDebit.builder().value(String.format("%.2f", totalCredit)).build())
                                        .totalCredit(SaftDTO.TotalCredit.builder().value(String.format("%.2f", totalCredit)).build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private SaftDTO.ProductDTO buildProduct(String code, String desc, String type, String qtyType) {
        return SaftDTO.ProductDTO.builder()
                .productType("S")
                .productCode(code)
                .productDescription(desc)
                .productGroup("SERVICOS")
                .numberType("0")
                .quantityType(qtyType)
                .unitOfMeasure("Un")
                .unitPrice("0.00")
                .taxType("IVA")
                .taxPercentage("0")
                .olympoCode("")
                .build();
    }

    public String generateSaftXml(String startDate, String endDate) {
        SaftDTO saft = generateSaft(startDate, endDate);
        return buildXml(saft);
    }

    private String buildXml(SaftDTO saft) {
        StringBuilder x = new StringBuilder();
        SaftDTO.AuditFileDTO af = saft.getAuditFile();
        SaftDTO.HeaderDTO h = af.getHeader();

        x.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        x.append("<AuditFile xmlns=\"urn:StandardAuditFile-Tax:AO_1.0_01\">\n");

        // Header
        x.append("  <Header>\n");
        x.append("    <CompanyID>").append(esc(h.getCompanyID())).append("</CompanyID>\n");
        x.append("    <CompanyName>").append(esc(h.getCompanyName())).append("</CompanyName>\n");
        x.append("    <TaxRegistrationNumber>").append(esc(h.getTaxRegistrationNumber())).append("</TaxRegistrationNumber>\n");
        x.append("    <Address>").append(esc(h.getAddress())).append("</Address>\n");
        x.append("    <City>").append(esc(h.getCity())).append("</City>\n");
        x.append("    <Country>").append(esc(h.getCountry())).append("</Country>\n");
        x.append("    <PostalCode>").append(esc(h.getPostalCode())).append("</PostalCode>\n");
        x.append("    <Phone>").append(esc(h.getPhone())).append("</Phone>\n");
        x.append("    <Email>").append(esc(h.getEmail())).append("</Email>\n");
        x.append("    <Website>").append(esc(h.getWebsite())).append("</Website>\n");
        x.append("    <FiscalYear>").append(h.getFiscalYear()).append("</FiscalYear>\n");
        x.append("    <StartDate>").append(h.getStartDate()).append("</StartDate>\n");
        x.append("    <EndDate>").append(h.getEndDate()).append("</EndDate>\n");
        x.append("    <DateCreated>").append(h.getDateCreated()).append("</DateCreated>\n");
        x.append("    <DateGenerated>").append(h.getDateGenerated()).append("</DateGenerated>\n");
        x.append("    <TaxEntity>").append(esc(h.getTaxEntity())).append("</TaxEntity>\n");
        x.append("    <TaxCompany>").append(esc(h.getTaxCompany())).append("</TaxCompany>\n");
        x.append("    <FiscalZone>").append(esc(h.getFiscalZone())).append("</FiscalZone>\n");
        x.append("    <AuditVersion>").append(esc(h.getAuditVersion())).append("</AuditVersion>\n");
        x.append("  </Header>\n");

        // MasterFiles
        SaftDTO.MasterFilesDTO mf = af.getMasterFiles();
        x.append("  <MasterFiles>\n");

        // Customers
        x.append("    <Customers>\n");
        for (SaftDTO.CustomerDTO c : mf.getCustomers()) {
            x.append("      <Customer>\n");
            x.append("        <CustomerID>").append(esc(c.getCustomerID())).append("</CustomerID>\n");
            x.append("        <AccountID>").append(esc(c.getAccountID())).append("</AccountID>\n");
            x.append("        <CompanyName>").append(esc(c.getCompanyName())).append("</CompanyName>\n");
            x.append("        <ContactName>").append(esc(c.getContactName())).append("</ContactName>\n");
            x.append("        <BillingAddress>\n");
            x.append("          <AddressDetail>").append(esc(c.getBillingAddress())).append("</AddressDetail>\n");
            x.append("          <PostalCode>").append(esc(c.getPostalCode())).append("</PostalCode>\n");
            x.append("          <City>").append(esc(c.getCity())).append("</City>\n");
            x.append("          <Country>").append(esc(c.getCountry())).append("</Country>\n");
            x.append("        </BillingAddress>\n");
            x.append("        <Phone>").append(esc(c.getPhone())).append("</Phone>\n");
            x.append("        <Email>").append(esc(c.getEmail())).append("</Email>\n");
            x.append("        <Fax>").append(esc(c.getFax())).append("</Fax>\n");
            x.append("        <Website>").append(esc(c.getWebsite())).append("</Website>\n");
            x.append("        <TaxRegistrationNumber>").append(esc(c.getTaxRegistrationNumber())).append("</TaxRegistrationNumber>\n");
            x.append("        <TaxCountryRegion>").append(esc(c.getTaxCountry())).append("</TaxCountryRegion>\n");
            x.append("        <TaxType>").append(esc(c.getTaxType())).append("</TaxType>\n");
            x.append("        <TaxNumber>").append(esc(c.getTaxNumber())).append("</TaxNumber>\n");
            x.append("        <Notes>").append(esc(c.getNotes())).append("</Notes>\n");
            x.append("      </Customer>\n");
        }
        x.append("    </Customers>\n");

        // Suppliers
        x.append("    <Suppliers/>\n");

        // Products
        x.append("    <Products>\n");
        for (SaftDTO.ProductDTO p : mf.getProducts()) {
            x.append("      <Product>\n");
            x.append("        <ProductType>").append(esc(p.getProductType())).append("</ProductType>\n");
            x.append("        <ProductCode>").append(esc(p.getProductCode())).append("</ProductCode>\n");
            x.append("        <ProductDescription>").append(esc(p.getProductDescription())).append("</ProductDescription>\n");
            x.append("        <ProductGroup>").append(esc(p.getProductGroup())).append("</ProductGroup>\n");
            x.append("        <NumberType>").append(p.getNumberType()).append("</NumberType>\n");
            x.append("        <QuantityType>").append(p.getQuantityType()).append("</QuantityType>\n");
            x.append("        <UnitOfMeasure>").append(esc(p.getUnitOfMeasure())).append("</UnitOfMeasure>\n");
            x.append("        <UnitPrice>").append(p.getUnitPrice()).append("</UnitPrice>\n");
            x.append("        <TaxType>").append(esc(p.getTaxType())).append("</TaxType>\n");
            x.append("        <TaxPercentage>").append(p.getTaxPercentage()).append("</TaxPercentage>\n");
            x.append("        <OlympoCode>").append(esc(p.getOlympoCode())).append("</OlympoCode>\n");
            x.append("      </Product>\n");
        }
        x.append("    </Products>\n");

        // TaxTable
        x.append("    <TaxTable>\n");
        for (SaftDTO.TaxTableDTO t : mf.getTaxTable()) {
            x.append("      <TaxTableEntry>\n");
            x.append("        <TaxType>").append(esc(t.getTaxType())).append("</TaxType>\n");
            x.append("        <TaxCountryRegion>").append(esc(t.getTaxCountryRegion())).append("</TaxCountryRegion>\n");
            x.append("        <TaxCode>").append(esc(t.getTaxCode())).append("</TaxCode>\n");
            x.append("        <Description>").append(esc(t.getDescription())).append("</Description>\n");
            x.append("        <TaxPercentage>").append(t.getTaxPercentage()).append("</TaxPercentage>\n");
            x.append("      </TaxTableEntry>\n");
        }
        x.append("    </TaxTable>\n");

        x.append("  </MasterFiles>\n");

        // SourceDocuments
        SaftDTO.SourceDocumentsDTO sd = af.getSourceDocuments();
        x.append("  <SourceDocuments>\n");

        // SalesInvoices
        SaftDTO.SalesInvoicesDTO si = sd.getSalesInvoices();
        x.append("    <SalesInvoices>\n");
        x.append("      <NumberOfEntries>").append(si.getNumber().getValue()).append("</NumberOfEntries>\n");
        x.append("      <TotalDebit>").append(si.getTotalDebit().getValue()).append("</TotalDebit>\n");
        x.append("      <TotalCredit>").append(si.getTotalCredit().getValue()).append("</TotalCredit>\n");
        for (SaftDTO.InvoiceDTO inv : si.getInvoice()) {
            x.append("      <Invoice>\n");
            // Header
            SaftDTO.InvoiceHeaderDTO ih = inv.getInvoiceHeader();
            x.append("        <InvoiceHeader>\n");
            x.append("          <InvoiceNo>").append(esc(ih.getInvoiceNo())).append("</InvoiceNo>\n");
            x.append("          <ATCUDCode>").append(esc(ih.getAtCudCode())).append("</ATCUDCode>\n");
            x.append("          <InvoiceDate>").append(ih.getInvoiceDate()).append("</InvoiceDate>\n");
            x.append("          <InvoiceType>").append(esc(ih.getInvoiceType())).append("</InvoiceType>\n");
            x.append("          <SourceID>").append(esc(ih.getSourceID())).append("</SourceID>\n");
            x.append("          <EACCode>").append(esc(ih.getEACCode())).append("</EACCode>\n");
            x.append("          <SystemID>").append(esc(ih.getSystemID())).append("</SystemID>\n");
            x.append("          <CustomerID>").append(esc(ih.getCustomerID())).append("</CustomerID>\n");
            x.append("          <CustomerTaxID>").append(esc(ih.getCustomerTaxID())).append("</CustomerTaxID>\n");
            x.append("          <CustomerName>").append(esc(ih.getCustomerName())).append("</CustomerName>\n");
            x.append("          <BillingAddress>\n");
            x.append("            <AddressDetail>").append(esc(ih.getBillingAddress())).append("</AddressDetail>\n");
            x.append("            <PostalCode>").append(esc(ih.getPostalCode())).append("</PostalCode>\n");
            x.append("            <City>").append(esc(ih.getCity())).append("</City>\n");
            x.append("            <Country>").append(esc(ih.getCountry())).append("</Country>\n");
            x.append("          </BillingAddress>\n");
            x.append("          <CustomerEmail>").append(esc(ih.getCustomerEmail())).append("</CustomerEmail>\n");
            x.append("          <ShipTo><DeliveryDate/> <Address><AddressDetail/> <PostalCode/> <City/> <Country/></Address></ShipTo>\n");
            x.append("          <ShipFrom><DeliveryDate/> <Address><AddressDetail/> <PostalCode/> <City/> <Country/></Address></ShipFrom>\n");
            x.append("          <DueDate>").append(ih.getDueDate()).append("</DueDate>\n");
            x.append("          <InvoiceStatus>").append(esc(ih.getInvoiceStatus())).append("</InvoiceStatus>\n");
            x.append("          <InvoiceStatusDate>").append(ih.getInvoiceStatusDate()).append("</InvoiceStatusDate>\n");
            x.append("          <Hash>").append(esc(ih.getHash())).append("</Hash>\n");
            x.append("          <HashControl>").append(esc(ih.getHashControl())).append("</HashControl>\n");
            x.append("          <Period>").append(ih.getPeriod()).append("</Period>\n");
            x.append("          <PaymentStatus>").append(esc(ih.getPaymentStatus())).append("</PaymentStatus>\n");
            x.append("          <PaymentStatusDate>").append(ih.getPaymentStatusDate()).append("</PaymentStatusDate>\n");
            x.append("          <SourceBilling>").append(esc(ih.getSourceBilling())).append("</SourceBilling>\n");
            x.append("        </InvoiceHeader>\n");
            // Lines
            x.append("        <InvoiceLines>\n");
            for (SaftDTO.InvoiceLineDTO il : inv.getInvoiceLines()) {
                x.append("          <InvoiceLine>\n");
                x.append("            <LineNumber>").append(il.getLineNumber()).append("</LineNumber>\n");
                x.append("            <ProductCode>").append(esc(il.getProductCode())).append("</ProductCode>\n");
                x.append("            <ProductDescription>").append(esc(il.getProductDescription())).append("</ProductDescription>\n");
                x.append("            <Quantity>").append(il.getQuantity()).append("</Quantity>\n");
                x.append("            <UnitOfMeasure>").append(esc(il.getUnitOfMeasure())).append("</UnitOfMeasure>\n");
                x.append("            <UnitPrice>").append(il.getUnitPrice()).append("</UnitPrice>\n");
                x.append("            <TaxPointDate>").append(il.getTaxPointDate()).append("</TaxPointDate>\n");
                x.append("            <Description>").append(esc(il.getDescription())).append("</Description>\n");
                x.append("            <DebitAmount>").append(il.getDebitAmount().getAmount()).append("</DebitAmount>\n");
                x.append("            <CreditAmount>").append(il.getCreditAmount().getAmount()).append("</CreditAmount>\n");
                x.append("            <Tax>\n");
                x.append("              <TaxType>").append(esc(il.getTax().getTaxType())).append("</TaxType>\n");
                x.append("              <TaxCode>").append(esc(il.getTax().getTaxCode())).append("</TaxCode>\n");
                x.append("              <TaxPercentage>").append(il.getTax().getTaxPercentage()).append("</TaxPercentage>\n");
                x.append("            </Tax>\n");
                x.append("            <SettlementAmount>").append(il.getSettlementAmount()).append("</SettlementAmount>\n");
                x.append("          </InvoiceLine>\n");
            }
            x.append("        </InvoiceLines>\n");
            // Totals
            SaftDTO.InvoiceTotalsDTO it = inv.getInvoiceTotals();
            x.append("        <InvoiceTotals>\n");
            x.append("          <TaxPayable>").append(it.getTaxPayable().getAmount()).append("</TaxPayable>\n");
            x.append("          <NetTotal>").append(it.getNetTotal().getAmount()).append("</NetTotal>\n");
            x.append("          <GrossTotal>").append(it.getGrossTotal().getAmount()).append("</GrossTotal>\n");
            x.append("        </InvoiceTotals>\n");
            // DocumentStatus
            SaftDTO.InvoiceDocumentStatusDTO ds = inv.getDocumentStatus();
            x.append("        <DocumentStatus>\n");
            x.append("          <InvoiceStatus>").append(esc(ds.getInvoiceStatus())).append("</InvoiceStatus>\n");
            x.append("          <InvoiceStatusDate>").append(ds.getInvoiceStatusDate()).append("</InvoiceStatusDate>\n");
            x.append("          <Reason>").append(esc(ds.getReason())).append("</Reason>\n");
            x.append("          <DateChanged>").append(ds.getDateChanged()).append("</DateChanged>\n");
            x.append("        </DocumentStatus>\n");
            x.append("      </Invoice>\n");
        }
        x.append("    </SalesInvoices>\n");

        // Payments
        SaftDTO.PaymentsDTO py = sd.getPayments();
        x.append("    <Payments>\n");
        x.append("      <NumberOfEntries>").append(py.getNumber().getValue()).append("</NumberOfEntries>\n");
        x.append("      <TotalDebit>").append(py.getTotalDebit().getValue()).append("</TotalDebit>\n");
        x.append("      <TotalCredit>").append(py.getTotalCredit().getValue()).append("</TotalCredit>\n");
        for (SaftDTO.PaymentDTO pay : py.getPayment()) {
            x.append("      <Payment>\n");
            SaftDTO.PaymentHeaderDTO ph = pay.getPaymentHeader();
            x.append("        <PaymentHeader>\n");
            x.append("          <PaymentRefNo>").append(esc(ph.getPaymentRefNo())).append("</PaymentRefNo>\n");
            x.append("          <ATCUDCode>").append(esc(ph.getAtCudCode())).append("</ATCUDCode>\n");
            x.append("          <Period>").append(ph.getPeriod()).append("</Period>\n");
            x.append("          <TransactionDate>").append(ph.getTransactionDate()).append("</TransactionDate>\n");
            x.append("          <PaymentType>").append(esc(ph.getPaymentType())).append("</PaymentType>\n");
            x.append("          <Description>").append(esc(ph.getDescription())).append("</Description>\n");
            x.append("          <SourceID>").append(esc(ph.getSourceID())).append("</SourceID>\n");
            x.append("          <SystemID>").append(esc(ph.getSystemID())).append("</SystemID>\n");
            x.append("          <CustomerID>").append(esc(ph.getCustomerID())).append("</CustomerID>\n");
            x.append("          <CustomerTaxID>").append(esc(ph.getCustomerTaxID())).append("</CustomerTaxID>\n");
            x.append("          <CustomerName>").append(esc(ph.getCustomerName())).append("</CustomerName>\n");
            x.append("          <PaymentMethod>").append(esc(ph.getPaymentMethod())).append("</PaymentMethod>\n");
            x.append("          <PaymentStatus>").append(esc(ph.getPaymentStatus())).append("</PaymentStatus>\n");
            x.append("          <PaymentStatusDate>").append(ph.getPaymentStatusDate()).append("</PaymentStatusDate>\n");
            x.append("          <SourcePayment>").append(esc(ph.getSourcePayment())).append("</SourcePayment>\n");
            x.append("        </PaymentHeader>\n");
            x.append("        <PaymentLines>\n");
            for (SaftDTO.PaymentLineDTO pl : pay.getPaymentLines()) {
                x.append("          <PaymentLine>\n");
                x.append("            <LineNumber>").append(pl.getLineNumber()).append("</LineNumber>\n");
                x.append("            <SourceDocumentID>\n");
                x.append("              <ATCUDCode/>");
                x.append("              <DocID>").append(esc(pl.getSourceDocumentID())).append("</DocID>\n");
                x.append("              <DocDate>").append(pl.getSourceDocumentDate()).append("</DocDate>\n");
                x.append("              <DocType>").append(esc(pl.getSourceDocumentType())).append("</DocType>\n");
                x.append("              <Description>").append(esc(pl.getDescription())).append("</Description>\n");
                x.append("              <DebitAmount>").append(pl.getDebitAmount().getAmount()).append("</DebitAmount>\n");
                x.append("              <CreditAmount>").append(pl.getCreditAmount().getAmount()).append("</CreditAmount>\n");
                x.append("            </SourceDocumentID>\n");
                x.append("          </PaymentLine>\n");
            }
            x.append("        </PaymentLines>\n");
            SaftDTO.PaymentDocumentStatusDTO pds = pay.getDocumentStatus();
            x.append("        <DocumentStatus>\n");
            x.append("          <PaymentStatus>").append(esc(pds.getPaymentStatus())).append("</PaymentStatus>\n");
            x.append("          <PaymentStatusDate>").append(pds.getPaymentStatusDate()).append("</PaymentStatusDate>\n");
            x.append("          <Reason>").append(esc(pds.getReason())).append("</Reason>\n");
            x.append("          <DateChanged>").append(pds.getDateChanged()).append("</DateChanged>\n");
            x.append("        </DocumentStatus>\n");
            x.append("      </Payment>\n");
        }
        x.append("    </Payments>\n");

        x.append("  </SourceDocuments>\n");
        x.append("</AuditFile>");

        return x.toString();
    }

    private String getPaymentDescription(PaymentType type) {
        if (type == null) return "Servico";
        return switch (type) {
            case TUITION -> "Propina Mensal";
            case REGISTRATION -> "Matricula";
            case TRANSPORT -> "Transporte Escolar";
            case LIBRARY -> "Servico de Biblioteca";
            case EXAM -> "Exame";
            case CERTIFICATE -> "Certificado";
            case INSURANCE -> "Seguro";
            case OTHER -> "Outro Servico";
        };
    }

    private String getPaymentMethodName(PaymentMethod method) {
        if (method == null) return "Dinheiro";
        return switch (method) {
            case CASH -> "Dinheiro";
            case CARD -> "Cartao";
            case TRANSFER -> "Transferencia";
            case DEPOSIT -> "Deposito";
            case MCX -> "Multicaixa Express";
            case REFERENCE -> "Referencia";
            case MIXED -> "Misto";
        };
    }

    private String getPaymentMethodCode(PaymentMethod method) {
        if (method == null) return "CC";
        return switch (method) {
            case CASH -> "CC";
            case CARD -> "CC";
            case TRANSFER -> "CM";
            case DEPOSIT -> "CD";
            case MCX -> "CC";
            case REFERENCE -> "CM";
            case MIXED -> "CC";
        };
    }

    private String nvl(String s) { return s != null ? s : ""; }
    private String nvl(String s, String def) { return s != null ? s : def; }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
