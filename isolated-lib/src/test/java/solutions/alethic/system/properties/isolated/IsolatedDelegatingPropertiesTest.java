/*******************************************************************************
 * Copyright 2019 Benjamin Muskalla
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package solutions.alethic.system.properties.isolated;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class IsolatedDelegatingPropertiesTest {

	@Test
	void propertyNamesIncludesScopedValues() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "newKey");

		assertThat(Collections.list(properties.propertyNames())).asList().containsExactlyInAnyOrder("key", "newKey");
	}

	@Test
	void stringPropertyNamesIncludesScopedValues() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "newKey");

		assertThat(properties.stringPropertyNames()).containsExactlyInAnyOrder("key", "newKey");
	}

	@Test
	void keysIncludesScopedValues() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "newKey");

		assertThat(properties.keySet()).containsExactlyInAnyOrder("key", "newKey");
		assertThat(Collections.list(properties.keys())).asList().containsExactlyInAnyOrder("key", "newKey");
	}

	@Test
	void canRemoveScopedKey() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		properties.remove("scopedKey");

		assertThat(properties.keySet()).containsExactlyInAnyOrder("key");
	}

	@Test
	void containsSeesScopedEntries() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		boolean contains = properties.contains("y");
		boolean containsKey = properties.containsKey("scopedKey");

		assertThat(contains).isTrue();
		assertThat(containsKey).isTrue();
	}

	@Test
	void storeContainsScopedKeys() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		properties.store(stream, "comments");

		String s = stream.toString();
		assertThat(stream.toString()).startsWith("#comment");
		assertThat(stream.toString()).contains(String.format("key=x%n"));
		assertThat(stream.toString()).contains(String.format("scopedKey=y%n"));
	}

	@Test
	void valuesContainsScopedValues() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		Collection<Object> values = properties.values();

		assertThat(values).containsExactlyInAnyOrder("x", "y");
	}

	@Test
	void valuesContainsOnlyScopedValuesWithOverride() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "key");

		Collection<Object> values = properties.values();

		assertThat(values).containsExactlyInAnyOrder("y");
	}

	@Test
	void storeWithoutBaselineContainsScopedKeys() throws Exception {
		IsolatedDelegatingProperties properties = new IsolatedDelegatingProperties(new Properties());
		properties.setProperty("scopedKey", "x");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		properties.store(stream, "comments");

		assertThat(stream.toString()).startsWith("#comment");
		assertThat(stream.toString()).endsWith(String.format("%nscopedKey=x%n"));
	}

	@Test
	void setPropertyStoresValueOnlyScoped() throws Exception {
		Properties baseline = new Properties();
		baseline.put("key", "x");
		IsolatedDelegatingProperties properties = new IsolatedDelegatingProperties(baseline);
		properties.setProperty("key", "value");

		assertThat(properties.getProperty("key")).isEqualTo("value");
		assertThat(baseline.getProperty("key")).isEqualTo("x");
	}

	@Test
	void setPropertyReturnsBaselineValueIfNoScopedValuePresent() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		Object previousValue = properties.setProperty("key", "value");

		assertThat(properties.getProperty("key")).isEqualTo("value");
		assertThat(previousValue).isEqualTo("x");
	}

	@Test
	void getPropertyFallsbackToBaseline() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		assertThat(properties.getProperty("key")).isEqualTo("x");
	}

	@Test
	void getPropertyWithDefaultWithBaselineKey() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		String value = properties.getProperty("key", "notused");

		assertThat(value).isEqualTo("x");
	}

	@Test
	void getPropertyWithDefaultWithScopedKey() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		String value = properties.getProperty("scopedKey", "notused");

		assertThat(value).isEqualTo("y");
	}

	@Test
	void getPropertyWithDefaultWithMissingKey() throws Exception {
		IsolatedDelegatingProperties properties = propertiesWithRegularAndScopedKey("key", "scopedKey");

		String value = properties.getProperty("xxx", "default");

		assertThat(value).isEqualTo("default");
	}

	@ParameterizedTest(name = "{1}")
	@ArgumentsSource(IsolatedPropertiesMethodProvider.class)
	void testWhetherMethodIsImplementedByUs(Method method, String testDisplayName) {
		List<Method> methodImplementations = Arrays.stream(IsolatedDelegatingProperties.class.getDeclaredMethods())
				.filter(m -> m.getName().equals(method.getName()))
				.filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
				.collect(Collectors.toList());
		assertThat(methodImplementations).as("missing delegate: " + testDisplayName).hasSize(1);
	}

	private IsolatedDelegatingProperties propertiesWithRegularAndScopedKey(String regularKey, String scopedKey) {
		Properties baseline = new Properties();
		baseline.put(regularKey, "x");
		IsolatedDelegatingProperties properties = new IsolatedDelegatingProperties(baseline);
		properties.setProperty(scopedKey, "y");
		return properties;
	}
}
