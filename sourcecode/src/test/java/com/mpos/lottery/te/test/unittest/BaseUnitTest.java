package com.mpos.lottery.te.test.unittest;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.SysConfiguration;
import com.mpos.lottery.te.config.dao.SysConfigurationDao;

import org.easymock.EasyMock;
import org.springframework.beans.factory.BeanFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BaseUnitTest {
    // Constants definition
    public static final String TMP_DIR = "c:/temp/SERIALKEY/";
    public static final String RSA_PRIVATE_KEY = TMP_DIR + "RSA_private.key";
    public static final String RSA_PUBLIC_KEY = TMP_DIR + "RSA_public.key";

    protected String getXmlFromClassPath(String classpathFile) throws Exception {
        this.prepareContext();

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile);
        // InputStream is =
        // this.getClass().getClassLoader().getResourceAsStream(classpathFile);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        for (String tmp = br.readLine(); tmp != null; tmp = br.readLine()) {
            buffer.append(tmp);
        }
        return buffer.toString();
    }

    protected void prepareContext() {
        MLotteryContext context = MLotteryContext.getInstance();
        SysConfiguration conf = new SysConfiguration();
        conf.setEncryptSerialNo(false);

        SysConfigurationDao dao = EasyMock.createMock(SysConfigurationDao.class);
        EasyMock.expect(dao.getSysConfiguration()).andReturn(conf).anyTimes();
        EasyMock.replay(dao);
        BeanFactory mockFactory = EasyMock.createMock(BeanFactory.class);
        EasyMock.expect(mockFactory.getBean(context.get(MLotteryContext.ENTRY_BEAN_SYSCONFDAO))).andReturn(dao)
                .anyTimes();
        // replay() translate a mock from record state into object state(Mock
        // object).
        EasyMock.replay(mockFactory);

        context.setBeanFactory(mockFactory);
    }
}
