/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mts.megogo.capcharesolver.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import ru.mts.megogo.capcharesolver.CaptchaResolver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

@Component
@Slf4j
public class YandexCaptchaResolverRuCaptcha implements CaptchaResolver {
   
    @Override
    public synchronized String getAnswerForCaptchaDoc(Document doc) {
       
       Element formElement = getFormElement(doc);
       
       String key = getValueForName("key", formElement);
       String retPath = getValueForName("retpath", formElement);
       
       
       Element captchaImageElement = formElement.getElementsByTag("img").first();
       
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
       
       return StringUtils.EMPTY;
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
        printImageUrl(imageUrl);
        Scanner in = new Scanner(System.in);
        log.info("ENTER CPATCHA: ");
        String captchaValue = in.nextLine();
        log.info("Captcha answer is {}", captchaValue);
        return captchaValue;
    }
    
    private void printImageUrl(String imageUrl) {
        String params = imageUrl.substring(imageUrl.indexOf("?") + 1);
        params = params.substring(0,params.indexOf(","));
        byte[] decoded = Base64.getUrlDecoder().decode(params);
        String connectingUrl = new String(decoded);
        
        log.info("Captcha image URL {}", connectingUrl);
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
