package com.mpos.lottery.te.test.integration;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.gamespec.prize.Payout;
import com.mpos.lottery.te.gamespec.prize.PayoutDetail;
import com.mpos.lottery.te.gamespec.prize.web.PrizeLevelItemDto;
import com.mpos.lottery.te.gamespec.sale.BaseEntry;
import com.mpos.lottery.te.gamespec.sale.BaseTamperProofTicket;
import com.mpos.lottery.te.gamespec.sale.BaseTicket;
import com.mpos.lottery.te.merchant.domain.BalanceTransactions;
import com.mpos.lottery.te.merchant.web.DailyActivityReport;
import com.mpos.lottery.te.trans.domain.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

/**
 * This test will be ran against <code>DispatchServlet</code> directly, that says we must support lookup
 * <code>ApplicationContext</code> from <code>ServletContext</code>, refer to
 * {@link org.springframework.web.context.support.WebApplicationContextUtils}
 * <p/>
 * Spring TestContext Framework. If extending from <code>AbstractTransactionalJUnit4SpringContextTests</code>, you don't
 * need to declare <code>@RunWith</code>, <code>TestExecutionListeners(3 default listeners)</code> and
 * <code>@Transactional</code>. Refer to {@link AbstractTransactionalJUnit4SpringContextTests} for more information.
 * <p/>
 * Legacy JUnit 3.8 class hierarchy is deprecated. Under new sprint test context framework, a field of property must be
 * annotated with <code>@Autowired</code> or <code>@Resource</code>( <code>@Autowired</code> in conjunction with
 * <code>@Qualifier</code>) explicitly to let spring inject dependency automatically.
 * <p/>
 * Reference:
 * <ul>
 * <li>https://jira.springsource.org/browse/SPR-5243</li>
 * <li>
 * http://forum.springsource.org/showthread.php?86124-How -to-register- BeanPostProcessor-programaticaly</li>
 * </ul>
 * 
 * @author Ramon Li
 */

// @RunWith(SpringJUnit4ClassRunner.class)

// Make sure loading a web application context.
@WebAppConfiguration
@ContextConfiguration(locations = { "/spring/spring-core.xml", "/spring/spring-core-dao.xml",
        "/spring/game/spring-raffle.xml", "/spring/game/spring-ig.xml", "/spring/game/spring-extraball.xml",
        "/spring/game/spring-lotto.xml", "/spring/game/spring-toto.xml", "/spring/game/spring-lfn.xml",
        "/spring/spring-3rdparty.xml", "/spring/game/spring-magic100.xml", "/spring/game/spring-digital.xml",
        "/spring/game/spring-union.xml", "/spring/spring-amqp.xml", "/spring/game/spring-vat.xml",
        "/spring/game/spring-bingo.xml", "/spring/vas/spring-airtime.xml", "/spring/vas/spring-teleco-voucher.xml" })
