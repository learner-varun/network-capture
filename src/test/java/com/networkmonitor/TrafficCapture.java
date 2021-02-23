package com.networkmonitor;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.*;

public class TrafficCapture {
    private static WebDriver driver;
    private static BrowserMobProxyServer proxyServer;

    @BeforeTest
    public void setupEnviornment()
    {
        WebDriverManager.chromedriver().version("2.4").setup();
        proxyServer = new BrowserMobProxyServer();
        proxyServer.start();
        proxyServer.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT,
                CaptureType.RESPONSE_CONTENT);
        proxyServer.newHar();
        final Proxy proxyConfig = new Proxy().
                setHttpProxy("localhost:"+ proxyServer.getPort()).
                setSslProxy("localhost:"+ proxyServer.getPort());
        ChromeOptions options = new ChromeOptions();
        options.setProxy(proxyConfig);
        options.setAcceptInsecureCerts(true);
        driver = new ChromeDriver(options);
    }
    @Test
    public void testSample() {
        driver.get("https://m.facebook.com/");
        final Har httpMessages = proxyServer.getHar();
        for (HarEntry httpMessage : httpMessages.getLog().getEntries()) {
            if (httpMessage.getRequest().getUrl().contains("m.facebook.com")) {
                System.out.println("Request:");
                System.out.println(httpMessage.getRequest().getUrl());
                System.out.println("Response:");
                System.out.println(httpMessage.getResponse().getContent().getText());
                System.out.println("Status Code:");
                System.out.println(httpMessage.getResponse().getStatus());
            }
        }
    }
        @AfterTest
       public void completeExecution()
        {
            driver.quit();
            proxyServer.abort();
        }
}
