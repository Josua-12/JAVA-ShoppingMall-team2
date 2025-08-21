package com.shopping.test.user;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.shopping.model.Role;

public class testRoleEnumValues {

	@Test
	void testRoleEnumValues() {
	    assertNotNull(Role.USER);
	    assertNotNull(Role.ADMIN);
	    // 필요한 경우 values() 배열 크기 확인 및 값 포함 여부 검사
	    assertTrue(Arrays.asList(Role.values()).contains(Role.USER));
	}

}

