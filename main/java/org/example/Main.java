package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class Main {
    static ChromeDriver driver;

    public static void main(String[] args) {
        InitializeChrome(); // инициализация браузера

        String[] links = {
                "https://parketelit.ru/magazin/folder/laminat",
                "https://parketelit.ru/magazin/folder/parketnaya-doska",
                "https://parketelit.ru/magazin/folder/inzhenernaya-doska",
                "https://parketelit.ru/magazin/folder/kvarcvinilovyj-pol",
                "https://parketelit.ru/magazin/folder/parket-elka",
                "https://parketelit.ru/magazin/folder/modulnyj-parket",
                "https://parketelit.ru/magazin/folder/massivnaya-doska",
                "https://parketelit.ru/magazin/folder/probkovoe-pokrytie",
                "https://parketelit.ru/magazin/folder/lepnina",
                "https://parketelit.ru/magazin/folder/poly",
                "https://parketelit.ru/magazin/folder/klej-i-grunt",
                "https://parketelit.ru/magazin/folder/terrasnaya-doska-dpk"
        };

        String[] categories = { "Ламинат", "Паркетная доска", "Инженерная доска", "Кварцвиниловый пол", "Паркет ёлка",
                "Модульный паркет", "Массивная доска", "Пробковое покрытие", "Лепнина", "Полы", "Клей и грунт", "Террасная доска ДПК" };

        Integer[] pages = { 70, 28, 16, 52, 11, 3, 4, 2, 58, 1, 1, 6 };

        ProductXMLWriter pxw = new ProductXMLWriter();
        pxw.CreateDoc();

        for(int x = 0; x < links.length; x++) {
            driver.get(links[x]); // открытие вкладки
            String level4Name = categories[x];

            String originalWindow = driver.getWindowHandle();

            for(int i = 0; i < pages[x]; i++) {
                // Получение ссылок на все товары
                List<WebElement> items = driver.findElements(By.className("gr-product-name"));
                System.out.println("\nItem number: " + items.size() + "\n");

                for (WebElement item : items) {
                    try {
                        Thread.sleep(1500);
                        String senderPrdCode, productName, price, price2 = "", multiplicity, vendorProdNum, brand, brandCode;

                        senderPrdCode = item.findElement(By.xpath(".//a")).getAttribute("href");

                        // Открытие страницы нового товара в новой вкладке
                        driver.switchTo().newWindow(WindowType.TAB);

                        driver.get(senderPrdCode);

                        productName = driver.findElement(By.tagName("h1")).getText();
                        price = findByClass("price-current").findElement(By.xpath(".//strong")).getText();

                        try {
                            multiplicity = findByClass("unit-val").getText();
                        } catch (Exception e) {
                            multiplicity = "шт";
                        }

                        vendorProdNum = findByClass("shop2-product-article").getText();

                        if(driver.findElements(By.className("gr-vendor-block")).size() != 0)
                            brand = findByClass("gr-vendor-block").findElement(By.xpath(".//a")).getText();
                        else
                            brand = "";

                        String bcXpath = "//div[@class='option-title' and contains(text(),'Код производителя')]";
                        if(driver.findElements(By.xpath(bcXpath)).size() != 0) {
                            WebElement optionBrandCode = driver.findElement(By.xpath("//div[@class='option-title' and contains(text(),'Код производителя')]"));
                            brandCode = optionBrandCode.findElement(By.xpath("./following-sibling::div[@class='option-body']")).getText();
                        } else brandCode = "";


                        if(driver.findElements(By.className("price-old")).size() != 0)
                        {
                            String str = findByClass("price-old").getText();
                            price2 = str.substring(0, str.length() - 2);
                        }

                        List <WebElement> fn = driver.findElements(By.className("param-title"));
                        List <WebElement> fv = driver.findElements(By.className("param-body"));

                        int s = fn.size();

                        String[] featureName = new String[s];
                        String[] featureValue = new String[s];

                        for(int z = 0; z < s; z++) {
                            featureName[z] = fn.get(z).getText();
                            featureValue[z] = fv.get(z).getText();
                        }

                        List <WebElement> imgs = driver.findElements(By.className("gr-image-zoom"));
                        String[] imgLinks = new String[imgs.size()];

                        for(int y = 0; y < imgLinks.length; y++) {
                            imgLinks[y] = imgs.get(y).findElement(By.xpath(".//img")).getAttribute("src");
                        }

                        pxwSet(pxw, senderPrdCode, productName, price, price2, multiplicity, vendorProdNum, brand, brandCode, level4Name, featureName, featureValue, imgLinks);

                        driver.close();
                        driver.switchTo().window(originalWindow);
                    } catch (Exception e) {

                        System.err.println("Ошибка: " + e.getMessage());
                        driver.close();
                        driver.switchTo().window(originalWindow);
                    }
                }

                if(driver.findElements(By.className("page-next")).size() != 0)
                    findByClass("page-next").click();
            }
        }

        driver.quit();
        PowerOff();
    }

    static void PowerOff() {
        try {
            Runtime.getRuntime().exec("shutdown -s -t 0");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static void pxwSet(ProductXMLWriter pxw, String
            spc, String pn, String p, String p2, String m, String vpn, String b, String bc, String l, String[] fn, String[] fv, String[] i) {

        vpn = vpn.substring(9);

        pxw.setSenderPrdCode(spc);
        pxw.setProductName(pn);
        pxw.setPrice(p);
        pxw.setPrice2(p2);
        pxw.setMultiplicity(m);
        pxw.setVendorProdNum(vpn);
        pxw.setBrand(b);
        pxw.setBrandCode(bc);
        pxw.setFeatureName(fn);
        pxw.setFeatureValue(fv);
        pxw.setImgLinks(i);
        pxw.setLevel4Name(l);
    }

    static void InitializeChrome() {
        // Создание объекта ChromeOptions и указание пути к профилю
        String chromeProfilePath = "C:\\Users\\User\\AppData\\Local\\Google\\Chrome\\User Data";
        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir=" + chromeProfilePath);

        // Инициализация браузера
        driver = new ChromeDriver(options);
    }

    static WebElement findByClass(String className) {
        return driver.findElement(By.className(className));
    }
}

class ProductXMLWriter {
    private String senderPrdCode, productName, price, price2, multiplicity, levelName, vendorProdNum, brand, brandCode;
    private String[] featureName, featureValue, imgLinks;

    Document doc;
    Element rootElement;

    private void XMLWriter() {

        String[] parameters = { "SenderPrdCode", "ProductName", "Price", "Price2", "Multiplicity", "VendorProdNum", "Brand", "BrandCode" };

        String[] values = {senderPrdCode, productName, price, price2, multiplicity,
                vendorProdNum, brand, brandCode };

        try {
            Element docDetail = doc.createElement("DocDetail");
            rootElement.appendChild(docDetail);


            for(int i = 0; i < parameters.length; i++) {
                if(values[i] != null) {
                    Element prm = doc.createElement(parameters[i]);
                    prm.appendChild(doc.createTextNode(values[i]));
                    docDetail.appendChild(prm);
                }
            }

            Element rsc = doc.createElement("RsCatalog");
            docDetail.appendChild(rsc);

            Element level4Name = doc.createElement("Level4Name");
            level4Name.appendChild(doc.createTextNode(levelName));
            rsc.appendChild(level4Name);

            for (int i = 0; i < featureName.length; i++) {
                Element featureETIMDetails = doc.createElement("FeatureETIMDetails");
                docDetail.appendChild(featureETIMDetails);

                // Добавляем FeatureName
                Element featureNameElement = doc.createElement("FeatureName");
                featureNameElement.appendChild(doc.createTextNode(featureName[i]));
                featureETIMDetails.appendChild(featureNameElement);

                // Добавляем FeatureValue
                Element featureValueElement = doc.createElement("FeatureValue");
                featureValueElement.appendChild(doc.createTextNode(featureValue[i]));
                featureETIMDetails.appendChild(featureValueElement);
            }

            for (String imgLink : imgLinks) {
                Element image = doc.createElement("Image");
                docDetail.appendChild(image);

                // Добавляем Value
                Element value = doc.createElement("Value");
                value.appendChild(doc.createTextNode(imgLink));
                image.appendChild(value);
            }

            // Сохраняем документ в файл
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // Включаем форматирование
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // Задаем количество пробелов для отступов

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("output.xml"));
            transformer.transform(source, result);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void CreateDoc() {
        try {
            // Создаем фабрику для построения документа
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();

            // Создаем элементы документа
            rootElement = doc.createElement("Document");
            doc.appendChild(rootElement);

            Element docType = doc.createElement("DocType");
            docType.appendChild(doc.createTextNode("PRODAT"));
            rootElement.appendChild(docType);
            System.out.println("Документ создан");
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSenderPrdCode(String senderPrdCode) {
        this.senderPrdCode = senderPrdCode;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setPrice2(String price2) {
        this.price2 = price2;
    }

    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }

    public void setLevel4Name(String level4) {
        this.levelName = level4;
    }

    public void setVendorProdNum(String vendorProdNum) {
        this.vendorProdNum = vendorProdNum;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setBrandCode(String brandCode) {
        this.brandCode = brandCode;
    }

    public void setFeatureName(String[] featureName) {
        this.featureName = featureName;
    }

    public void setFeatureValue(String[] featureValue) {
        this.featureValue = featureValue;
    }

    public void setImgLinks(String[] imgLinks) {
        this.imgLinks = imgLinks;

        XMLWriter();
    }
}