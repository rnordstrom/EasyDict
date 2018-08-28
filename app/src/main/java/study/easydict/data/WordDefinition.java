package study.easydict.data;

public class WordDefinition {

    private String wordJp;
    private String engDef;
    private String furigana;
    private int numForms;
    private boolean hasException;
    private boolean lookedUp;

    public WordDefinition(String wordJp, String engDef, String furigana, int numForms) {
        this.wordJp = wordJp;
        this.engDef = engDef;
        this.furigana = furigana;
        this.numForms = numForms;
        hasException = false;
        lookedUp = false;
    }

    public String getWordJp() {
        return wordJp;
    }

    public void setWordJp(String wordJp) {
        this.wordJp = wordJp;
    }

    public String getEngDef() {
        return engDef;
    }

    public void setEngDef(String engDef) {
        this.engDef = engDef;
    }

    public String getFurigana() {
        return furigana;
    }

    public void setFurigana(String furigana) {
        this.furigana = furigana;
    }

    public int getNumForms() {
        return numForms;
    }

    public void setNumForms(int numForms) {
        this.numForms = numForms;
    }

    public boolean isHasException() {
        return hasException;
    }

    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }

    public boolean isLookedUp() {
        return lookedUp;
    }

    public void setLookedUp(boolean lookedUp) {
        this.lookedUp = lookedUp;
    }
}
