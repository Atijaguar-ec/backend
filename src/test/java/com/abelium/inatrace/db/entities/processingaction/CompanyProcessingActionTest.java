package com.abelium.inatrace.db.entities.processingaction;

import com.abelium.inatrace.db.entities.company.Company;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class CompanyProcessingActionTest {

    private Company company;
    private ProcessingAction processingAction;
    private CompanyProcessingAction companyProcessingAction;

    @BeforeEach
    void setUp() {
        company = new Company();
        
        processingAction = new ProcessingAction();
        processingAction.setSortOrder(10L);
        processingAction.setPrefix("TEST_ACTION");
        
        companyProcessingAction = new CompanyProcessingAction(company, processingAction);
    }

    @Test
    void testConstructorWithParameters() {
        CompanyProcessingAction cpa = new CompanyProcessingAction(company, processingAction);
        
        assertEquals(company, cpa.getCompany());
        assertEquals(processingAction, cpa.getProcessingAction());
        assertTrue(cpa.getEnabled());
    }

    @Test
    void testConstructorNullCompany() {
        assertThrows(NullPointerException.class, () -> {
            new CompanyProcessingAction(null, processingAction);
        });
    }

    @Test
    void testConstructorNullProcessingAction() {
        assertThrows(NullPointerException.class, () -> {
            new CompanyProcessingAction(company, null);
        });
    }

    @Test
    void testSetCompanyNull() {
        assertThrows(NullPointerException.class, () -> {
            companyProcessingAction.setCompany(null);
        });
    }

    @Test
    void testSetProcessingActionNull() {
        assertThrows(NullPointerException.class, () -> {
            companyProcessingAction.setProcessingAction(null);
        });
    }

    @Test
    void testEnabledDefaultValue() {
        assertTrue(companyProcessingAction.getEnabled());
    }

    @Test
    void testSetEnabledNull() {
        companyProcessingAction.setEnabled(null);
        assertTrue(companyProcessingAction.getEnabled());
    }

    @Test
    void testSetEnabledFalse() {
        companyProcessingAction.setEnabled(false);
        assertFalse(companyProcessingAction.getEnabled());
    }

    @Test
    void testGetEffectiveOrderWithOverride() {
        companyProcessingAction.setOrderOverride(20);
        assertEquals(20L, companyProcessingAction.getEffectiveOrder());
    }

    @Test
    void testGetEffectiveOrderWithoutOverride() {
        assertEquals(10L, companyProcessingAction.getEffectiveOrder());
    }

    @Test
    void testGetEffectiveOrderNullProcessingAction() {
        // Create a new instance with null processing action to test the fallback
        CompanyProcessingAction cpa = new CompanyProcessingAction();
        assertEquals(0L, cpa.getEffectiveOrder());
    }

    @Test
    void testGetEffectiveLabelWithAlias() {
        companyProcessingAction.setAliasLabel("Custom Label");
        assertEquals("Custom Label", companyProcessingAction.getEffectiveLabel());
    }

    @Test
    void testGetEffectiveLabelWithoutAlias() {
        assertEquals("TEST_ACTION", companyProcessingAction.getEffectiveLabel());
    }

    @Test
    void testGetEffectiveLabelEmptyAlias() {
        companyProcessingAction.setAliasLabel("   ");
        assertEquals("TEST_ACTION", companyProcessingAction.getEffectiveLabel());
    }

    @Test
    void testSetAliasLabelTrimsWhitespace() {
        companyProcessingAction.setAliasLabel("  Test Label  ");
        assertEquals("Test Label", companyProcessingAction.getAliasLabel());
    }

    @Test
    void testSetAliasLabelNull() {
        companyProcessingAction.setAliasLabel(null);
        assertNull(companyProcessingAction.getAliasLabel());
    }

    @Test
    void testEqualsAndHashCode() {
        CompanyProcessingAction cpa1 = new CompanyProcessingAction(company, processingAction);
        CompanyProcessingAction cpa2 = new CompanyProcessingAction(company, processingAction);
        
        assertEquals(cpa1, cpa2);
        assertEquals(cpa1.hashCode(), cpa2.hashCode());
    }

    @Test
    void testEqualsDifferentCompany() {
        Company differentCompany = new Company();
        
        CompanyProcessingAction cpa1 = new CompanyProcessingAction(company, processingAction);
        CompanyProcessingAction cpa2 = new CompanyProcessingAction(differentCompany, processingAction);
        
        assertNotEquals(cpa1, cpa2);
    }

    @Test
    void testToString() {
        String toString = companyProcessingAction.toString();
        
        assertTrue(toString.contains("CompanyProcessingAction"));
        assertTrue(toString.contains("enabled=true"));
    }
}