// this annotation defines the transaction manager for each test case.
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
// As our TEST extending from AbstractTransactionalJUnit4SpringContextTests,
// below 3 listeners have been registered by default, and it will be inherited
// by subclass.
// @TestExecutionListeners(listeners = {ShardAwareTestExecutionListener.class})
// @Transactional
public class BaseTransactionalIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
    protected static Log logger = LogFactory.getLog(BaseTransactionalIntegrationTest.class);
    // SPRING DEPENDENCIES
    /**
     * Always auto wire the data source to a javax.sql.DataSource with name 'dataSource' even there are multiple data
     * sources. It means there must be a DataSource bean named 'dataSource' and a
     * <code>PlatformTransactionManager</code> named 'transactionManager'.
     * <p/>
     * 
     * @see AbstractTransactionalJUnit4SpringContextTests#setDataSource(javax.sql.DataSource)
     */
    @PersistenceContext(unitName = "lottery_te")
    protected EntityManager entityManager;

    protected String dateFormat = "yyyyMMddHHmmss";

    /**
     * do something if want configure test case when initialization.
     */
    public BaseTransactionalIntegrationTest() {
        // initialize MLottery context.
        MLotteryContext.getInstance();
    }

    // run once for current test suite.
    @BeforeClass
    public static void beforeClass() {
        logger.trace("@BeforeClass:beforeClass()");
    }

    /**
     * logic to verify the initial state before a transaction is started.
     * <p/>
     * The @BeforeTransaction methods declared in superclass will be run after those of the current class. Supported by
     * {@link TransactionalTestExecutionListener}
     */
    @BeforeTransaction
    public void prepareServlet() throws Exception {
        logger.trace("@BeforeTransaction:verifyInitialDatabaseState()");
    }

    /**
     * As the generation of risk control log is in a independent transaction, we must prepare them before each test.
     */
    @BeforeTransaction
    public void prepareRiskControlLog() throws Exception {
        logger.trace("@BeforeTransaction:prepareRiskControlLog()");
        String sql1 = "delete from BD_RISK_BETTING";
        this.executeSqlInNewTransaction(sql1);
    }

    /**
     * Set up test data within the transaction.
     * <p/>
     * The @Before methods of superclass will be run before those of the current class. No other ordering is defined.
     * <p/>
     * NOTE: Any before methods (for example, methods annotated with JUnit 4's <code>@Before</code>) and any after
     * methods (such as methods annotated with JUnit 4's <code>@After</code>) are executed within a transaction.
     */
    @Before
    public void setUpTestDataWithinTransaction() {
        logger.trace("@Before:setUpTestDataWithinTransaction()");
        this.initializeMLotteryContext();
    }

    /**
     * execute "tear down" logic within the transaction.
     * <p/>
     * The @After methods declared in superclass will be run after those of the current class.
     */
    @After
    public void tearDownWithinTransaction() throws Exception {
        logger.trace("@After:tearDownWithinTransaction()");
    }

    /**
     * logic to verify the final state after transaction has rolled back.
     * <p/>
     * The @AfterTransaction methods declared in superclass will be run after those of the current class.
     */
    @AfterTransaction
    public void verifyFinalDatabaseState() throws Exception {
        logger.trace("@AfterTransaction:verifyFinalDatabaseState()");
    }

    @AfterClass
    public static void afterClass() {
        logger.trace("@AfterClass:afterClass()");
    }

    // ----------------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------------

    protected void initializeMLotteryContext() {
        logger.debug("Retrieve a ApplicationContext(" + this.applicationContext + ").");
        MLotteryContext.getInstance().setBeanFactory(this.applicationContext);
    }

    protected void printMethod() {
        StringBuffer lineBuffer = new StringBuffer("+");
        for (int i = 0; i < 120; i++) {
            lineBuffer.append("-");
        }
        lineBuffer.append("+");
        String line = lineBuffer.toString();

        // Get the test method. If index=0, it means get current method.
        StackTraceElement eles[] = new Exception().getStackTrace();
        // StackTraceElement eles[] = new Exception().getStackTrace();
        // for (StackTraceElement ele : eles){
        // System.out.println("class:" + ele.getClassName());
        // System.out.println("method:" + ele.getMethodName());
        // }
        String className = eles[1].getClassName();
        int index = className.lastIndexOf(".");
        className = className.substring((index == -1 ? 0 : (index + 1)));

        String method = className + "." + eles[1].getMethodName();
        StringBuffer padding = new StringBuffer();
        for (int i = 0; i < line.length(); i++) {
            padding.append(" ");
        }
        logger.info(line);
        String methodSig = (method + padding.toString()).substring(0, line.length() - 3);
        logger.info("| " + methodSig + "|");
        logger.info(line);
    }

    protected String uuid() {
        UUID uuid = UUID.randomUUID();
        String uuidStr = uuid.toString();
        return uuidStr.replace("-", "");
    }

    /**
     * Execute SQL in a new transaction, won't be affected by Spring test transaction.
     */
    protected void executeSqlInNewTransaction(String... sqls) throws Exception {
        DataSource dataSource = (DataSource) this.applicationContext.getBean("dataSource");
        Connection conn = dataSource.getConnection();

        try {
            conn.setAutoCommit(false);
            for (String sql : sqls) {
                Statement statement = conn.createStatement();
                statement.execute(sql);
                statement.close();
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    protected static void printVersionOfJdbcDriver() throws Exception {
        String dbDriver = "oracle.jdbc.driver.OracleDriver";
        String dbURL = "jdbc:oracle:thin:@192.168.2.148:1521/devdb";
        String dbPW = "ramon";
        String dbUser = "ramon";

        Connection con = null;
        Class.forName(dbDriver);

        con = DriverManager.getConnection(dbURL, dbUser, dbPW);
        con.setAutoCommit(false);

        DatabaseMetaData dbmd = con.getMetaData();

        System.out.println("=====  Database info =====");
        System.out.println("DatabaseProductName: " + dbmd.getDatabaseProductName());
        System.out.println("DatabaseProductVersion: " + dbmd.getDatabaseProductVersion());
        System.out.println("DatabaseMajorVersion: " + dbmd.getDatabaseMajorVersion());
        System.out.println("DatabaseMinorVersion: " + dbmd.getDatabaseMinorVersion());
        System.out.println("=====  Driver info =====");
        System.out.println("DriverName: " + dbmd.getDriverName());
        System.out.println("DriverVersion: " + dbmd.getDriverVersion());
        System.out.println("DriverMajorVersion: " + dbmd.getDriverMajorVersion());
        System.out.println("DriverMinorVersion: " + dbmd.getDriverMinorVersion());
        System.out.println("=====  JDBC/DB attributes =====");
        System.out.print("Supports getGeneratedKeys(): ");
        if (dbmd.supportsGetGeneratedKeys()) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }

        con.close();
    }

    // ----------------------------------------------------------------
    // ASSERTION METHODS
    // ----------------------------------------------------------------

    protected void assertTransaction(Transaction expectedTrans, Transaction actualTrans) {
        assertEquals(expectedTrans.getId(), actualTrans.getId());
        assertEquals(expectedTrans.getGameId(), actualTrans.getGameId());
        assertEquals(expectedTrans.getTotalAmount().doubleValue(), actualTrans.getTotalAmount().doubleValue(), 0);
        assertEquals(expectedTrans.getTicketSerialNo(), actualTrans.getTicketSerialNo());
        assertEquals(expectedTrans.getDeviceId(), actualTrans.getDeviceId());
        assertEquals(expectedTrans.getMerchantId(), actualTrans.getMerchantId());
        assertEquals(expectedTrans.getType(), actualTrans.getType());
        assertEquals(expectedTrans.getOperatorId(), actualTrans.getOperatorId());
        assertEquals(expectedTrans.getTraceMessageId(), actualTrans.getTraceMessageId());
        assertEquals(expectedTrans.getResponseCode(), actualTrans.getResponseCode());
    }

    protected void assertTicket(BaseTicket expectTicket, BaseTicket actualTicket) {
        assertEquals(expectTicket.getSerialNo(), actualTicket.getSerialNo());
        assertEquals(expectTicket.getStatus(), actualTicket.getStatus());
        assertEquals(expectTicket.getTotalAmount().doubleValue(), actualTicket.getTotalAmount().doubleValue(), 0);
        assertEquals(expectTicket.getMultipleDraws(), actualTicket.getMultipleDraws());
        assertEquals(expectTicket.getMobile(), actualTicket.getMobile());
        assertEquals(expectTicket.getCreditCardSN(), actualTicket.getCreditCardSN());
        assertEquals(expectTicket.getDevId(), actualTicket.getDevId());
        assertEquals(expectTicket.getMerchantId(), actualTicket.getMerchantId());
        assertEquals(expectTicket.getOperatorId(), actualTicket.getOperatorId());
        assertEquals(expectTicket.getTicketFrom(), actualTicket.getTicketFrom());
        assertEquals(expectTicket.getTicketType(), actualTicket.getTicketType());
        assertEquals(expectTicket.getTransType(), actualTicket.getTransType());
        assertEquals(expectTicket.isCountInPool(), actualTicket.isCountInPool());
        assertEquals(expectTicket.getGameInstance().getId(), actualTicket.getGameInstance().getId());
        assertEquals(expectTicket.getPIN(), actualTicket.getPIN());
        assertEquals(expectTicket.getUserId(), actualTicket.getUserId());
        assertEquals(expectTicket.isWinning(), actualTicket.isWinning());
        assertEquals(expectTicket.getTotalBets(), actualTicket.getTotalBets());
        assertEquals(expectTicket.getValidationCode(), actualTicket.getValidationCode());
        assertEquals(expectTicket.getBarcode(), actualTicket.getBarcode());
        if (actualTicket instanceof BaseTamperProofTicket && expectTicket instanceof BaseTamperProofTicket) {
            if (((BaseTamperProofTicket) expectTicket).getExtendText() != null) {
                assertEquals(((BaseTamperProofTicket) expectTicket).getExtendText(),
                        ((BaseTamperProofTicket) actualTicket).getExtendText());
            }
        }
    }

    protected void assertPayout(Payout exp, Payout actual) {
        assertEquals(exp.getTransaction().getId(), actual.getTransaction().getId());
        assertEquals(exp.getGameId(), actual.getGameId());
        assertEquals(exp.getGameInstanceId(), actual.getGameInstanceId());
        assertEquals(exp.getDevId(), actual.getDevId());
        assertEquals(exp.getMerchantId(), actual.getMerchantId());
        assertEquals(exp.getOperatorId(), actual.getOperatorId());
        assertEquals(exp.getTicketSerialNo(), actual.getTicketSerialNo());
        assertEquals(exp.getBeforeTaxObjectAmount().doubleValue(), actual.getBeforeTaxObjectAmount().doubleValue(), 0);
        assertEquals(exp.getBeforeTaxTotalAmount().doubleValue(), actual.getBeforeTaxTotalAmount().doubleValue(), 0);
        assertEquals(exp.getTotalAmount().doubleValue(), actual.getTotalAmount().doubleValue(), 0);
        assertEquals(exp.getNumberOfObject(), actual.getNumberOfObject());
    }

    protected Payout findPayoutByGameInstance(String gameInstanceId, List<Payout> payouts) {
        for (Payout payout : payouts) {
            if (gameInstanceId.equals(payout.getGameInstanceId())) {
                return payout;
            }
        }
        return null;
    }

    protected void sortPrizeLevelItemDto(List<PrizeLevelItemDto> prizeLevelItemDtos) {
        Collections.sort(prizeLevelItemDtos, new Comparator<PrizeLevelItemDto>() {

            @Override
            public int compare(PrizeLevelItemDto o1, PrizeLevelItemDto o2) {
                return Integer.parseInt(o1.getPrizeLevel()) - (Integer.parseInt(o2.getPrizeLevel()));
            }

        });
    }

    protected void sortPayoutDetailByPrizeAmount(List<PayoutDetail> payoutDetails) {
        Collections.sort(payoutDetails, new Comparator<PayoutDetail>() {

            @Override
            public int compare(PayoutDetail o1, PayoutDetail o2) {
                return o1.getPrizeAmount().compareTo(o2.getPrizeAmount());
            }

        });
    }

    protected void sortPayoutByPrizeAmount(List<Payout> payouts) {
        Collections.sort(payouts, new Comparator<Payout>() {

            @Override
            public int compare(Payout o1, Payout o2) {
                return o1.getBeforeTaxTotalAmount().compareTo(o2.getBeforeTaxTotalAmount());
            }

        });
    }

    protected void sortDailyActivityReport(List<DailyActivityReport> payouts) {
        Collections.sort(payouts, new Comparator<DailyActivityReport>() {

            @Override
            public int compare(DailyActivityReport o1, DailyActivityReport o2) {
                return o1.getDate().compareTo(o2.getDate());
            }

        });
    }

    @SuppressWarnings("unchecked")
    protected void sortTicketEntries(List entries) {
        Collections.sort(entries, new Comparator<BaseEntry>() {

            @Override
            public int compare(BaseEntry o1, BaseEntry o2) {
                return o1.getSelectNumber().compareTo(o2.getSelectNumber());
            }

        });
    }

    protected void sortBalanceTransactions(List<BalanceTransactions> balanceLogs) {
        Collections.sort(balanceLogs, new Comparator<BalanceTransactions>() {

            @Override
            public int compare(BalanceTransactions o1, BalanceTransactions o2) {
                return o1.getOwnerType() - o2.getOwnerType();
            }

        });
    }

    /**
     * Convert java.util.Date to string, then compare the string of date. Due to the long value of java.util.Date is
     * different from the long value of java.util.Date retrieved from database.
     */
    protected String date2String(Date date) {
        assert date != null : "Argument 'date' can not be null.";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(date);
    }

    // ----------------------------------------------------------------
    // SPRINT DEPENDENCIES INJECTION
    // ----------------------------------------------------------------

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static void main(String args[]) throws Exception {
        printVersionOfJdbcDriver();
    }
}
