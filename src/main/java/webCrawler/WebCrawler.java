package webCrawler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import threadUtil.ThreadUtil;

public class WebCrawler {
    private final static String ms_url = "https://www.ilan.gov.tr/ilan/kategori/9/ihale-duyurulari?txv=9&currentPage=%d";
    private final static int pageCount = 20;
    private final List<String> m_auctionLinks;
    private int m_counter;

    private WebDriver m_driver;

    private final ChromeOptions m_options;
    private BufferedWriter m_writer;

    public WebCrawler()
    {
        System.setProperty("webdriver.chrome.driver", "files\\chromedriver.exe");

        m_options = new ChromeOptions();
        m_options.addArguments("-incognito");
        m_options.addArguments("--disable-popup-blocking");

        m_auctionLinks = new ArrayList<>();

        File file = new File("files\\auctions_info.txt");


        try {
            m_writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void run()
    {
        //20 tane sayfadan her bir ilanın link'i alınıyor ve verdiğimiz metot ile listeye ekleniyor
        IntStream.rangeClosed(1, pageCount)
                .mapToObj(page -> String.format(ms_url, page))
                .forEach(this::fillAuctionLinks);

        m_auctionLinks.forEach(this::getInformationFromAuctionLinkAndSave);

        try {
            m_writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void fillAuctionLinks(String url)
    {

        ThreadUtil.sleep(1000);
        m_driver = new ChromeDriver(m_options);

        m_driver.get(url);

        var elements = m_driver.findElements(By.xpath("//div[@class='col ng-tns-c146-3']//igt-ad-single-list/ng-component/a"));

        for (var element: elements) {
            ThreadUtil.sleep(250);
            m_auctionLinks.add(element.getAttribute("href"));
        }

        m_driver.quit();
    }

    private void getInformationFromAuctionLinkAndSave(String url)
    {
        ThreadUtil.sleep(500);
        m_driver = new ChromeDriver(m_options);
        m_driver.get(url);

        var auctionRegNumber = getAuctionRegNumber(m_driver);
        var auctionType = getAuctionType(m_driver);
        var auctionPlace = String.format("%s/%s", getCity(m_driver).toUpperCase(), getTown(m_driver));
        var qualityAndTypeAndQuantityInfo = getQualityAndTypeAndQuantityInfo(m_driver);

        saveAuctionToFile(auctionRegNumber, auctionType, auctionPlace, qualityAndTypeAndQuantityInfo);

        m_driver.quit();
    }




    private String getAuctionRegNumber(WebDriver driver)
    {
        String str;

        try {
            str = driver.findElement(By.xpath("//*[@id=\"print-section\"]/div[2]/div[2]/div/div[1]/ul/li/div[text()='İhale Kayıt No']/following::div"))
                    .getText();
        }
        catch (Exception ignore) {
            str = "İhale Kayıt Numarası Bulunamadı";
        }

        return str;

    }

    private String getAuctionType(WebDriver driver)
    {
        String str;

        try {
            str = driver.findElement(By.xpath("//*[@id=\"print-section\"]/div[2]/div[2]/div/div[1]/ul/li/div[text()='İhale Türü']/following::div"))
                    .getText();
        }
        catch (Exception ignore) {
            str = "İhale Türü Bulanamadı";
        }

        return str;
    }

    private String getCity(WebDriver driver)
    {
        String str;

        try {
            str = driver.findElement(By.xpath("//*[@id=\"print-section\"]/div[2]/div[2]/div/div[1]/ul/li/div[text()='Şehir']/following::div"))
                    .getText();
        }
        catch (Exception ex) {
            str = "Şehir bilgisi bulunamadı.";
        }

        return str;
    }

    private String getTown(WebDriver driver)
    {
        String str;

        try {
            str = driver.findElement(By.xpath("//*[@id=\"print-section\"]/div[2]/div[2]/div/div[1]/ul/li/div[text()='İlçe']/following::div"))
                    .getText();
        }
        catch (Exception ex) {
            str = "İlçe bilgisi bulunamadı.";
        }

        return str;
    }

    private String getQualityAndTypeAndQuantityInfo(WebDriver driver)
    {
        String str;
        String unusedStr = "Ayrıntılı bilgiye EKAP’ta";
        int endPos;

        try {
            str = driver.findElement(By.xpath("//*[@id=\"print-section\"]/div[2]/div[1]/div/div/div/tabset/div/tab/div/table[3]/tbody/tr[2]/td[3]")).getAttribute("innerText");

            endPos = str.length();

            if (str.contains(unusedStr))
                endPos = str.indexOf(unusedStr);

        }

        catch (Exception ignore) {
            str = "İhalenin Niteliği, Türü ve Miktarı Bilgisi Bulunmadı.";
            endPos = str.length();
        }

        return str.substring(0, endPos);
    }

    private void saveAuctionToFile(String auctionRegNumber, String auctionType, String auctionPlace, String qualityAndTypeAndQuantityInfo)
    {

        try {
            m_writer.write(String.format("%d Nolu İhale : \n", ++m_counter));
            m_writer.write(String.format("İhale Kayıt No : %s\n", auctionRegNumber));
            m_writer.write(String.format("Nitelik, Tür ve Miktarı : %s\n", qualityAndTypeAndQuantityInfo));
            m_writer.write(String.format("İşin Yapılacağı Yer : %s\n", auctionPlace));
            m_writer.write(String.format("İhale Türü : %s\n", auctionType));
            m_writer.write("****************************************************\n");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
