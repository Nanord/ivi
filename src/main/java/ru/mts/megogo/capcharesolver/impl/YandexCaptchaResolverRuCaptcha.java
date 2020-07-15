/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mts.megogo.capcharesolver.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.mts.megogo.capcharesolver.CaptchaResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class YandexCaptchaResolverRuCaptcha implements CaptchaResolver {
   
    @Override
    public synchronized String getAnswerForCaptchaDoc(Document doc) {
       
       Element formElement = getFormElement(doc);
       
       String key = getValueForName("key", formElement);
       String retPath = getValueForName("retpath", formElement);
       
       
       Element captchaImageElement = formElement.getElementsByTag("img")
                                                    .first();
       
       if(captchaImageElement != null) {
           String captchaValue = getValueFromImage(captchaImageElement);
           try {
            return "https://market.yandex.ru/checkcaptcha?key=" + URLEncoder.encode(key,"UTF-8")
                                                       + "&retpath=" + URLEncoder.encode(retPath,"UTF-8") 
                                                        + "&rep=" + URLEncoder.encode(captchaValue,"UTF-8");
           } catch (UnsupportedEncodingException ex) {
               log.error("Error encoding captcha resolve url");
           }
       }
       
       return "";
    }
    
    private Element getFormElement(Document doc) {
       List<Element> capchaInputForms =  doc.getElementsByTag("form");
       
       if(capchaInputForms.size() == 1) {
           return capchaInputForms.get(0);
       }
       return null;
    }
    
    private String getValueForName(String name, Element formElement){
        Element elementWithValue = formElement.getElementsByAttributeValue("name",name)
                .stream()
                .findAny()
                .orElse(null);
        if(Objects.nonNull(elementWithValue)) {
            return elementWithValue.attr("value");
        }
        return StringUtils.EMPTY;
    }
    
    private String getValueFromImage(Element imageElement) {
        String imageUrl = imageElement.attr("src");
        String imgFilename = "";
        try {
            imgFilename = loadImage(imageUrl);
        } catch (IOException ex) {
            log.error("Error loading image {}: {}", imageUrl, ex.getLocalizedMessage());
        }
        
        if(imgFilename.isEmpty()) {
            return StringUtils.EMPTY;
        }
        Scanner in = new Scanner(System.in);
        log.info("ENTER CPATCHA: ");
        String captchaValue = in.nextLine();
        log.info("Captcha answer is {}", captchaValue);

        new File(imgFilename).delete();

        return captchaValue;
    }
    
    private String loadImage(String imageUrl) throws IOException {
        
        String params = imageUrl.substring(imageUrl.indexOf("?") + 1);
        
        params = params.substring(0,params.indexOf(","));
        
        log.info("Params are {}",params);
        
        
        byte[] decoded = Base64.getUrlDecoder().decode(params);
        log.info("Decoded length - {}", decoded.length);
        String connectingUrl =new String( decoded);
        
        log.info("Loading image {}", connectingUrl);
        
        Connection.Response resp = Jsoup.connect(connectingUrl).ignoreContentType(true).execute();
        
        String filename = "image" + UUID.randomUUID().hashCode() + ".jpg";
        
        FileOutputStream fileStream = new FileOutputStream(new File(filename));
        
        fileStream.write(resp.bodyAsBytes());
        
        return filename;
        
    }
    
    @Override
    public boolean isCaptcha(Document doc) {
        if(doc == null) {
            return false;
        }
        if(StringUtils.contains(doc.location(), "showcaptcha")) {
            return true;
        }
        Element formElement = getFormElement(doc);

        if(Objects.nonNull(formElement)) {
            return !getValueForName(formElement).isEmpty();
        }

        return false;
    }

    private String getValueForName(Element formElement){
        Element elementWithValue = formElement.getElementsByAttributeValue("name", "key")
                .stream()
                .findAny()
                .orElse(null);

        if(Objects.nonNull(elementWithValue)) {
            return elementWithValue.attr("value");
        }

        return StringUtils.EMPTY;
    }
}
