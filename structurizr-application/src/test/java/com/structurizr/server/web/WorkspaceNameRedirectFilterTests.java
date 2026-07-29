package com.structurizr.server.web;

import com.structurizr.server.domain.WorkspaceMetadata;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspaceNameRedirectFilterTests {

	private WorkspaceNameRedirectFilter filter;

	@BeforeEach
	void setUp() {
		filter = new WorkspaceNameRedirectFilter(new MockWorkspaceComponent() {
			@Override
			public WorkspaceMetadata getWorkspaceMetadataByRoutingKey(String routingKey) {
				if ("dewey".equalsIgnoreCase(routingKey)) {
					WorkspaceMetadata workspaceMetadata = new WorkspaceMetadata(2);
					workspaceMetadata.setRoutingKey("dewey");
					return workspaceMetadata;
				}

				return null;
			}
		});
	}

	@Test
	void redirectsWorkspaceRootByRoutingKey() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/workspace");
		request.setParameter("key", "dewey");
		request.setParameter("branch", "main");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new NoOpFilterChain());

		assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
		assertEquals("/workspace/2?branch=main", response.getRedirectedUrl());
	}

	@Test
	void redirectsWorkspaceSubpathByRoutingKey() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/workspace/diagrams");
		request.setParameter("key", "dewey");
		request.setParameter("version", "5");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new NoOpFilterChain());

		assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
		assertEquals("/workspace/2/diagrams?version=5", response.getRedirectedUrl());
	}

	@Test
	void removesLeadingWorkspaceRoutingKeySegmentWhenPresent() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/workspace/dewey/documentation/payments");
		request.setParameter("key", "dewey");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new NoOpFilterChain());

		assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
		assertEquals("/workspace/2/documentation/payments", response.getRedirectedUrl());
	}

	@Test
	void ignoresCanonicalNumericWorkspaceUrls() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/workspace/2/diagrams");
		request.setParameter("key", "dewey");
		MockHttpServletResponse response = new MockHttpServletResponse();
		TrackingFilterChain filterChain = new TrackingFilterChain();

		filter.doFilter(request, response, filterChain);

		assertTrue(filterChain.called);
		assertNull(response.getRedirectedUrl());
	}

	@Test
	void redirectsToRootWhenWorkspaceRoutingKeyCannotBeResolved() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setRequestURI("/workspace/diagrams");
		request.setParameter("key", "unknown");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new NoOpFilterChain());

		assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
		assertEquals("/", response.getRedirectedUrl());
	}

	private static final class NoOpFilterChain implements FilterChain {

		@Override
		public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
		}
	}

	private static final class TrackingFilterChain implements FilterChain {

		private boolean called;

		@Override
		public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
			called = true;
		}
	}

}