package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {
    private final ExecutorService executorService;
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final Set<String> visitedUrls = new CopyOnWriteArraySet<>();
    private final Set<String> imageUrls = new CopyOnWriteArraySet<>();
    private String domain;
    private final long crawlDelay;
    private final int timeout;

    public WebCrawler(int threadPoolSize, long crawlDelay, int timeout) throws Exception {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.crawlDelay = crawlDelay;
        this.timeout = timeout;
    }

    public Set<String> crawl(String startUrl) {
        if (domain == null || domain.isEmpty()) {
            throw new IllegalStateException("Domain not set");
        }

        try {
            submitTask(() -> processUrl(startUrl));

            while (activeTaskCount.get() > 0) {
                try {
                    Thread.sleep(100); // Short sleep to reduce CPU usage
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Crawler interrupted", e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdownAndAwaitTermination(executorService);
        }

        return imageUrls;
    }

    public void setDomain(String startUrl) throws Exception {
        this.domain = new URL(startUrl).getHost();
    }

    private void processUrl(String url) {
        if (visitedUrls.add(url) && url.contains(domain)) {
            try {
                Thread.sleep(crawlDelay);

                Document doc = Jsoup.connect(url).get();
                addImageUrls(doc, url);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absUrl = link.absUrl("href");
                    if (!visitedUrls.contains(absUrl)) {
                        submitTask(() -> processUrl(absUrl));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addImageUrls(Document doc, String url) {
        Elements images = doc.select("img[src]");
        images.stream()
                .map(img -> normalizeUrl(img.absUrl("src")))
                .forEach(imageUrls::add);

        Elements divImgTags = doc.select("div[role=img]");
        divImgTags.stream()
                .map(div -> normalizeUrl(extractImageUrlFromDiv(div)))
                .filter(imgUrl -> !imgUrl.isEmpty())
                .forEach(imageUrls::add);
    }

    private String extractImageUrlFromDiv(Element div) {
        String style = div.attr("style");
        // Pattern to match 'background-image' CSS property
        Pattern pattern = Pattern.compile("background-image: *url\\(['\"]?(.+?)['\"]?\\)");
        Matcher matcher = pattern.matcher(style);

        if (matcher.find()) {
            return matcher.group(1); // Returns the first captured group (the URL)
        }

        return ""; // Return an empty string if no URL is found
    }

    private String normalizeUrl(String url) {
        try {
            URL aURL = new URL(url);
            String protocol = aURL.getProtocol();
            String host = aURL.getHost();
            String path = aURL.getPath();
            return new URL(protocol, host, path).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return url; // Return the original URL if there's an error
        }
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait for existing tasks to complete
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void submitTask(Runnable task) {
        activeTaskCount.incrementAndGet();
        executorService.submit(() -> {
            try {
                task.run();
            } finally {
                activeTaskCount.decrementAndGet();
            }
        });
    }
}