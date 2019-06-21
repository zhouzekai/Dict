package cn.yummmy.dict.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.yummmy.dict.model.EnglishWord;
import cn.yummmy.dict.model.Sense;

public class Scraper {

    public static EnglishWord scrape(String url) {
        EnglishWord englishWord = new EnglishWord();
        try {
            Document doc = Jsoup.connect(url).get();
            Element entry = doc.getElementsByClass("ldoceEntry Entry").get(0);
            Element head = entry.getElementsByClass("frequent Head").get(0);
            Elements senses = entry.getElementsByClass("Sense");

            // get head information, what if the list is empty?
            // out of bound exception will be threw
            String hwd = head.getElementsByClass("HWD").get(0).text();
            String hyphenation = head.getElementsByClass("HYPHENATION").get(0).text();
            String pronCodes = head.getElementsByClass("PronCodes").get(0)
                                    .getElementsByClass("PRON").get(0)
                                    .text();
            String pronAudioSrc = head.getElementsByClass("speaker brefile fa fa-volume-up hideOnAmp").get(0).attr("data-src-mp3");

            englishWord.hwd = hwd;
            englishWord.hyphenation = hyphenation;
            englishWord.pronCodes = pronCodes;
            englishWord.pronunciationSrc = pronAudioSrc;

            // get DEF and EXAMPLE
            for (Element sense : senses) {
                Sense wordSense = new Sense();
                String def = sense.getElementsByClass("DEF").get(0).text();
                wordSense.def = def;

                Elements examples = sense.getElementsByClass("EXAMPLE");
                for (Element example : examples) {
                    String sentence = example.text();
                    String audioSrc = example.getElementsByClass("speaker exafile fa fa-volume-up hideOnAmp").get(0).attr("data-src-mp3");
                    wordSense.examples.add(sentence);
                    wordSense.examplesAudioSrc.add(audioSrc);
                }

                englishWord.senses.add(wordSense);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return englishWord;
    }
}
