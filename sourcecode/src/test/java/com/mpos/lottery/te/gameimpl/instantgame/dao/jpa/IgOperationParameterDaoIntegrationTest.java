package com.mpos.lottery.te.gameimpl.instantgame.dao.jpa;

import static org.junit.Assert.assertEquals;

import com.mpos.lottery.te.config.dao.OperationParameterDao;
import com.mpos.lottery.te.gameimpl.instantgame.domain.IgOperationParameter;
import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.Resource;

public class IgOperationParameterDaoIntegrationTest extends BaseTransactionalIntegrationTest {
    @Resource(name = "igOperationParameterDao")
    private OperationParameterDao igOperationParameterDao;

    @Test
    public void testGetById() {
        IgOperationParameter param = (IgOperationParameter) this.getIgOperationParameterDao().findById(
                IgOperationParameter.class, "1");
        this.doAssert(param);
    }

    private void doAssert(IgOperationParameter param) {
        assertEquals("1", param.getId());
        assertEquals(50, param.getMaxValidateTimes());
        assertEquals(0, param.getBeginTicketIndex());
        assertEquals(199, param.getEndTicketIndex());
    }

    public OperationParameterDao getIgOperationParameterDao() {
        // return
        // (OperationParameterDao)this.getApplicationContext().getBean("igOperationParameterDao");
        return igOperationParameterDao;
    }

    /**
     * NOTE: If no Qualifier specified, below exception will be thrown out:
     * org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name
     * 'com.mpos.lottery.te.config.dao.LottoOperatorParameterDaoImplTest': Unsatisfied dependency expressed through bean
     * property 'lottoOperatorParameterDao': : No unique bean of type
     * [com.mpos.lottery.te.config.dao.OperationParameterDao] is defined: expected single matching bean but found 2:
     * [lottoOperatorParameterDao, igOperatorParameterDao]; nested exception is
     * org.springframework.beans.factory.NoSuchBeanDefinitionException: No unique bean of type
     * [com.mpos.lottery.te.config.dao.OperationParameterDao] is defined: expected single matching bean but found 2:
     * [lottoOperatorParameterDao, igOperatorParameterDao]
     */
    public void setIgOperationParameterDao(
            @Qualifier("igOperationParameterDao") OperationParameterDao<IgOperationParameter> igOperationParameterDao) {
        this.igOperationParameterDao = igOperationParameterDao;
    }

}
