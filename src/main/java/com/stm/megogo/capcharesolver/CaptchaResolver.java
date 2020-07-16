/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stm.megogo.capcharesolver;

import org.jsoup.nodes.Document;


public interface CaptchaResolver {
    String getAnswerForCaptchaDoc(Document doc);
    boolean isCaptcha(Document doc);
}
