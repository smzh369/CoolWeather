package com.coolweather.android.Binding;


/**
 * Created by 令子 on 2017/2/4.
 */

public class ForcastInfo {

    private String dateText;

    private String infoText;

    private String maxText;

    private String minText;

    public ForcastInfo(String dateText,String infoText,String maxText,String minText){
        this.dateText = dateText;
        this.infoText = infoText;
        this.maxText = maxText;
        this.minText = minText;
    }

    public String getDateText() {
        return dateText;
    }

    public void setDateText(String dateText) {
        this.dateText = dateText;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public String getMaxText() {
        return maxText;
    }

    public void setMaxText(String maxText) {
        this.maxText = maxText;
    }

    public String getMinText() {
        return minText;
    }

    public void setMinText(String minText) {
        this.minText = minText;
    }
}
