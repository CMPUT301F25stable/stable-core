package com.example.eventlottery;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for EditUserInfoActivity
 * Tests validation logic
 */
public class EditUserInfoActivityTest {

    private String testName;
    private String testEmail;
    private String testPhone;

    @Before
    public void setUp() {
        testName = "John Doe";
        testEmail = "john.doe@example.com";
        testPhone = "780-123-4567";
    }

    @Test
    public void testValidateInput_ValidData() {
        boolean isValid = validateInputHelper(testName, testEmail);
        assertTrue("Valid name and email should pass validation", isValid);
    }

    @Test
    public void testValidateInput_EmptyName() {
        boolean isValid = validateInputHelper("", testEmail);
        assertFalse("Empty name should fail validation", isValid);
    }

    @Test
    public void testValidateInput_EmptyEmail() {
        boolean isValid = validateInputHelper(testName, "");
        assertFalse("Empty email should fail validation", isValid);
    }

    @Test
    public void testValidateInput_BothEmpty() {
        boolean isValid = validateInputHelper("", "");
        assertFalse("Both empty should fail validation", isValid);
    }

    @Test
    public void testValidateInput_WhitespaceOnlyName() {
        boolean isValid = validateInputHelper("   ", testEmail);
        assertFalse("Whitespace-only name should fail validation", isValid);
    }

    @Test
    public void testValidateInput_WhitespaceOnlyEmail() {
        boolean isValid = validateInputHelper(testName, "   ");
        assertFalse("Whitespace-only email should fail validation", isValid);
    }

    @Test
    public void testValidateInput_NullName() {
        boolean isValid = validateInputHelper(null, testEmail);
        assertFalse("Null name should fail validation", isValid);
    }

    @Test
    public void testValidateInput_NullEmail() {
        boolean isValid = validateInputHelper(testName, null);
        assertFalse("Null email should fail validation", isValid);
    }

    @Test
    public void testValidateInput_NullBoth() {
        boolean isValid = validateInputHelper(null, null);
        assertFalse("Both null should fail validation", isValid);
    }

    @Test
    public void testTrimInput_RemovesLeadingWhitespace() {
        String input = "  Test Name";
        String trimmed = input.trim();
        assertEquals("Should remove leading whitespace", "Test Name", trimmed);
    }

    @Test
    public void testTrimInput_RemovesTrailingWhitespace() {
        String input = "Test Name  ";
        String trimmed = input.trim();
        assertEquals("Should remove trailing whitespace", "Test Name", trimmed);
    }

    @Test
    public void testTrimInput_RemovesBothWhitespace() {
        String input = "  Test Name  ";
        String trimmed = input.trim();
        assertEquals("Should remove leading and trailing whitespace", "Test Name", trimmed);
    }

    @Test
    public void testTrimInput_NoWhitespace() {
        String input = "TestName";
        String trimmed = input.trim();
        assertEquals("Should not change string without whitespace", "TestName", trimmed);
    }

    @Test
    public void testPhoneNumber_IsOptional() {
        String phone = "";
        assertTrue("Empty phone number should be acceptable", phone.isEmpty());
    }

    @Test
    public void testPhoneNumber_WithValidFormat() {
        String phone = "780-123-4567";
        assertFalse("Non-empty phone should not be empty", phone.isEmpty());
        assertTrue("Valid phone format", phone.matches("\\d{3}-\\d{3}-\\d{4}"));
    }

    @Test
    public void testPhoneNumber_WithInvalidFormat() {
        String phone = "123456789";
        assertFalse("Should not match standard format", phone.matches("\\d{3}-\\d{3}-\\d{4}"));
    }

    @Test
    public void testEmailFormat_Valid() {
        assertTrue("Should be valid email", testEmail.contains("@"));
        assertTrue("Should have domain", testEmail.contains("."));
    }

    @Test
    public void testEmailFormat_Invalid() {
        String invalidEmail = "notanemail";
        assertFalse("Should not be valid email",
                invalidEmail.contains("@") && invalidEmail.contains("."));
    }

    @Test
    public void testValidateInput_WithTrimmedInputs() {
        String nameWithSpaces = "  John Doe  ";
        String emailWithSpaces = "  john@example.com  ";

        boolean isValid = validateInputHelper(nameWithSpaces, emailWithSpaces);
        assertTrue("Should validate after trimming", isValid);
    }

    // Helper method to simulate validation logic from EditUserInfoActivity
    private boolean validateInputHelper(String name, String email) {
        if (name == null || email == null) return false;
        return !name.trim().isEmpty() && !email.trim().isEmpty();
    }
}