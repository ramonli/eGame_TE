package com.mpos.lottery.te.valueaddservice.vat;

import com.mpos.lottery.te.valueaddservice.vat.sale.OfflineVatUploadIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2BSaleCancellationIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2BSaleIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2BSaleRefundIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2CSaleCancellationIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2CSaleIntegrationTest;
import com.mpos.lottery.te.valueaddservice.vat.sale.VatB2CSaleRefundIntegrationTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ VatB2BSaleCancellationIntegrationTest.class, VatB2CSaleIntegrationTest.class,
        VatB2BSaleRefundIntegrationTest.class, VatB2BSaleIntegrationTest.class,
        VatB2CSaleCancellationIntegrationTest.class, DownloadOfflineTicketIntegrationTest.class,
        VatB2CSaleRefundIntegrationTest.class, OfflineVatUploadIntegrationTest.class })
public class VatTestSuite {

}
