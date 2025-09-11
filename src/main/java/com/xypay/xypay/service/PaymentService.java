package com.xypay.xypay.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    public String exportPaymentsAsIso20022Xml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Document>\n  <CstmrCdtTrfInitn>\n    <GrpHdr>\n      <MsgId>MSG123</MsgId>\n      <CreDtTm>2024-06-01T12:00:00</CreDtTm>\n    </GrpHdr>\n    <PmtInf>\n      <PmtInfId>PMT456</PmtInfId>\n      <Dbtr>\n        <Nm>John Doe</Nm>\n      </Dbtr>\n      <CdtTrfTxInf>\n        <Amt>1000.00</Amt>\n        <Cdtr>\n          <Nm>Jane Smith</Nm>\n        </Cdtr>\n      </CdtTrfTxInf>\n    </PmtInf>\n  </CstmrCdtTrfInitn>\n</Document>";
    }
    public int importPaymentsFromIso20022Xml(String xml) {
        return 1;
    }
}
