/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.bmuskalla.system.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class ScopedSystemPropertiesTest {

	@Test
	void onlySeesScopedValueWithinScope() {
		try (SystemPropertyScope scope = ScopedSystemProperties.scoped()) {
			scope.setProperty("scopedKey", "scopedValue");
			assertThatSystemPropertyHasValue("scopedKey", "scopedValue");
		}
		assertThatSystemPropertyHasValue("scopedKey", null);
	}

	@Test
	void otherThreadDoesNotSeeScopedValue() throws Exception {
		CountDownLatch setupLatch = new CountDownLatch(1);
		CountDownLatch assertionLatch = new CountDownLatch(1);
		Thread.currentThread().setName("test thread");
		try (SystemPropertyScope scope = ScopedSystemProperties.scoped()) {
			scope.setProperty("scopedKey", "scopedValue");
			setupLatch.countDown();
			assertThatSystemPropertyHasValue("scopedKey", "scopedValue");
			new Thread(() -> {
				try {
					setupLatch.await(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					throw new AssertionError(e);
				}
				assertThatSystemPropertyHasValue("scopedKey", null);
				assertionLatch.countDown();
			}, "outside scope").start();
		}
		assertionLatch.await(1, TimeUnit.MINUTES);

		assertThatSystemPropertyHasValue("scopedKey", null);
	}

	private void assertThatSystemPropertyHasValue(String key, String value) {
		assertThat(System.getProperty(key)).isEqualTo(value);
	}
}
