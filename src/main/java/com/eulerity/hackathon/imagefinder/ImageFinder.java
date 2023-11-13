package com.eulerity.hackathon.imagefinder;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(name = "ImageFinder", urlPatterns = { "/main" })
public class ImageFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int THREAD_POOL_SIZE = 10;
	private static final long CRAWL_DELAY = 1000; // in milliseconds
	private static final int TIMEOUT = 10; // in seconds

	protected static final Gson GSON = new GsonBuilder().create();

	private WebCrawler webCrawler;

	public ImageFinder() throws Exception {
		this.webCrawler = new WebCrawler(THREAD_POOL_SIZE, CRAWL_DELAY, TIMEOUT);
	}

	// For testing purposes: allows injection of a mock WebCrawler
	public ImageFinder(WebCrawler webCrawler) {
		this.webCrawler = webCrawler;
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getParameter("url");
		String path = req.getServletPath();
		System.out.println("Got request of:" + path + " with query param:" + url);

		resp.setContentType("text/json");
		try {
			webCrawler.setDomain(url);
			Set<String> imageUrls = webCrawler.crawl(url);
			System.out.println("Found " + imageUrls.size() + " images");
			imageUrls.forEach(System.out::println);
			resp.getWriter().print(GSON.toJson(imageUrls));
		} catch (Exception e) {
			e.printStackTrace();
			resp.getWriter().print(GSON.toJson(Collections.emptyList()));
		}
	}
}
