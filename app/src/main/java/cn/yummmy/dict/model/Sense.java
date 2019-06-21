package cn.yummmy.dict.model;

import java.util.ArrayList;
import java.util.List;

public class Sense {
    public String def;
    public List<String> examples;
    public List<String> examplesAudioSrc;

    public Sense() {
        examples = new ArrayList<>();
        examplesAudioSrc = new ArrayList<>();
    }
}
