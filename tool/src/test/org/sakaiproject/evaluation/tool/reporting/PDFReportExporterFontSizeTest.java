/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.reporting;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDFReportExporterFontSizeTest {

	// Accuracy of floating point comparisons.
	private static final double DELTA = 1e-6;
	private PDFReportExporter exporter;

	@Before
	public void setUp() {
		exporter = new PDFReportExporter();

	}

	@Test
	public void testSimpleFontSize() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size: large; color: blue'>Hello</div>"), DELTA);
	}

	@Test
	public void testJustDefaultFontSize() {
		assertEquals(10.0f, exporter.calculateFontSize("<div>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNumeric() {
		// If we have a font style but can't understand it we have a slightly larger font.
		assertEquals(12.0f, exporter.calculateFontSize("<div style='font-size: 12pt;'>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNoSemiColon() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size: large'>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNoSpace() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size:large;'>Hello</div>"), DELTA);
	}

}
