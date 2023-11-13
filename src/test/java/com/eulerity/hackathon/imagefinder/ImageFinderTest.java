package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;

public class ImageFinderTest {

	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private HttpSession session;
	@Mock
	private WebCrawler webCrawler;

	private StringWriter sw;

	@InjectMocks
	private ImageFinder imageFinder;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		imageFinder = new ImageFinder(webCrawler);
		sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Mockito.when(response.getWriter()).thenReturn(pw);
		Mockito.when(request.getServletPath()).thenReturn("/main");
		Mockito.when(request.getParameter("url")).thenReturn("http://test.com");
	}

	@Test
	public void testImageFinder() throws IOException, ServletException {
		// Set<String> mockImageUrls = new
		// HashSet<>(Arrays.asList("http://test.com/image1.jpg",
		// "http://test.com/image2.jpg"));
		// Mockito.when(webCrawler.crawl("http://test.com")).thenReturn(mockImageUrls);

		// imageFinder.doPost(request, response);

		// Assert.assertEquals(new Gson().toJson(mockImageUrls), sw.toString());
	}
}
