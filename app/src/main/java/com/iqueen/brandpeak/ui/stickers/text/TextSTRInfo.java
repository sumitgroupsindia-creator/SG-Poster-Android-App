package com.sgdigitalposter.app.ui.stickers.text;

import java.io.Serializable;

public class TextSTRInfo implements Serializable {

    private String font_family;
    private String text;
    private String text_id;
    private String txt_color;
    private String txt_height;
    private String txt_order;
    private String txt_rotation;
    private String txt_width;
    private String txt_x_pos;
    private String txt_y_pos;
    private String name;

    private String justification;

    private String uppercase = "";

    private int lineSize = 5;
    private String lineColor = "";
    private int lineOpacity = 100;

    public int sdDistance = 0;
    public String sdColor = "";
    public int sdAngle = 0;
    public int sdOpacity = 100;
    public int sdBlur = 0;

    public TextSTRInfo() {
    }

    public int getSdDistance() {
        return sdDistance;
    }

    public void setSdDistance(int sdDistance) {
        this.sdDistance = sdDistance;
    }

    public String getSdColor() {
        return sdColor;
    }

    public void setSdColor(String sdColor) {
        this.sdColor = sdColor;
    }

    public int getSdAngle() {
        return sdAngle;
    }

    public void setSdAngle(int sdAngle) {
        this.sdAngle = sdAngle;
    }

    public int getSdOpacity() {
        return sdOpacity;
    }

    public void setSdOpacity(int sdOpacity) {
        this.sdOpacity = sdOpacity;
    }

    public int getSdBlur() {
        return sdBlur;
    }

    public void setSdBlur(int sdBlur) {
        this.sdBlur = sdBlur;
    }

    public String getUppercase() {

        return uppercase;
    }

    public void setUppercase(String uppercase) {
        this.uppercase = uppercase;
    }

    public int describeContents() {
        return 0;
    }

    public int getLineSize() {
        return lineSize;
    }

    public void setLineSize(int lineSize) {
        this.lineSize = lineSize;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineOpacity() {
        return lineOpacity;
    }

    public void setLineOpacity(int lineOpacity) {
        this.lineOpacity = lineOpacity;
    }

    public String getTxt_height() {
        return this.txt_height;
    }

    public void setTxt_height(String str) {
        this.txt_height = str;
    }

    public String getTxt_width() {
        return this.txt_width;
    }

    public void setTxt_width(String str) {
        this.txt_width = str;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String str) {
        this.text = str;
    }

    public String getTxt_x_pos() {
        return this.txt_x_pos;
    }

    public void setTxt_x_pos(String str) {
        this.txt_x_pos = str;
    }

    public String getFont_family() {
        return this.font_family;
    }

    public void setFont_family(String str) {
        this.font_family = str;
    }

    public String getTxt_y_pos() {
        return this.txt_y_pos;
    }

    public void setTxt_y_pos(String str) {
        this.txt_y_pos = str;
    }

    public String getTxt_order() {
        return this.txt_order;
    }

    public void setTxt_order(String str) {
        this.txt_order = str;
    }

    public String getText_id() {
        return this.text_id;
    }

    public void setText_id(String str) {
        this.text_id = str;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getTxt_rotation() {
        return this.txt_rotation;
    }

    public void setTxt_rotation(String str) {
        this.txt_rotation = str;
    }

    public String getTxt_color() {
        return this.txt_color;
    }

    public void setTxt_color(String str) {
        this.txt_color = str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "TextSTRInfo{" +
                "font_family='" + font_family + '\'' +
                ", text='" + text + '\'' +
                ", text_id='" + text_id + '\'' +
                ", txt_color='" + txt_color + '\'' +
                ", txt_height='" + txt_height + '\'' +
                ", txt_order='" + txt_order + '\'' +
                ", txt_rotation='" + txt_rotation + '\'' +
                ", txt_width='" + txt_width + '\'' +
                ", txt_x_pos='" + txt_x_pos + '\'' +
                ", txt_y_pos='" + txt_y_pos + '\'' +
                ", name='" + name + '\'' +
                ", justification='" + justification + '\'' +
                ", uppercase='" + uppercase + '\'' +
                ", lineSize=" + lineSize +
                ", lineColor='" + lineColor + '\'' +
                ", lineOpacity=" + lineOpacity +
                ", sdDistance=" + sdDistance +
                ", sdColor='" + sdColor + '\'' +
                ", sdAngle=" + sdAngle +
                ", sdOpacity=" + sdOpacity +
                ", sdBlur=" + sdBlur +
                '}';
    }
}
