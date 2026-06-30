package it.nexoapp.common.utils;

import it.nexoapp.common.constant.MDCConstant;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MDCUtilsTest {

    @Test
    void putMDCAndGet(){
        String uuid = UUID.randomUUID().toString();
        MDCUtils.put(MDCConstant.CALL_ID, uuid);

        assertEquals(uuid, MDCUtils.get(MDCConstant.CALL_ID), "MDC should contain the value");
    }

    @Test
    void putMDCAndRemove() {
        String uuid = UUID.randomUUID().toString();
        MDCUtils.put(MDCConstant.CALL_ID, uuid);
        MDCUtils.put(MDCConstant.CALLER, uuid);

        MDCUtils.remove(MDCConstant.CALL_ID);

        assertNull(MDCUtils.get(MDCConstant.CALL_ID), "MDC should not contain the value");
        assertEquals(uuid, MDCUtils.get(MDCConstant.CALLER), "MDC CALLER should not be removed, only requested should be removed");
    }

    @Test
    void putMDCAndClear() {
        String uuid = UUID.randomUUID().toString();
        MDCUtils.put(MDCConstant.CALL_ID, uuid);
        MDCUtils.put(MDCConstant.CALLER, uuid);

        MDCUtils.clear();

        assertNull(MDCUtils.get(MDCConstant.CALL_ID), "MDC should not contain the value");
        assertNull(MDCUtils.get(MDCConstant.CALLER), "MDC should not contain the value");
    }

}