package cn.yummmy.dict.model;

import java.util.ArrayList;
import java.util.List;

public class EnglishWord {

    public String hwd;
    public String hyphenation;
    public String pronCodes;
    public String pronunciationSrc;
    public List<Sense> senses;

    public EnglishWord() {
        senses = new ArrayList<>();
    }
}
