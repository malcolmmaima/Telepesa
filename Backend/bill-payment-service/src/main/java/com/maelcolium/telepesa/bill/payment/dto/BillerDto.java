package com.maelcolium.telepesa.bill.payment.dto;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillerDto {
    
    private String id;
    private String name;
    private String category;
    private String logo;
    private boolean requiresAccount;
    private String accountLabel;
    private String accountPlaceholder;
    private BillPayment.BillType billType;
    private String serviceProviderCode;
    private boolean active;
    private String description;
}
